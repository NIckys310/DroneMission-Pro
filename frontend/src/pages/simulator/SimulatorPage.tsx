import { useCallback, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  Alert, Box, Button, Card, CardContent, CardHeader, Chip, Divider, Grid2 as Grid, LinearProgress, List, ListItem, ListItemIcon,
  ListItemText, MenuItem, Slider, Stack, TextField, ToggleButton, ToggleButtonGroup, Tooltip, Typography,
} from '@mui/material';
import FlightTakeoffIcon from '@mui/icons-material/FlightTakeoff';
import PauseIcon from '@mui/icons-material/Pause';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import HomeIcon from '@mui/icons-material/Home';
import RestartAltIcon from '@mui/icons-material/RestartAlt';
import BatteryAlertIcon from '@mui/icons-material/BatteryAlert';
import AirIcon from '@mui/icons-material/Air';
import SignalWifiOffIcon from '@mui/icons-material/SignalWifiOff';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import ViewInArIcon from '@mui/icons-material/ViewInAr';
import { missionApi, simulatorApi } from '../../api/endpoints';
import { errorMessage } from '../../api/client';
import { useAuth } from '../../auth/AuthContext';
import { useFetch } from '../../hooks/useFetch';
import PageHeader from '../../components/PageHeader';
import { missionType } from '../../utils/labels';
import { formatDuration, formatNumber } from '../../utils/format';
import type { FlightPlan } from '../../types';
import {
  createState, injectFailure, isAirborne, returnToHome, step, takeOff, toLocal, togglePause,
  type EventLevel, type Phase, type SimConfig, type SimState,
} from './engine';
import { DroneScene, relocateBase, type CameraMode } from './scene';

/** Misiones de práctica: describen qué hace el drone en cada tipo. */
const PRACTICE = [
  { type: 'INSPECCION', alt: 50, text: 'Órbitas alrededor de una torre de energía buscando puntos calientes.' },
  { type: 'AGRICULTURA', alt: 15, text: 'Zigzag a baja altura fumigando un lote; las plantas cambian de color al rociarlas.' },
  { type: 'VIGILANCIA', alt: 50, text: 'Recorrido perimetral de una planta industrial grabando video.' },
  { type: 'TOPOGRAFIA', alt: 80, text: 'Cuadrícula fotogramétrica sobre una urbanización; se ve la cobertura de cada foto.' },
  { type: 'FOTOGRAFIA', alt: 60, text: 'Órbita fotográfica alrededor de un objetivo.' },
  { type: 'MONITOREO', alt: 40, text: 'Recorrido sobre un cultivo de café para revisar su estado.' },
];

const WIND_DIRS = [
  { label: 'Norte', deg: 0 }, { label: 'Noreste', deg: 45 }, { label: 'Este', deg: 90 }, { label: 'Sureste', deg: 135 },
  { label: 'Sur', deg: 180 }, { label: 'Suroeste', deg: 225 }, { label: 'Oeste', deg: 270 }, { label: 'Noroeste', deg: 315 },
];

const PHASE_LABEL: Record<Phase, { label: string; color: 'default' | 'info' | 'primary' | 'warning' | 'success' | 'error' }> = {
  EN_TIERRA: { label: 'En tierra', color: 'default' },
  DESPEGANDO: { label: 'Despegando', color: 'info' },
  EN_RUTA: { label: 'En ruta', color: 'primary' },
  REGRESANDO: { label: 'Regresando a base', color: 'warning' },
  ATERRIZANDO: { label: 'Aterrizando', color: 'info' },
  COMPLETADA: { label: 'En tierra · vuelo terminado', color: 'success' },
  EMERGENCIA: { label: 'Aterrizaje de emergencia', color: 'error' },
};

const EVENT_ICON: Record<EventLevel, JSX.Element> = {
  info: <InfoOutlinedIcon fontSize="small" color="info" />,
  success: <CheckCircleOutlineIcon fontSize="small" color="success" />,
  warning: <WarningAmberIcon fontSize="small" color="warning" />,
  error: <ErrorOutlineIcon fontSize="small" color="error" />,
};

interface Loaded { title: string; type: string; plan: FlightPlan; config: SimConfig }

/**
 * Simulador de vuelo 3D: el drone virtual recorre el plan de vuelo de una
 * misión (real o de práctica) con física simple, viento, batería y emergencias.
 */
export default function SimulatorPage() {
  const { can } = useAuth();
  const canSeeMissions = can('MISIONES_VER');
  const [params] = useSearchParams();
  const [source, setSource] = useState<'practica' | 'mision'>(params.get('missionId') && canSeeMissions ? 'mision' : 'practica');
  const [practiceType, setPracticeType] = useState('AGRICULTURA');
  const [altitude, setAltitude] = useState(15);
  const [missionId, setMissionId] = useState<string>(params.get('missionId') ?? '');
  const [wind, setWind] = useState(8);
  const [windDir, setWindDir] = useState(90);
  const [startBattery, setStartBattery] = useState(100);
  const [speed, setSpeed] = useState(2);
  const [camera, setCamera] = useState<CameraMode>('seguimiento');
  const [loaded, setLoaded] = useState<Loaded | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [hud, setHud] = useState<SimState | null>(null);

  const missions = useFetch(() => (canSeeMissions ? missionApi.list() : Promise.resolve([])), [canSeeMissions]);
  const viewport = useRef<HTMLDivElement>(null);
  const sceneRef = useRef<DroneScene | null>(null);
  const stateRef = useRef<SimState | null>(null);
  const configRef = useRef<SimConfig | null>(null);
  const speedRef = useRef(speed);
  speedRef.current = speed;

  // Crea la escena 3D una sola vez y corre el ciclo de animación
  useEffect(() => {
    if (!viewport.current) return undefined;
    const scene = new DroneScene(viewport.current);
    sceneRef.current = scene;
    let last = performance.now();
    let raf = 0;
    let hudTimer = 0;
    const loop = (now: number) => {
      const dt = Math.min(0.1, (now - last) / 1000);
      last = now;
      const s = stateRef.current;
      const cfg = configRef.current;
      if (s && cfg) {
        // Varios subpasos para que la física sea estable a velocidades altas
        const total = dt * speedRef.current;
        const steps = Math.ceil(total / 0.05);
        for (let i = 0; i < steps; i++) step(s, cfg, total / steps);
        scene.update(s, dt);
        hudTimer += dt;
        if (hudTimer > 0.1) { hudTimer = 0; setHud({ ...s, events: [...s.events] }); }
      }
      raf = requestAnimationFrame(loop);
    };
    raf = requestAnimationFrame(loop);
    return () => { cancelAnimationFrame(raf); scene.dispose(); sceneRef.current = null; };
  }, []);

  useEffect(() => { sceneRef.current?.setCameraMode(camera); }, [camera]);

  const start = useCallback((data: Loaded) => {
    configRef.current = data.config;
    stateRef.current = createState(data.config);
    sceneRef.current?.setMission(data.type, data.config.plan);
    setLoaded(data);
  }, []);

  const load = useCallback(async () => {
    setError(null);
    try {
      let type = practiceType;
      let title = `Práctica · ${missionType[practiceType]}`;
      let plan: FlightPlan;
      if (source === 'mision') {
        const m = missions.data?.find((x) => String(x.id) === missionId);
        if (!m) { setError('Selecciona una misión'); return; }
        type = m.type;
        title = `${m.code} · ${m.name}`;
        plan = await missionApi.flightPlan(m.id);
      } else {
        plan = await simulatorApi.plan(practiceType, altitude);
      }
      const local = relocateBase(toLocal(plan.waypoints), type);
      start({
        title, type, plan,
        config: { plan: local, cruiseSpeedKmh: plan.cruiseSpeedKmh, drainPerMinute: plan.estimatedBatteryUsePercent / plan.estimatedDurationMin,
          windKmh: wind, windDirDeg: windDir, startBattery },
      });
    } catch (e) {
      setError(errorMessage(e));
    }
  }, [source, practiceType, altitude, missionId, missions.data, wind, windDir, startBattery, start]);

  // Carga automática: misión indicada en la URL o práctica por defecto
  const autoLoaded = useRef(false);
  useEffect(() => {
    if (autoLoaded.current) return;
    if (source === 'mision' && !missions.data) return;
    autoLoaded.current = true;
    load();
  }, [load, source, missions.data]);

  // El viento se puede cambiar en pleno vuelo
  useEffect(() => {
    if (configRef.current) { configRef.current.windKmh = wind; configRef.current.windDirDeg = windDir; }
  }, [wind, windDir]);

  // Atajos de teclado: espacio = pausa, H = regresar a base
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      const s = stateRef.current;
      if (!s || (e.target as HTMLElement).tagName === 'INPUT') return;
      if (e.code === 'Space') { e.preventDefault(); if (s.phase === 'EN_TIERRA') takeOff(s); else togglePause(s); }
      if (e.key.toLowerCase() === 'h') returnToHome(s);
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, []);

  const s = hud;
  const cmd = (fn: (st: SimState) => void) => () => { if (stateRef.current) fn(stateRef.current); };
  const total = loaded ? loaded.config.plan.length - 1 : 0;
  const progress = s && total ? Math.min(100, ((s.completedRoute ? total : s.target - 1) / (total - 1)) * 100) : 0;
  const airborne = s ? isAirborne(s) : false;
  const batteryColor = !s ? 'success' : s.battery < 20 ? 'error' : s.battery < 40 ? 'warning' : 'success';
  const finished = s?.phase === 'COMPLETADA';

  return (
    <>
      <PageHeader title="Simulador de vuelo 3D" subtitle="Practica misiones con un drone virtual: física, viento, batería y emergencias. Datos simulados." />
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Grid container spacing={2}>
        {/* ------------------------------------------------ panel de configuración */}
        <Grid size={{ xs: 12, lg: 3 }}>
          <Card>
            <CardHeader title="Configurar vuelo" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <CardContent sx={{ pt: 0, display: 'grid', gap: 2 }}>
              {canSeeMissions && (
                <ToggleButtonGroup exclusive fullWidth size="small" value={source} onChange={(_, v) => v && setSource(v)}>
                  <ToggleButton value="practica">Práctica</ToggleButton>
                  <ToggleButton value="mision">Misión real</ToggleButton>
                </ToggleButtonGroup>
              )}
              {source === 'practica' ? (
                <>
                  <Stack spacing={1}>
                    {PRACTICE.map((p) => (
                      <Box key={p.type} onClick={() => { setPracticeType(p.type); setAltitude(p.alt); }}
                        sx={{ p: 1.2, borderRadius: 2, cursor: 'pointer', border: '1px solid', borderColor: practiceType === p.type ? 'primary.main' : 'divider', bgcolor: practiceType === p.type ? 'rgba(14,90,156,0.06)' : 'transparent' }}>
                        <Typography variant="body2" fontWeight={700}>{missionType[p.type]}</Typography>
                        <Typography variant="caption" color="text.secondary">{p.text}</Typography>
                      </Box>
                    ))}
                  </Stack>
                  <Box>
                    <Typography variant="caption" color="text.secondary">Altitud de vuelo: {altitude} m</Typography>
                    <Slider size="small" min={5} max={120} value={altitude} onChange={(_, v) => setAltitude(v as number)} />
                  </Box>
                </>
              ) : (
                <TextField select label="Misión" value={missionId} onChange={(e) => setMissionId(e.target.value)}>
                  {(missions.data ?? []).map((m) => <MenuItem key={m.id} value={String(m.id)}>{m.code} · {m.name}</MenuItem>)}
                </TextField>
              )}
              <Divider />
              <Box>
                <Typography variant="caption" color="text.secondary">Viento: {wind} km/h (se puede cambiar en vuelo)</Typography>
                <Slider size="small" min={0} max={45} value={wind} onChange={(_, v) => setWind(v as number)} />
              </Box>
              <TextField select label="El viento sopla hacia" value={windDir} onChange={(e) => setWindDir(Number(e.target.value))}>
                {WIND_DIRS.map((d) => <MenuItem key={d.deg} value={d.deg}>{d.label}</MenuItem>)}
              </TextField>
              <Box>
                <Typography variant="caption" color="text.secondary">Batería al despegar: {startBattery} %</Typography>
                <Slider size="small" min={25} max={100} value={startBattery} onChange={(_, v) => setStartBattery(v as number)} />
              </Box>
              <Button variant="contained" startIcon={<ViewInArIcon />} onClick={load}>Cargar en el simulador</Button>
            </CardContent>
          </Card>
        </Grid>

        {/* ------------------------------------------------ vista 3D y controles */}
        <Grid size={{ xs: 12, lg: 9 }}>
          <Card>
            <Box sx={{ position: 'relative', height: { xs: 380, md: 560 }, bgcolor: '#0b2238' }}>
              <Box ref={viewport} sx={{ position: 'absolute', inset: 0 }} />
              {/* Indicadores superpuestos (HUD) */}
              <Box sx={{ position: 'absolute', top: 12, left: 12, display: 'flex', gap: 1, flexWrap: 'wrap', pointerEvents: 'none' }}>
                {s && <Chip size="small" color={PHASE_LABEL[s.phase].color} label={s.paused ? 'En pausa (hover)' : PHASE_LABEL[s.phase].label} />}
                <Chip size="small" label="SIMULACIÓN" sx={{ bgcolor: 'rgba(255,179,0,0.9)', fontWeight: 700 }} />
                {s?.spraying && <Chip size="small" color="secondary" label="Fumigando" />}
                {s?.recording && airborne && <Chip size="small" color="error" label="● REC" />}
                {s && s.signalLostTimer > 0 && <Chip size="small" color="error" label="SIN SEÑAL" />}
              </Box>
              {loaded && (
                <Box sx={{ position: 'absolute', top: 12, right: 12, color: '#fff', textAlign: 'right', pointerEvents: 'none', textShadow: '0 1px 3px rgba(0,0,0,0.6)' }}>
                  <Typography variant="body2" fontWeight={700}>{loaded.title}</Typography>
                  <Typography variant="caption">{loaded.plan.strategy}</Typography>
                  <Box sx={{ mt: 1, display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: 1 }}>
                    <svg width="54" height="54" viewBox="-27 -27 54 54" aria-label="Brújula">
                      <circle r="25" fill="rgba(0,0,0,0.45)" stroke="#fff" strokeOpacity="0.6" />
                      <text y="-14" textAnchor="middle" fontSize="9" fill="#fff" fontWeight="700">N</text>
                      <g transform={`rotate(${s ? (s.heading * 180) / Math.PI : 0})`}>
                        <path d="M0,-17 L5,6 L0,2 L-5,6 Z" fill="#00e5ff" />
                      </g>
                    </svg>
                    <Box sx={{ fontSize: 12 }}>
                      <AirIcon sx={{ fontSize: 16, verticalAlign: 'middle' }} /> {wind} km/h
                    </Box>
                  </Box>
                </Box>
              )}
              {s && (
                <Box sx={{ position: 'absolute', left: 12, right: 12, bottom: 12, display: 'grid', gridTemplateColumns: { xs: 'repeat(3, 1fr)', md: 'repeat(7, 1fr)' }, gap: 1, pointerEvents: 'none' }}>
                  {[
                    ['Altitud', `${formatNumber(s.pos.y, 1)} m`],
                    ['Velocidad', `${formatNumber(Math.hypot(s.vel.x, s.vel.z) * 3.6, 1)} km/h`],
                    ['Distancia', `${formatNumber(s.distance)} m`],
                    ['Tiempo', formatDuration(Math.round(s.time))],
                    ['Punto', `${Math.min(Math.max(s.target - 1, 0), total - 1)}/${total - 1}`],
                    [s.sprayedMeters > 0 ? 'Fumigado' : 'Fotos', s.sprayedMeters > 0 ? `${formatNumber(s.sprayedMeters)} m` : String(s.photos)],
                  ].map(([label, value]) => (
                    <Box key={label} sx={{ bgcolor: 'rgba(5,20,35,0.72)', color: '#fff', borderRadius: 1.5, px: 1.2, py: 0.6 }}>
                      <Typography sx={{ fontSize: 10, opacity: 0.75, textTransform: 'uppercase', letterSpacing: 0.5 }}>{label}</Typography>
                      <Typography sx={{ fontSize: 15, fontWeight: 700, fontVariantNumeric: 'tabular-nums' }}>{value}</Typography>
                    </Box>
                  ))}
                  <Box sx={{ bgcolor: 'rgba(5,20,35,0.72)', color: '#fff', borderRadius: 1.5, px: 1.2, py: 0.6 }}>
                    <Typography sx={{ fontSize: 10, opacity: 0.75, textTransform: 'uppercase', letterSpacing: 0.5 }}>Batería</Typography>
                    <Typography sx={{ fontSize: 15, fontWeight: 700 }}>{Math.round(s.battery)} %</Typography>
                    <LinearProgress variant="determinate" value={s.battery} color={batteryColor} sx={{ height: 4, borderRadius: 2 }} />
                  </Box>
                </Box>
              )}
            </Box>
            <CardContent>
              <LinearProgress variant="determinate" value={progress} sx={{ height: 6, borderRadius: 3, mb: 2 }} />
              <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} justifyContent="space-between" alignItems={{ md: 'center' }}>
                <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                  <Tooltip title="Atajo: barra espaciadora">
                    <span><Button variant="contained" startIcon={<FlightTakeoffIcon />} disabled={s?.phase !== 'EN_TIERRA'} onClick={cmd(takeOff)}>Despegar</Button></span>
                  </Tooltip>
                  <Tooltip title="Atajo: barra espaciadora">
                    <span><Button variant="outlined" startIcon={s?.paused ? <PlayArrowIcon /> : <PauseIcon />} disabled={!airborne} onClick={cmd(togglePause)}>{s?.paused ? 'Reanudar' : 'Pausar'}</Button></span>
                  </Tooltip>
                  <Tooltip title="Atajo: tecla H">
                    <span><Button variant="outlined" color="warning" startIcon={<HomeIcon />} disabled={s?.phase !== 'EN_RUTA'} onClick={cmd((st) => returnToHome(st))}>Regresar a base</Button></span>
                  </Tooltip>
                  <Button startIcon={<RestartAltIcon />} disabled={!loaded} onClick={() => loaded && start(loaded)}>Reiniciar</Button>
                </Stack>
                <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap alignItems="center">
                  <ToggleButtonGroup exclusive size="small" value={speed} onChange={(_, v) => v && setSpeed(v)}>
                    {[1, 2, 5, 10].map((x) => <ToggleButton key={x} value={x}>{x}×</ToggleButton>)}
                  </ToggleButtonGroup>
                  <ToggleButtonGroup exclusive size="small" value={camera} onChange={(_, v) => v && setCamera(v)}>
                    <ToggleButton value="seguimiento">Seguir</ToggleButton>
                    <ToggleButton value="cenital">Cenital</ToggleButton>
                    <ToggleButton value="libre">Libre</ToggleButton>
                  </ToggleButtonGroup>
                </Stack>
              </Stack>
              <Stack direction="row" spacing={1} sx={{ mt: 2 }} flexWrap="wrap" useFlexGap alignItems="center">
                <Typography variant="caption" color="text.secondary" sx={{ mr: 1 }}>Simular emergencia:</Typography>
                <Button size="small" color="error" variant="outlined" startIcon={<BatteryAlertIcon />} disabled={!airborne} onClick={cmd((st) => injectFailure(st, 'bateria'))}>Falla de batería</Button>
                <Button size="small" color="warning" variant="outlined" startIcon={<AirIcon />} disabled={!airborne} onClick={cmd((st) => injectFailure(st, 'viento'))}>Ráfaga de viento</Button>
                <Button size="small" color="error" variant="outlined" startIcon={<SignalWifiOffIcon />} disabled={!airborne} onClick={cmd((st) => injectFailure(st, 'senal'))}>Pérdida de señal</Button>
                {camera === 'libre' && <Typography variant="caption" color="text.secondary">Cámara libre: arrastra para girar, rueda para acercar.</Typography>}
              </Stack>
            </CardContent>
          </Card>

          <Grid container spacing={2} sx={{ mt: 0 }}>
            <Grid size={{ xs: 12, md: 7 }}>
              <Card sx={{ height: '100%' }}>
                <CardHeader title="Bitácora de vuelo" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
                <List dense sx={{ maxHeight: 260, overflow: 'auto', pt: 0 }}>
                  {s?.events.map((e, i) => (
                    <ListItem key={`${e.t}-${i}`}>
                      <ListItemIcon sx={{ minWidth: 32 }}>{EVENT_ICON[e.level]}</ListItemIcon>
                      <ListItemText primary={e.text} secondary={`T+${formatDuration(Math.round(e.t))}`} />
                    </ListItem>
                  ))}
                </List>
              </Card>
            </Grid>
            <Grid size={{ xs: 12, md: 5 }}>
              <Card sx={{ height: '100%' }}>
                <CardHeader title={finished ? 'Resultado del vuelo' : 'Plan de vuelo'} titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
                <CardContent sx={{ pt: 0 }}>
                  {loaded && (
                    <Box component="dl" sx={{ display: 'grid', gridTemplateColumns: 'auto 1fr', gap: 0.75, m: 0, '& dt': { color: 'text.secondary', fontSize: 13 }, '& dd': { m: 0, fontWeight: 600, fontSize: 13, textAlign: 'right' } }}>
                      <dt>Patrón</dt><dd>{loaded.plan.strategy}</dd>
                      <dt>Puntos de ruta</dt><dd>{loaded.plan.waypoints.length}</dd>
                      <dt>Velocidad crucero</dt><dd>{loaded.plan.cruiseSpeedKmh} km/h</dd>
                      <dt>Distancia estimada</dt><dd>{formatNumber(loaded.plan.estimatedDistanceM)} m</dd>
                      <dt>Duración estimada</dt><dd>{loaded.plan.estimatedDurationMin} min</dd>
                      <dt>Consumo estimado</dt><dd>{loaded.plan.estimatedBatteryUsePercent} %</dd>
                      {s && finished && (
                        <>
                          <dt>Ruta completa</dt><dd>{s.completedRoute ? 'Sí' : 'No (regreso anticipado)'}</dd>
                          <dt>Tiempo real de vuelo</dt><dd>{formatDuration(Math.round(s.time))} min</dd>
                          <dt>Distancia recorrida</dt><dd>{formatNumber(s.distance)} m</dd>
                          <dt>Fotos / escaneos</dt><dd>{s.photos}</dd>
                          <dt>Metros fumigados</dt><dd>{formatNumber(s.sprayedMeters)} m</dd>
                          <dt>Batería final</dt><dd>{Math.round(s.battery)} %</dd>
                        </>
                      )}
                    </Box>
                  )}
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 2 }}>
                    Los datos de este simulador son de práctica y no se guardan. El patrón de vuelo es el mismo que usan las misiones reales.
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </Grid>
      </Grid>
    </>
  );
}
