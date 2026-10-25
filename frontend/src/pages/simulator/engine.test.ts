import { describe, expect, it } from 'vitest';
import { createState, injectFailure, returnToHome, step, takeOff, toLocal, type SimConfig } from './engine';

const plan = [
  { x: 0, z: 0, alt: 0, action: 'DESPEGUE' },
  { x: 60, z: 0, alt: 30, action: 'FOTO' },
  { x: 60, z: -60, alt: 30, action: 'FOTO' },
  { x: 0, z: 0, alt: 0, action: 'ATERRIZAJE' },
];
const cfg = (over: Partial<SimConfig> = {}): SimConfig => ({
  plan, cruiseSpeedKmh: 30, drainPerMinute: 2, windKmh: 0, windDirDeg: 0, startBattery: 100, ...over,
});
const run = (s: ReturnType<typeof createState>, c: SimConfig, seconds: number) => {
  for (let t = 0; t < seconds; t += 0.1) step(s, c, 0.1);
};

describe('motor del simulador', () => {
  it('no se mueve hasta despegar', () => {
    const c = cfg(); const s = createState(c);
    run(s, c, 5);
    expect(s.pos.y).toBe(0);
    expect(s.phase).toBe('EN_TIERRA');
  });

  it('despega, recorre la ruta tomando fotos y aterriza en la base', () => {
    const c = cfg(); const s = createState(c);
    takeOff(s);
    run(s, c, 300);
    expect(s.phase).toBe('COMPLETADA');
    expect(s.completedRoute).toBe(true);
    expect(s.photos).toBe(2);
    expect(Math.hypot(s.pos.x, s.pos.z)).toBeLessThan(5);
    expect(s.battery).toBeLessThan(100);
  });

  it('regresa automáticamente a base con batería crítica', () => {
    const c = cfg({ startBattery: 22, drainPerMinute: 20 }); const s = createState(c);
    takeOff(s);
    run(s, c, 20);
    expect(['REGRESANDO', 'ATERRIZANDO', 'EMERGENCIA', 'COMPLETADA']).toContain(s.phase);
    expect(s.events.some((e) => e.text.includes('regreso automático'))).toBe(true);
  });

  it('la pérdida de señal termina en regreso a base', () => {
    const c = cfg(); const s = createState(c);
    takeOff(s); run(s, c, 15);
    injectFailure(s, 'senal'); run(s, c, 6);
    expect(['REGRESANDO', 'ATERRIZANDO', 'COMPLETADA']).toContain(s.phase);
  });

  it('el regreso manual aterriza en la base sin completar la ruta', () => {
    const c = cfg(); const s = createState(c);
    takeOff(s); run(s, c, 15);
    returnToHome(s); run(s, c, 120);
    expect(s.phase).toBe('COMPLETADA');
    expect(s.completedRoute).toBe(false);
  });

  it('convierte coordenadas geográficas a metros locales', () => {
    const local = toLocal([
      { latitude: 3.45, longitude: -76.53, altitudeM: 0, action: 'DESPEGUE' },
      { latitude: 3.45, longitude: -76.529, altitudeM: 50, action: 'FOTO' },
    ]);
    expect(local[1].x).toBeGreaterThan(105);
    expect(local[1].x).toBeLessThan(115);
    expect(local[1].z).toBeCloseTo(0, 5);
  });
});
