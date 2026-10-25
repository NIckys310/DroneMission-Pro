// =====================================================================
// Escena 3D del simulador (Three.js): drone, entorno según el tipo de
// misión, ruta programada, recorrido, fumigación y fotos.
// =====================================================================
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import type { PlanPoint, SimState } from './engine';

export type CameraMode = 'seguimiento' | 'cenital' | 'libre';

const ACTION_COLORS: Record<string, number> = {
  DESPEGUE: 0xffb300, ATERRIZAJE: 0xffb300, FOTO: 0x42a5f5, ESCANEO: 0xab47bc,
  VIDEO: 0xef5350, FUMIGACION: 0x26a69a, PASO: 0x90a4ae,
};

/** Generador pseudoaleatorio con semilla: el paisaje es igual en cada carga. */
function seeded(seed: number) {
  let s = seed;
  return () => {
    s = (s * 16807) % 2147483647;
    return (s - 1) / 2147483646;
  };
}

/**
 * Ubica la base de despegue junto al área de trabajo (no encima del objetivo),
 * como se hace en una operación real. Solo cambia el primer y el último punto.
 */
export function relocateBase(plan: PlanPoint[], type: string): PlanPoint[] {
  const offset: Record<string, number> = {
    INSPECCION: 40, FOTOGRAFIA: 40, AGRICULTURA: 95, TOPOGRAFIA: 120, VIGILANCIA: 75, MONITOREO: 75,
  };
  const z = offset[type] ?? 40;
  return plan.map((p, i) => (i === 0 || i === plan.length - 1 ? { ...p, x: 0, z } : p));
}

export class DroneScene {
  private renderer: THREE.WebGLRenderer;
  private scene = new THREE.Scene();
  private camera: THREE.PerspectiveCamera;
  private controls: OrbitControls;
  private drone = new THREE.Group();
  private props: THREE.Object3D[] = [];
  private leds: THREE.MeshBasicMaterial[] = [];
  private world = new THREE.Group();
  private routeGroup = new THREE.Group();
  private footprintGroup = new THREE.Group();
  private trail: THREE.Line;
  private trailPositions = new Float32Array(4000 * 3);
  private spray: THREE.Points;
  private sprayVel: Float32Array;
  private sprayLife: Float32Array;
  private crops: THREE.InstancedMesh | null = null;
  private cropPositions: { x: number; z: number }[] = [];
  private sprayedCrops = new Set<number>();
  private footprintsDrawn = 0;
  private frame = 0;
  private resizeObserver: ResizeObserver;
  private cameraMode: CameraMode = 'seguimiento';

  constructor(private container: HTMLElement) {
    this.renderer = new THREE.WebGLRenderer({ antialias: true });
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    this.renderer.shadowMap.enabled = true;
    this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
    this.renderer.outputColorSpace = THREE.SRGBColorSpace;
    container.appendChild(this.renderer.domElement);
    this.renderer.domElement.style.display = 'block';

    this.camera = new THREE.PerspectiveCamera(55, 1, 0.5, 4000);
    this.camera.position.set(0, 60, 160);
    this.controls = new OrbitControls(this.camera, this.renderer.domElement);
    this.controls.enableDamping = true;
    this.controls.maxPolarAngle = Math.PI / 2.05;
    this.controls.enabled = false;

    this.buildSkyAndLights();
    this.buildGround();
    this.buildDrone();
    this.scene.add(this.world, this.routeGroup, this.footprintGroup, this.drone);

    // Recorrido real del drone (línea cian)
    const trailGeo = new THREE.BufferGeometry();
    trailGeo.setAttribute('position', new THREE.BufferAttribute(this.trailPositions, 3));
    trailGeo.setDrawRange(0, 0);
    this.trail = new THREE.Line(trailGeo, new THREE.LineBasicMaterial({ color: 0x00e5ff }));
    this.trail.frustumCulled = false;
    this.scene.add(this.trail);

    // Partículas de fumigación
    const n = 600;
    const sprayGeo = new THREE.BufferGeometry();
    sprayGeo.setAttribute('position', new THREE.BufferAttribute(new Float32Array(n * 3).fill(-999), 3));
    this.spray = new THREE.Points(sprayGeo, new THREE.PointsMaterial({ color: 0xb3e5fc, size: 0.35, transparent: true, opacity: 0.75 }));
    this.spray.frustumCulled = false;
    this.sprayVel = new Float32Array(n * 3);
    this.sprayLife = new Float32Array(n);
    this.scene.add(this.spray);

    this.resizeObserver = new ResizeObserver(() => this.resize());
    this.resizeObserver.observe(container);
    this.resize();
  }

  // ------------------------------------------------------------------ construcción

  private buildSkyAndLights() {
    const c = document.createElement('canvas');
    c.width = 2; c.height = 256;
    const g = c.getContext('2d')!;
    const grad = g.createLinearGradient(0, 0, 0, 256);
    grad.addColorStop(0, '#5b9bd5');
    grad.addColorStop(0.6, '#a9d1f0');
    grad.addColorStop(1, '#e8f1f8');
    g.fillStyle = grad;
    g.fillRect(0, 0, 2, 256);
    const sky = new THREE.CanvasTexture(c);
    sky.colorSpace = THREE.SRGBColorSpace;
    this.scene.background = sky;
    this.scene.fog = new THREE.Fog(0xdbe9f4, 350, 1400);

    this.scene.add(new THREE.HemisphereLight(0xdcefff, 0x55703a, 1.1));
    const sun = new THREE.DirectionalLight(0xfff4e0, 2.4);
    sun.position.set(220, 320, 140);
    sun.castShadow = true;
    sun.shadow.mapSize.set(2048, 2048);
    const sc = sun.shadow.camera;
    sc.left = -260; sc.right = 260; sc.top = 260; sc.bottom = -260; sc.far = 900;
    this.scene.add(sun);
  }

  private buildGround() {
    const c = document.createElement('canvas');
    c.width = 512; c.height = 512;
    const g = c.getContext('2d')!;
    g.fillStyle = '#6f9a4c';
    g.fillRect(0, 0, 512, 512);
    const rnd = seeded(7);
    for (let i = 0; i < 1800; i++) {
      const shade = 70 + Math.floor(rnd() * 50);
      g.fillStyle = `rgba(${shade - 20}, ${shade + 60}, ${shade - 30}, ${0.15 + rnd() * 0.2})`;
      g.beginPath();
      g.arc(rnd() * 512, rnd() * 512, 2 + rnd() * 14, 0, Math.PI * 2);
      g.fill();
    }
    const tex = new THREE.CanvasTexture(c);
    tex.wrapS = tex.wrapT = THREE.RepeatWrapping;
    tex.repeat.set(40, 40);
    tex.colorSpace = THREE.SRGBColorSpace;
    const ground = new THREE.Mesh(new THREE.PlaneGeometry(4000, 4000), new THREE.MeshStandardMaterial({ map: tex, roughness: 1 }));
    ground.rotation.x = -Math.PI / 2;
    ground.receiveShadow = true;
    this.scene.add(ground);
  }

  private buildDrone() {
    const body = new THREE.MeshStandardMaterial({ color: 0x2b3440, roughness: 0.4, metalness: 0.3 });
    const accent = new THREE.MeshStandardMaterial({ color: 0x00acc1, roughness: 0.5 });
    const dark = new THREE.MeshStandardMaterial({ color: 0x111111, roughness: 0.6 });
    const add = (mesh: THREE.Mesh) => { mesh.castShadow = true; this.drone.add(mesh); return mesh; };

    add(new THREE.Mesh(new THREE.BoxGeometry(1.1, 0.35, 1.5), body));
    const top = add(new THREE.Mesh(new THREE.BoxGeometry(0.7, 0.15, 0.9), accent));
    top.position.y = 0.25;
    const gimbal = add(new THREE.Mesh(new THREE.SphereGeometry(0.2, 16, 12), dark));
    gimbal.position.set(0, -0.3, -0.55);

    const corners: [number, number][] = [[1, 1], [-1, 1], [1, -1], [-1, -1]];
    corners.forEach(([sx, sz], i) => {
      const arm = add(new THREE.Mesh(new THREE.BoxGeometry(0.12, 0.08, 1.9), body));
      arm.position.set(sx * 0.65, 0, sz * 0.65);
      arm.rotation.y = Math.atan2(sx, sz);
      const motor = add(new THREE.Mesh(new THREE.CylinderGeometry(0.17, 0.17, 0.25, 16), dark));
      motor.position.set(sx * 1.3, 0.12, sz * 1.3);
      const prop = new THREE.Mesh(new THREE.BoxGeometry(1.5, 0.02, 0.12), new THREE.MeshStandardMaterial({ color: 0xdddddd, transparent: true, opacity: 0.85 }));
      prop.position.set(sx * 1.3, 0.27, sz * 1.3);
      this.drone.add(prop);
      this.props.push(prop);
      // LED: rojo al frente (-z), verde atrás
      const ledMat = new THREE.MeshBasicMaterial({ color: sz < 0 ? 0xff1744 : 0x00e676 });
      const led = new THREE.Mesh(new THREE.SphereGeometry(0.07, 8, 8), ledMat);
      led.position.set(sx * 1.3, -0.05, sz * 1.3);
      this.drone.add(led);
      if (i < 4) this.leds.push(ledMat);
    });
    const legMat = new THREE.MeshStandardMaterial({ color: 0x555555 });
    [-0.45, 0.45].forEach((x) => {
      const leg = add(new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.5, 0.06), legMat));
      leg.position.set(x, -0.4, 0);
      const skid = add(new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.06, 1.2), legMat));
      skid.position.set(x, -0.65, 0);
    });
    this.drone.scale.setScalar(2.2); // más grande que un drone real para que se vea bien en pantalla
    this.drone.rotation.order = 'YXZ';
  }

  /** Construye el entorno y la ruta de una misión. */
  setMission(type: string, plan: PlanPoint[]) {
    this.clearGroup(this.world);
    this.clearGroup(this.routeGroup);
    this.clearGroup(this.footprintGroup);
    this.crops = null;
    this.cropPositions = [];
    this.sprayedCrops.clear();
    this.footprintsDrawn = 0;

    const xs = plan.map((p) => p.x);
    const zs = plan.map((p) => p.z);
    const bounds = { minX: Math.min(...xs), maxX: Math.max(...xs), minZ: Math.min(...zs), maxZ: Math.max(...zs) };

    if (type === 'INSPECCION' || type === 'FOTOGRAFIA') this.buildTower();
    else if (type === 'AGRICULTURA') this.buildField(210, 160, 0x7cb342, 0.9);
    else if (type === 'MONITOREO') { this.buildField(260, 170, 0x2e7d32, 1.4); this.buildFence(bounds); }
    else if (type === 'VIGILANCIA') { this.buildPlant(); this.buildFence(bounds); }
    else if (type === 'TOPOGRAFIA') this.buildHousing();
    this.buildTrees(bounds);
    this.buildRoute(plan);
  }

  private clearGroup(group: THREE.Group) {
    group.traverse((o) => {
      const mesh = o as THREE.Mesh;
      mesh.geometry?.dispose();
      const mat = mesh.material as THREE.Material | THREE.Material[] | undefined;
      if (Array.isArray(mat)) mat.forEach((m) => m.dispose()); else mat?.dispose();
    });
    group.clear();
  }

  private box(w: number, h: number, d: number, color: number, x: number, y: number, z: number, group = this.world) {
    const mesh = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), new THREE.MeshStandardMaterial({ color, roughness: 0.85 }));
    mesh.position.set(x, y, z);
    mesh.castShadow = true;
    mesh.receiveShadow = true;
    group.add(mesh);
    return mesh;
  }

  /** Torre de transmisión de energía (objetivo de inspección). */
  private buildTower() {
    const steel = 0x8a939b;
    const H = 46;
    const beam = (a: THREE.Vector3, b: THREE.Vector3, t = 0.35) => {
      const len = a.distanceTo(b);
      const m = new THREE.Mesh(new THREE.BoxGeometry(t, len, t), new THREE.MeshStandardMaterial({ color: steel, metalness: 0.6, roughness: 0.4 }));
      m.position.copy(a.clone().add(b).multiplyScalar(0.5));
      m.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), b.clone().sub(a).normalize());
      m.castShadow = true;
      this.world.add(m);
    };
    const leg = (y: number) => 6 - (y / H) * 4.5;
    const corners = [[1, 1], [-1, 1], [-1, -1], [1, -1]];
    corners.forEach(([sx, sz]) => beam(new THREE.Vector3(sx * 6, 0, sz * 6), new THREE.Vector3(sx * 1.5, H, sz * 1.5), 0.5));
    for (let y = 4; y < H; y += 6) {
      const w0 = leg(y), w1 = leg(y + 6);
      for (let i = 0; i < 4; i++) {
        const [ax, az] = corners[i];
        const [bx, bz] = corners[(i + 1) % 4];
        beam(new THREE.Vector3(ax * w0, y, az * w0), new THREE.Vector3(bx * w0, y, bz * w0));
        beam(new THREE.Vector3(ax * w0, y, az * w0), new THREE.Vector3(bx * w1, Math.min(y + 6, H), bz * w1), 0.2);
      }
    }
    // Brazos con aisladores y cables
    [32, 40].forEach((y) => {
      beam(new THREE.Vector3(-14, y, 0), new THREE.Vector3(14, y, 0), 0.6);
      [-12, 12].forEach((x) => {
        const ins = new THREE.Mesh(new THREE.CylinderGeometry(0.35, 0.35, 2.4, 10), new THREE.MeshStandardMaterial({ color: 0x4fc3f7, roughness: 0.2 }));
        ins.position.set(x, y - 1.4, 0);
        this.world.add(ins);
        // Cable con catenaria (se cuelga entre torres cada 100 m)
        const pts: THREE.Vector3[] = [];
        for (let z = -500; z <= 500; z += 10) {
          const t = (((z % 100) + 100) % 100) / 100;
          pts.push(new THREE.Vector3(x, y - 2.6 - 4 * Math.sin(Math.PI * t), z));
        }
        const line = new THREE.Line(new THREE.BufferGeometry().setFromPoints(pts), new THREE.LineBasicMaterial({ color: 0x333333 }));
        this.world.add(line);
      });
    });
    // Punto caliente (lo que la inspección busca)
    const hot = new THREE.Mesh(new THREE.SphereGeometry(0.9, 16, 12), new THREE.MeshBasicMaterial({ color: 0xff6d00 }));
    hot.position.set(12, 38.6, 0);
    this.world.add(hot);
  }

  /** Lote de cultivo con plantas individuales (cambian de color al fumigarlas). */
  private buildField(w: number, d: number, color: number, plantSize: number) {
    const soil = new THREE.Mesh(new THREE.PlaneGeometry(w + 10, d + 10), new THREE.MeshStandardMaterial({ color: 0x8d6e63, roughness: 1 }));
    soil.rotation.x = -Math.PI / 2;
    soil.position.y = 0.05;
    soil.receiveShadow = true;
    this.world.add(soil);
    const positions: { x: number; z: number }[] = [];
    for (let z = -d / 2; z <= d / 2; z += 2.5) {
      for (let x = -w / 2; x <= w / 2; x += 2.2) positions.push({ x, z });
    }
    const geo = new THREE.ConeGeometry(plantSize * 0.6, plantSize * 1.6, 6);
    const mesh = new THREE.InstancedMesh(geo, new THREE.MeshStandardMaterial({ color: 0xffffff, roughness: 0.9 }), positions.length);
    const m = new THREE.Matrix4();
    const base = new THREE.Color(color);
    const rnd = seeded(3);
    positions.forEach((p, i) => {
      m.makeTranslation(p.x, plantSize * 0.8, p.z);
      mesh.setMatrixAt(i, m);
      mesh.setColorAt(i, base.clone().offsetHSL(0, 0, (rnd() - 0.5) * 0.08));
    });
    mesh.receiveShadow = true;
    this.world.add(mesh);
    this.crops = mesh;
    this.cropPositions = positions;
  }

  /** Subestación / planta industrial (objetivo de vigilancia). */
  private buildPlant() {
    this.box(60, 12, 30, 0xb0bec5, -30, 6, -10);
    this.box(60, 1, 30, 0x78909c, -30, 12.5, -10);
    this.box(28, 8, 22, 0xcfd8dc, 40, 4, -25);
    for (let i = 0; i < 4; i++) {
      const t = new THREE.Mesh(new THREE.CylinderGeometry(3, 3, 7, 18), new THREE.MeshStandardMaterial({ color: 0x9e9e9e, metalness: 0.5 }));
      t.position.set(20 + i * 10, 3.5, 25);
      t.castShadow = true;
      this.world.add(t);
    }
    // Vehículos en el parqueadero
    const colors = [0xd32f2f, 0x1976d2, 0xfafafa, 0x424242, 0xfbc02d];
    for (let i = 0; i < 7; i++) this.box(4.2, 1.5, 2, colors[i % colors.length], -50 + i * 6, 0.75, 40);
  }

  /** Cerca perimetral alrededor del área a vigilar. */
  private buildFence(b: { minX: number; maxX: number; minZ: number; maxZ: number }) {
    const pad = 12;
    const corners = [
      [b.minX - pad, b.minZ - pad], [b.maxX + pad, b.minZ - pad], [b.maxX + pad, b.maxZ - 30], [b.minX - pad, b.maxZ - 30],
    ];
    const pts = [...corners, corners[0]].map(([x, z]) => new THREE.Vector3(x, 2, z));
    this.world.add(new THREE.Line(new THREE.BufferGeometry().setFromPoints(pts), new THREE.LineBasicMaterial({ color: 0x546e7a })));
    for (let i = 0; i < 4; i++) {
      const [ax, az] = corners[i];
      const [bx, bz] = corners[(i + 1) % 4];
      const len = Math.hypot(bx - ax, bz - az);
      for (let d = 0; d <= len; d += 10) {
        const t = d / len;
        this.box(0.25, 2.4, 0.25, 0x607d8b, ax + (bx - ax) * t, 1.2, az + (bz - az) * t);
      }
    }
  }

  /** Urbanización en construcción (objetivo del levantamiento topográfico). */
  private buildHousing() {
    const rnd = seeded(11);
    const road = new THREE.MeshStandardMaterial({ color: 0xa1887f, roughness: 1 });
    [-60, 0, 60].forEach((z) => {
      const r = new THREE.Mesh(new THREE.PlaneGeometry(280, 9), road);
      r.rotation.x = -Math.PI / 2;
      r.position.set(0, 0.06, z);
      this.world.add(r);
    });
    for (let x = -120; x <= 120; x += 22) {
      for (const z of [-82, -38, 22, 38, 82]) {
        if (rnd() < 0.25) continue;
        const built = rnd() > 0.35;
        this.box(14, built ? 6 : 0.4, 12, built ? 0xfff3e0 : 0xbdbdbd, x, built ? 3 : 0.2, z);
        if (built) {
          const roof = new THREE.Mesh(new THREE.ConeGeometry(10.5, 4, 4), new THREE.MeshStandardMaterial({ color: 0xbf360c, roughness: 0.8 }));
          roof.position.set(x, 8, z);
          roof.rotation.y = Math.PI / 4;
          roof.castShadow = true;
          this.world.add(roof);
        }
      }
    }
  }

  private buildTrees(b: { minX: number; maxX: number; minZ: number; maxZ: number }) {
    const rnd = seeded(42);
    const count = 260;
    const trunks = new THREE.InstancedMesh(new THREE.CylinderGeometry(0.4, 0.6, 4, 6), new THREE.MeshStandardMaterial({ color: 0x6d4c41 }), count);
    const crowns = new THREE.InstancedMesh(new THREE.ConeGeometry(3.5, 9, 8), new THREE.MeshStandardMaterial({ color: 0x33691e, roughness: 0.9 }), count);
    const m = new THREE.Matrix4();
    const margin = 45;
    let i = 0;
    while (i < count) {
      const x = (rnd() - 0.5) * 1100;
      const z = (rnd() - 0.5) * 1100;
      if (x > b.minX - margin && x < b.maxX + margin && z > b.minZ - margin && z < b.maxZ + margin) continue;
      const s = 0.7 + rnd() * 0.8;
      m.compose(new THREE.Vector3(x, 2 * s, z), new THREE.Quaternion(), new THREE.Vector3(s, s, s));
      trunks.setMatrixAt(i, m);
      m.compose(new THREE.Vector3(x, 8 * s, z), new THREE.Quaternion(), new THREE.Vector3(s, s, s));
      crowns.setMatrixAt(i, m);
      i++;
    }
    crowns.castShadow = true;
    this.world.add(trunks, crowns);
  }

  private buildRoute(plan: PlanPoint[]) {
    const pts = plan.map((p) => new THREE.Vector3(p.x, Math.max(p.alt, 0.5), p.z));
    const line = new THREE.Line(new THREE.BufferGeometry().setFromPoints(pts),
      new THREE.LineDashedMaterial({ color: 0xffffff, dashSize: 4, gapSize: 3, transparent: true, opacity: 0.85 }));
    line.computeLineDistances();
    this.routeGroup.add(line);
    plan.forEach((p, i) => {
      if (i === 0 || i === plan.length - 1) return;
      const marker = new THREE.Mesh(new THREE.SphereGeometry(0.9, 12, 10), new THREE.MeshBasicMaterial({ color: ACTION_COLORS[p.action] ?? 0xffffff }));
      marker.position.set(p.x, p.alt, p.z);
      this.routeGroup.add(marker);
      // Línea vertical tenue para ubicar el punto en el suelo
      const pole = new THREE.Line(new THREE.BufferGeometry().setFromPoints([new THREE.Vector3(p.x, 0, p.z), new THREE.Vector3(p.x, p.alt, p.z)]),
        new THREE.LineBasicMaterial({ color: 0xffffff, transparent: true, opacity: 0.18 }));
      this.routeGroup.add(pole);
    });
    // Base de despegue (helipuerto)
    const home = plan[0];
    const pad = new THREE.Mesh(new THREE.CylinderGeometry(4, 4, 0.2, 32), new THREE.MeshStandardMaterial({ color: 0x37474f }));
    pad.position.set(home.x, 0.1, home.z);
    pad.receiveShadow = true;
    this.routeGroup.add(pad);
    const ring = new THREE.Mesh(new THREE.RingGeometry(3.1, 3.6, 32), new THREE.MeshBasicMaterial({ color: 0xffb300, side: THREE.DoubleSide }));
    ring.rotation.x = -Math.PI / 2;
    ring.position.set(home.x, 0.25, home.z);
    this.routeGroup.add(ring);
    const hBar = (w: number, d: number, x: number) => {
      const bar = new THREE.Mesh(new THREE.PlaneGeometry(w, d), new THREE.MeshBasicMaterial({ color: 0xffb300 }));
      bar.rotation.x = -Math.PI / 2;
      bar.position.set(home.x + x, 0.26, home.z);
      this.routeGroup.add(bar);
    };
    hBar(0.5, 3, -0.9); hBar(0.5, 3, 0.9); hBar(1.8, 0.5, 0);
  }

  // ------------------------------------------------------------------ actualización por cuadro

  setCameraMode(mode: CameraMode) {
    this.cameraMode = mode;
    this.controls.enabled = mode === 'libre';
    if (mode === 'libre') this.controls.target.copy(this.drone.position);
  }

  update(s: SimState, realDt: number) {
    this.frame++;
    const airborne = s.phase !== 'EN_TIERRA' && s.phase !== 'COMPLETADA';
    const speed = Math.hypot(s.vel.x, s.vel.z);

    // Drone: posición, rumbo e inclinación según la velocidad
    this.drone.position.set(s.pos.x, s.pos.y + 1.1, s.pos.z);
    this.drone.rotation.y = -s.heading;
    this.drone.rotation.x = THREE.MathUtils.lerp(this.drone.rotation.x, -Math.min(speed, 12) * 0.018, 0.1);
    this.props.forEach((p, i) => { p.rotation.y += (airborne && !s.paused ? 1.2 : airborne ? 0.9 : 0) * (i % 2 ? 1 : -1); });
    const blink = Math.floor(performance.now() / 500) % 2 === 0;
    this.leds.forEach((l, i) => l.color.setHex(airborne && blink ? (i < 2 ? 0x00e676 : 0xff1744) : (i < 2 ? 0x1b5e20 : 0x7f0000)));

    // Recorrido
    const n = Math.min(s.trail.length, 4000);
    for (let i = 0; i < n; i++) {
      const p = s.trail[s.trail.length - n + i];
      this.trailPositions[i * 3] = p.x;
      this.trailPositions[i * 3 + 1] = p.y + 1;
      this.trailPositions[i * 3 + 2] = p.z;
    }
    const tg = this.trail.geometry;
    tg.setDrawRange(0, n);
    (tg.attributes.position as THREE.BufferAttribute).needsUpdate = true;

    // Huellas de las fotografías (cobertura del terreno)
    while (this.footprintsDrawn < s.footprints.length) {
      const f = s.footprints[this.footprintsDrawn++];
      // Área que cubre la foto en el suelo (crece con la altitud), como en software de fotogrametría
      const w = Math.max(6, f.y * 0.75);
      const h = w * 0.66;
      const fill = new THREE.Mesh(new THREE.PlaneGeometry(w, h),
        new THREE.MeshBasicMaterial({ color: 0x42a5f5, transparent: true, opacity: 0.07, depthWrite: false }));
      fill.rotation.x = -Math.PI / 2;
      fill.position.set(f.x, 0.3, f.z);
      const corners = [[-w / 2, -h / 2], [w / 2, -h / 2], [w / 2, h / 2], [-w / 2, h / 2], [-w / 2, -h / 2]]
        .map(([x, z]) => new THREE.Vector3(f.x + x, 0.35, f.z + z));
      const outline = new THREE.Line(new THREE.BufferGeometry().setFromPoints(corners),
        new THREE.LineBasicMaterial({ color: 0x1e88e5, transparent: true, opacity: 0.8 }));
      this.footprintGroup.add(fill, outline);
    }

    this.updateSpray(s, realDt);
    this.updateCamera(s);
    if (this.cameraMode === 'libre') this.controls.update();
    this.renderer.render(this.scene, this.camera);
  }

  private updateSpray(s: SimState, dt: number) {
    const pos = this.spray.geometry.attributes.position as THREE.BufferAttribute;
    const arr = pos.array as Float32Array;
    const count = this.sprayLife.length;
    let emitted = 0;
    for (let i = 0; i < count; i++) {
      if (this.sprayLife[i] > 0) {
        this.sprayLife[i] -= dt;
        this.sprayVel[i * 3 + 1] -= 9.8 * dt * 0.5;
        arr[i * 3] += this.sprayVel[i * 3] * dt;
        arr[i * 3 + 1] += this.sprayVel[i * 3 + 1] * dt;
        arr[i * 3 + 2] += this.sprayVel[i * 3 + 2] * dt;
        if (arr[i * 3 + 1] < 0 || this.sprayLife[i] <= 0) { this.sprayLife[i] = 0; arr[i * 3 + 1] = -999; }
      } else if (s.spraying && !s.paused && emitted < 14) {
        emitted++;
        this.sprayLife[i] = 1.5;
        arr[i * 3] = s.pos.x + (Math.random() - 0.5) * 3;
        arr[i * 3 + 1] = s.pos.y + 0.4;
        arr[i * 3 + 2] = s.pos.z + (Math.random() - 0.5) * 1.5;
        this.sprayVel[i * 3] = (Math.random() - 0.5) * 2.5 + s.vel.x * 0.5;
        this.sprayVel[i * 3 + 1] = -6 - Math.random() * 3;
        this.sprayVel[i * 3 + 2] = (Math.random() - 0.5) * 2.5 + s.vel.z * 0.5;
      }
    }
    pos.needsUpdate = true;

    // Plantas fumigadas cambian de color (mapa de cobertura)
    if (s.spraying && this.crops && this.frame % 3 === 0) {
      const sprayed = new THREE.Color(0x00838f);
      let changed = false;
      this.cropPositions.forEach((p, i) => {
        if (!this.sprayedCrops.has(i) && Math.abs(p.x - s.pos.x) < 6 && Math.abs(p.z - s.pos.z) < 5) {
          this.sprayedCrops.add(i);
          this.crops!.setColorAt(i, sprayed);
          changed = true;
        }
      });
      if (changed && this.crops.instanceColor) this.crops.instanceColor.needsUpdate = true;
    }
  }

  private updateCamera(s: SimState) {
    const target = this.drone.position;
    if (this.cameraMode === 'seguimiento') {
      const back = 15, up = 6;
      const desired = new THREE.Vector3(
        target.x - Math.sin(s.heading) * back,
        target.y + up,
        target.z + Math.cos(s.heading) * back,
      );
      this.camera.position.lerp(desired, 0.05);
      this.camera.lookAt(target);
    } else if (this.cameraMode === 'cenital') {
      this.camera.position.lerp(new THREE.Vector3(target.x, target.y + 170, target.z + 0.1), 0.08);
      this.camera.lookAt(target);
    } else {
      this.controls.target.lerp(target, 0.05);
    }
  }

  private resize() {
    const w = this.container.clientWidth;
    const h = this.container.clientHeight;
    if (w === 0 || h === 0) return;
    this.renderer.setSize(w, h);
    this.camera.aspect = w / h;
    this.camera.updateProjectionMatrix();
  }

  dispose() {
    this.resizeObserver.disconnect();
    this.controls.dispose();
    this.clearGroup(this.world);
    this.clearGroup(this.routeGroup);
    this.clearGroup(this.footprintGroup);
    this.renderer.dispose();
    this.renderer.domElement.remove();
  }
}
