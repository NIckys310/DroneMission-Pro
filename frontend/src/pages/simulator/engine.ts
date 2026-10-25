// =====================================================================
// Motor de física del simulador 3D (se ejecuta en el navegador).
// Coordenadas locales en metros: x = este, z = sur (convención Three.js),
// y = altitud. El drone sigue el plan de vuelo generado por el backend.
// =====================================================================

export interface Vec3 { x: number; y: number; z: number }

export interface PlanPoint { x: number; z: number; alt: number; action: string }

export type Phase =
  | 'EN_TIERRA' | 'DESPEGANDO' | 'EN_RUTA' | 'REGRESANDO' | 'ATERRIZANDO' | 'COMPLETADA' | 'EMERGENCIA';

export type EventLevel = 'info' | 'success' | 'warning' | 'error';
export interface SimEvent { t: number; level: EventLevel; text: string }

export interface SimConfig {
  plan: PlanPoint[];
  cruiseSpeedKmh: number;
  /** % de batería por minuto de vuelo (viene de la estrategia del backend). */
  drainPerMinute: number;
  windKmh: number;
  /** Dirección hacia la que sopla el viento (0 = norte, 90 = este). */
  windDirDeg: number;
  startBattery: number;
}

export interface SimState {
  pos: Vec3;
  vel: Vec3;
  heading: number;          // radianes, 0 = norte
  phase: Phase;
  target: number;           // índice del punto del plan al que se dirige
  battery: number;
  time: number;             // segundos de vuelo simulados
  distance: number;         // metros recorridos (horizontal)
  photos: number;
  sprayedMeters: number;
  spraying: boolean;
  recording: boolean;
  paused: boolean;
  events: SimEvent[];
  trail: Vec3[];
  footprints: Vec3[];       // lugares donde se tomaron fotos / escaneos
  gustTimer: number;        // segundos restantes de ráfaga de viento
  signalLostTimer: number;  // segundos restantes de pérdida de señal
  lowBatteryWarned: boolean;
  completedRoute: boolean;
  lastTrailTime: number;
}

const CLIMB_RATE = 3;        // m/s
const DESCENT_RATE = 2;      // m/s
const ARRIVAL_RADIUS = 2.5;  // m
const RTH_ALTITUDE = 30;     // m
const LOW_BATTERY = 25;
const CRITICAL_BATTERY = 18;
const EMERGENCY_BATTERY = 6;

export function createState(cfg: SimConfig): SimState {
  const home = cfg.plan[0];
  return {
    pos: { x: home.x, y: 0, z: home.z }, vel: { x: 0, y: 0, z: 0 }, heading: 0,
    phase: 'EN_TIERRA', target: 1, battery: cfg.startBattery, time: 0, distance: 0,
    photos: 0, sprayedMeters: 0, spraying: false, recording: false, paused: false,
    events: [{ t: 0, level: 'info', text: 'Drone en la base. Plan de vuelo cargado: ' + (cfg.plan.length) + ' puntos.' }],
    trail: [], footprints: [], gustTimer: 0, signalLostTimer: 0, lowBatteryWarned: false,
    completedRoute: false, lastTrailTime: 0,
  };
}

function log(s: SimState, level: EventLevel, text: string) {
  s.events.unshift({ t: s.time, level, text });
  if (s.events.length > 60) s.events.pop();
}

export const isAirborne = (s: SimState) =>
  s.phase === 'DESPEGANDO' || s.phase === 'EN_RUTA' || s.phase === 'REGRESANDO' || s.phase === 'ATERRIZANDO' || s.phase === 'EMERGENCIA';

// ------------------------------------------------------------------ comandos del piloto

export function takeOff(s: SimState) {
  if (s.phase !== 'EN_TIERRA') return;
  s.phase = 'DESPEGANDO';
  log(s, 'info', 'Despegue autorizado. Motores encendidos.');
}

export function togglePause(s: SimState) {
  if (!isAirborne(s)) return;
  s.paused = !s.paused;
  log(s, 'info', s.paused ? 'Vuelo en pausa: el drone mantiene su posición (hover).' : 'Vuelo reanudado.');
}

export function returnToHome(s: SimState, reason = 'Regreso a base solicitado por el piloto.') {
  if (s.phase !== 'EN_RUTA' && s.phase !== 'DESPEGANDO') return;
  s.phase = 'REGRESANDO';
  s.spraying = false;
  s.paused = false;
  log(s, 'warning', reason);
}

export function injectFailure(s: SimState, kind: 'bateria' | 'viento' | 'senal') {
  if (!isAirborne(s)) return;
  if (kind === 'bateria') {
    s.battery = Math.max(1, s.battery - 35);
    log(s, 'error', 'Falla simulada: celda de batería dañada (-35 %).');
  } else if (kind === 'viento') {
    s.gustTimer = 10;
    log(s, 'warning', 'Ráfaga de viento fuerte durante 10 s: el drone compensa la deriva.');
  } else {
    s.signalLostTimer = 4;
    log(s, 'error', 'Pérdida de señal con el control remoto. Protocolo: hover y regreso a base.');
  }
}

// ------------------------------------------------------------------ paso de simulación

/** Avanza la simulación dt segundos (ya multiplicados por la velocidad de simulación). */
export function step(s: SimState, cfg: SimConfig, dt: number) {
  if (!isAirborne(s) || s.paused || dt <= 0) return;
  s.time += dt;

  // Viento: deriva parcial que el controlador de vuelo no alcanza a corregir + turbulencia
  const gust = s.gustTimer > 0 ? 3 : 1;
  s.gustTimer = Math.max(0, s.gustTimer - dt);
  const wind = (cfg.windKmh / 3.6) * gust;
  const dir = (cfg.windDirDeg * Math.PI) / 180;
  const windX = Math.sin(dir) * wind * 0.25 + (Math.random() - 0.5) * wind * 0.15;
  const windZ = -Math.cos(dir) * wind * 0.25 + (Math.random() - 0.5) * wind * 0.15;

  // Batería: el viento y las maniobras aumentan el consumo
  const drain = cfg.drainPerMinute * (dt / 60) * (1 + (cfg.windKmh * gust) / 50);
  s.battery = Math.max(0, s.battery - drain);
  checkBattery(s);

  // Pérdida de señal: hover unos segundos y luego regreso automático
  if (s.signalLostTimer > 0) {
    s.signalLostTimer -= dt;
    s.vel = { x: windX, y: 0, z: windZ };
    move(s, dt);
    if (s.signalLostTimer <= 0) returnToHome(s, 'Señal no recuperada: regreso automático a base (RTH).');
    return;
  }

  const home = cfg.plan[0];
  const cruise = cfg.cruiseSpeedKmh / 3.6;

  switch (s.phase) {
    case 'DESPEGANDO': {
      const firstAlt = cfg.plan[1]?.alt ?? 20;
      s.vel = { x: 0, y: CLIMB_RATE, z: 0 };
      move(s, dt);
      if (s.pos.y >= firstAlt) {
        s.pos.y = firstAlt;
        s.phase = 'EN_RUTA';
        log(s, 'info', `Altitud de crucero alcanzada (${firstAlt} m). Iniciando ruta programada.`);
      }
      break;
    }
    case 'EN_RUTA': {
      const isLast = s.target >= cfg.plan.length - 1;
      const tp = cfg.plan[s.target];
      const targetAlt = isLast ? Math.max(s.pos.y, 10) : tp.alt;
      if (flyTowards(s, tp.x, tp.z, targetAlt, cruise, windX, windZ, dt)) {
        if (isLast) {
          s.phase = 'ATERRIZANDO';
          s.completedRoute = true;
          s.recording = false;
          s.spraying = false;
          log(s, 'success', 'Ruta completada. Iniciando aterrizaje en la base.');
        } else {
          arriveAt(s, cfg, s.target);
          s.target += 1;
        }
      }
      break;
    }
    case 'REGRESANDO': {
      const alt = Math.max(RTH_ALTITUDE, Math.min(s.pos.y, 60));
      if (flyTowards(s, home.x, home.z, alt, cruise * 1.2, windX, windZ, dt)) {
        s.phase = 'ATERRIZANDO';
        log(s, 'info', 'Sobre la base. Descendiendo.');
      }
      break;
    }
    case 'ATERRIZANDO':
    case 'EMERGENCIA': {
      s.vel = { x: windX * 0.3, y: -DESCENT_RATE, z: windZ * 0.3 };
      move(s, dt);
      if (s.pos.y <= 0) {
        s.pos.y = 0;
        s.vel = { x: 0, y: 0, z: 0 };
        const emergency = s.phase === 'EMERGENCIA';
        s.phase = 'COMPLETADA';
        s.recording = false;
        s.spraying = false;
        log(s, emergency ? 'error' : 'success', emergency
          ? 'Aterrizaje de emergencia fuera de la base. Recuperar el drone en campo.'
          : `Aterrizaje completado. Batería final ${Math.round(s.battery)} %.`);
      }
      break;
    }
    default:
      break;
  }
}

function checkBattery(s: SimState) {
  if (!s.lowBatteryWarned && s.battery <= LOW_BATTERY) {
    s.lowBatteryWarned = true;
    log(s, 'warning', `Batería baja (${Math.round(s.battery)} %).`);
  }
  if (s.battery <= CRITICAL_BATTERY && s.phase === 'EN_RUTA') {
    returnToHome(s, `Batería crítica (${Math.round(s.battery)} %): regreso automático a base.`);
  }
  if (s.battery <= EMERGENCY_BATTERY && (s.phase === 'REGRESANDO' || s.phase === 'EN_RUTA')) {
    s.phase = 'EMERGENCIA';
    s.spraying = false;
    log(s, 'error', 'Batería agotada: aterrizaje de emergencia inmediato.');
  }
}

/** Vuela hacia (x, z, alt). Devuelve true al llegar. */
function flyTowards(s: SimState, x: number, z: number, alt: number, speed: number, windX: number, windZ: number, dt: number): boolean {
  const dx = x - s.pos.x;
  const dz = z - s.pos.z;
  const dist = Math.hypot(dx, dz);
  // Frena al acercarse al punto (llegada suave)
  const desired = Math.min(speed, Math.max(1.5, dist * 0.8));
  const vx = dist > 0.01 ? (dx / dist) * desired : 0;
  const vz = dist > 0.01 ? (dz / dist) * desired : 0;
  const vy = Math.max(-DESCENT_RATE, Math.min(CLIMB_RATE, (alt - s.pos.y) * 1.2));
  // Aceleración limitada (inercia)
  const k = Math.min(1, dt * 2.5);
  s.vel = {
    x: s.vel.x + (vx + windX - s.vel.x) * k,
    y: vy,
    z: s.vel.z + (vz + windZ - s.vel.z) * k,
  };
  move(s, dt);
  if (Math.hypot(s.vel.x, s.vel.z) > 0.3) s.heading = Math.atan2(s.vel.x, -s.vel.z);
  return Math.hypot(x - s.pos.x, z - s.pos.z) < ARRIVAL_RADIUS && Math.abs(alt - s.pos.y) < 3;
}

function move(s: SimState, dt: number) {
  const before = { x: s.pos.x, z: s.pos.z };
  s.pos = { x: s.pos.x + s.vel.x * dt, y: Math.max(0, s.pos.y + s.vel.y * dt), z: s.pos.z + s.vel.z * dt };
  const d = Math.hypot(s.pos.x - before.x, s.pos.z - before.z);
  s.distance += d;
  if (s.spraying) s.sprayedMeters += d;
  if (s.time - s.lastTrailTime >= 0.4) {
    s.trail.push({ ...s.pos });
    if (s.trail.length > 4000) s.trail.shift();
    s.lastTrailTime = s.time;
  }
}

/** Acción que el drone ejecuta al llegar a cada punto del plan. */
function arriveAt(s: SimState, cfg: SimConfig, index: number) {
  const p = cfg.plan[index];
  const n = `Punto ${index}/${cfg.plan.length - 1}`;
  switch (p.action) {
    case 'FOTO':
      s.photos += 1;
      s.footprints.push({ ...s.pos });
      log(s, 'info', `${n}: fotografía capturada (#${s.photos}).`);
      break;
    case 'ESCANEO':
      s.photos += 1;
      s.footprints.push({ ...s.pos });
      if (s.photos % 2 === 1) log(s, 'info', `${n}: línea de escaneo fotogramétrico.`);
      break;
    case 'FUMIGACION':
      s.spraying = true;
      log(s, 'info', `${n}: boquillas abiertas, fumigando hilera.`);
      break;
    case 'PASO':
      if (s.spraying) {
        s.spraying = false;
        log(s, 'info', `${n}: fin de hilera, boquillas cerradas.`);
      }
      break;
    case 'VIDEO':
      if (!s.recording) {
        s.recording = true;
        log(s, 'info', `${n}: grabación de video del perímetro iniciada.`);
      }
      break;
    default:
      break;
  }
}

/** Convierte latitud/longitud a metros locales respecto al primer punto. */
export function toLocal(points: { latitude: number; longitude: number; altitudeM: number; action: string }[]): PlanPoint[] {
  if (points.length === 0) return [];
  const lat0 = points[0].latitude;
  const lon0 = points[0].longitude;
  const m = 111_320;
  const cos = Math.cos((lat0 * Math.PI) / 180);
  return points.map((p) => ({
    x: (p.longitude - lon0) * cos * m,
    z: -(p.latitude - lat0) * m,
    alt: p.altitudeM,
    action: p.action,
  }));
}
