import { Box, Typography } from '@mui/material';
import type { TelemetryPoint, Waypoint } from '../types';

interface Props {
  waypoints: Waypoint[];
  track?: TelemetryPoint[];
  live?: boolean;
  height?: number;
}

const W = 640;
const PAD = 36;

/**
 * Mapa vectorial (SVG) del plan de vuelo y del recorrido del drone virtual.
 * No depende de internet: proyecta latitud/longitud a coordenadas de pantalla.
 *  - Línea punteada: ruta programada.  - Línea cian: recorrido real (simulado).
 */
export default function FlightMap({ waypoints, track = [], live = false, height = 380 }: Props) {
  if (waypoints.length === 0) {
    return <Typography color="text.secondary">La misión no tiene plan de vuelo.</Typography>;
  }
  const all = [...waypoints.map((w) => [w.latitude, w.longitude]), ...track.map((t) => [t.latitude, t.longitude])];
  const lats = all.map((p) => p[0]);
  const lons = all.map((p) => p[1]);
  const minLat = Math.min(...lats), maxLat = Math.max(...lats);
  const minLon = Math.min(...lons), maxLon = Math.max(...lons);
  const cosLat = Math.cos(((minLat + maxLat) / 2) * Math.PI / 180);
  const spanX = Math.max((maxLon - minLon) * cosLat, 1e-6);
  const spanY = Math.max(maxLat - minLat, 1e-6);
  const H = height;
  const scale = Math.min((W - 2 * PAD) / spanX, (H - 2 * PAD) / spanY);
  const offX = (W - spanX * scale) / 2;
  const offY = (H - spanY * scale) / 2;
  const project = (lat: number, lon: number) => [offX + (lon - minLon) * cosLat * scale, H - (offY + (lat - minLat) * scale)];

  const planned = waypoints.map((w) => project(w.latitude, w.longitude).join(',')).join(' ');
  const flown = track.map((t) => project(t.latitude, t.longitude).join(',')).join(' ');
  const current = track.length > 0 ? project(track[track.length - 1].latitude, track[track.length - 1].longitude) : null;
  const home = project(waypoints[0].latitude, waypoints[0].longitude);
  const nextIndex = track.length > 0 ? track[track.length - 1].currentWaypoint : 0;

  // Barra de escala: metros por píxel
  const metersPerDegree = 111_320;
  const metersPerPixel = metersPerDegree / scale;
  const scaleMeters = [50, 100, 200, 500, 1000].find((m) => m / metersPerPixel > 60) ?? 1000;

  return (
    <Box sx={{ position: 'relative', borderRadius: 2, overflow: 'hidden', border: '1px solid', borderColor: 'divider', bgcolor: '#0b2238' }}>
      <svg viewBox={`0 0 ${W} ${H}`} width="100%" style={{ display: 'block' }} role="img" aria-label="Mapa del plan de vuelo">
        <defs>
          <pattern id="grid" width="32" height="32" patternUnits="userSpaceOnUse">
            <path d="M 32 0 L 0 0 0 32" fill="none" stroke="#17395a" strokeWidth="1" />
          </pattern>
        </defs>
        <rect width={W} height={H} fill="url(#grid)" />
        <polyline points={planned} fill="none" stroke="#7fa6c9" strokeWidth="2" strokeDasharray="6 5" />
        {waypoints.map((w, i) => {
          const [x, y] = project(w.latitude, w.longitude);
          const reached = i < nextIndex;
          return (
            <g key={w.sequence}>
              <circle cx={x} cy={y} r={4.5} fill={reached ? '#26c6da' : '#0b2238'} stroke={reached ? '#26c6da' : '#7fa6c9'} strokeWidth="1.5" />
              {i > 0 && i < waypoints.length - 1 && (
                <text x={x + 7} y={y - 6} fontSize="10" fill="#9bb7d1">{w.sequence}</text>
              )}
            </g>
          );
        })}
        <g>
          <circle cx={home[0]} cy={home[1]} r={11} fill="#ffb300" />
          <text x={home[0]} y={home[1] + 4} fontSize="11" fontWeight="700" textAnchor="middle" fill="#0b2238">H</text>
        </g>
        {flown && <polyline points={flown} fill="none" stroke="#26c6da" strokeWidth="3" strokeLinejoin="round" />}
        {current && (
          <g transform={`translate(${current[0]},${current[1]})`}>
            {live && (
              <circle r="10" fill="none" stroke="#26c6da" strokeWidth="2">
                <animate attributeName="r" from="8" to="22" dur="1.4s" repeatCount="indefinite" />
                <animate attributeName="opacity" from="0.9" to="0" dur="1.4s" repeatCount="indefinite" />
              </circle>
            )}
            <g stroke="#ffffff" strokeWidth="2.2"><line x1="-7" y1="-7" x2="7" y2="7" /><line x1="7" y1="-7" x2="-7" y2="7" /></g>
            {[[-7, -7], [7, -7], [-7, 7], [7, 7]].map(([x, y]) => <circle key={`${x}${y}`} cx={x} cy={y} r="3.4" fill="#26c6da" />)}
            <rect x="-3" y="-3" width="6" height="6" rx="1.5" fill="#ffffff" />
          </g>
        )}
        <g transform={`translate(${PAD - 20}, ${H - 18})`}>
          <line x1="0" y1="0" x2={scaleMeters / metersPerPixel} y2="0" stroke="#cfe0f0" strokeWidth="2" />
          <text x="0" y="-5" fontSize="10" fill="#cfe0f0">{scaleMeters >= 1000 ? `${scaleMeters / 1000} km` : `${scaleMeters} m`}</text>
        </g>
      </svg>
      <Box sx={{ position: 'absolute', top: 8, right: 10, display: 'flex', gap: 1.5, fontSize: 11, color: '#cfe0f0' }}>
        <span>┄ Ruta programada</span><span style={{ color: '#26c6da' }}>━ Recorrido</span><span style={{ color: '#ffb300' }}>● Base</span>
      </Box>
    </Box>
  );
}
