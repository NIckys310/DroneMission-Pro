import { Alert, Box, Card, CardContent, CardHeader, Chip, Grid2 as Grid, LinearProgress, Typography } from '@mui/material';
import SensorsIcon from '@mui/icons-material/Sensors';
import { CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { missionApi } from '../../api/endpoints';
import { useFetch } from '../../hooks/useFetch';
import FlightMap from '../../components/FlightMap';
import { Loading } from '../../components/Feedback';
import { formatDuration, formatNumber } from '../../utils/format';
import type { FlightPlan, Mission } from '../../types';
import { AXIS, GRID, SERIES_1, SERIES_2 } from '../home/charts';

function Gauge({ label, value, unit, sub }: { label: string; value: string; unit?: string; sub?: React.ReactNode }) {
  return (
    <Box sx={{ p: 1.5, borderRadius: 2, bgcolor: '#f6f9fc', border: '1px solid', borderColor: 'divider', height: '100%' }}>
      <Typography variant="caption" color="text.secondary" fontWeight={600}>{label}</Typography>
      <Typography variant="h6" sx={{ fontVariantNumeric: 'tabular-nums' }}>{value}<Typography component="span" variant="body2" color="text.secondary"> {unit}</Typography></Typography>
      {sub}
    </Box>
  );
}

/**
 * Seguimiento del vuelo del drone virtual. Mientras la misión está en vuelo,
 * consulta la telemetría cada 2 segundos (polling).
 */
export default function TelemetryPanel({ mission, plan }: { mission: Mission; plan: FlightPlan | null }) {
  const polling = ['EN_VUELO', 'PAUSADA'].includes(mission.status);
  const { data, loading } = useFetch(() => missionApi.telemetry(mission.id), [mission.id, mission.status], polling ? 2000 : undefined);

  if (loading && !data) return <Loading />;
  const points = data?.points ?? [];
  const last = points[points.length - 1];
  const total = data?.totalWaypoints ?? plan?.waypoints.length ?? 0;
  const progress = last && total > 0 ? Math.min(100, Math.round((last.currentWaypoint / total) * 100)) : 0;
  const chartData = points.map((p) => ({ t: formatDuration(p.flightTimeS), altitude: p.altitudeM, battery: p.battery }));

  return (
    <Grid container spacing={2}>
      <Grid size={12}>
        <Alert severity="warning" icon={<SensorsIcon />} sx={{ fontWeight: 600 }}>
          {data?.notice ?? 'DATOS SIMULADOS DE DEMOSTRACIÓN'} El drone virtual recorre el plan de vuelo programado.
        </Alert>
      </Grid>
      <Grid size={{ xs: 12, lg: 7 }}>
        <Card>
          <CardHeader
            title="Mapa de vuelo"
            titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }}
            action={data?.live ? <Chip color="primary" size="small" label="● EN VIVO" /> : data?.routeCompleted ? <Chip color="success" size="small" label="Ruta completada" /> : mission.status === 'PAUSADA' ? <Chip color="warning" size="small" label="Pausada" /> : null}
          />
          <CardContent sx={{ pt: 0 }}>
            <FlightMap waypoints={plan?.waypoints ?? []} track={points} live={!!data?.live} />
            <Box sx={{ mt: 1.5 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                <Typography variant="caption" color="text.secondary">Progreso de la ruta</Typography>
                <Typography variant="caption" fontWeight={600}>{last ? `Punto ${Math.min(last.currentWaypoint, total)} de ${total}` : 'Sin despegar'} · {progress}%</Typography>
              </Box>
              <LinearProgress variant="determinate" value={progress} sx={{ height: 8, borderRadius: 4 }} />
            </Box>
            {data?.routeCompleted && mission.status === 'EN_VUELO' && (
              <Alert severity="success" sx={{ mt: 1.5 }}>El drone completó la ruta y aterrizó. El piloto ya puede finalizar la misión.</Alert>
            )}
          </CardContent>
        </Card>
      </Grid>
      <Grid size={{ xs: 12, lg: 5 }}>
        <Grid container spacing={1.5}>
          <Grid size={6}><Gauge label="Altitud" value={last ? formatNumber(last.altitudeM, 1) : '—'} unit="m" /></Grid>
          <Grid size={6}><Gauge label="Velocidad" value={last ? formatNumber(last.speedKmh, 1) : '—'} unit="km/h" /></Grid>
          <Grid size={6}>
            <Gauge label="Batería" value={last ? String(last.battery) : '—'} unit="%"
              sub={last && <LinearProgress variant="determinate" value={last.battery} color={last.battery < 30 ? 'error' : last.battery < 60 ? 'warning' : 'success'} sx={{ mt: 0.5, height: 5, borderRadius: 3 }} />} />
          </Grid>
          <Grid size={6}><Gauge label="Tiempo de vuelo" value={last ? formatDuration(last.flightTimeS) : '—'} unit="min" /></Grid>
          <Grid size={6}><Gauge label="Distancia recorrida" value={last ? formatNumber(last.distanceM) : '—'} unit="m" /></Grid>
          <Grid size={6}><Gauge label="Temperatura" value={last ? formatNumber(last.temperatureC, 1) : '—'} unit="°C" /></Grid>
          <Grid size={6}><Gauge label="Latitud" value={last ? last.latitude.toFixed(5) : '—'} /></Grid>
          <Grid size={6}><Gauge label="Longitud" value={last ? last.longitude.toFixed(5) : '—'} /></Grid>
        </Grid>
      </Grid>
      <Grid size={12}>
        <Card>
          <CardHeader title="Altitud y batería durante el vuelo" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }}
            subheader={data ? `${data.summary.readings} lecturas · altitud máx. ${data.summary.maxAltitudeM} m · consumo ${data.summary.batteryUsed} %` : undefined} />
          <CardContent sx={{ height: 260 }}>
            {chartData.length === 0 ? (
              <Typography color="text.secondary" variant="body2">Aún no hay lecturas. La telemetría aparece cuando el piloto despega.</Typography>
            ) : (
              <ResponsiveContainer>
                <LineChart data={chartData} margin={{ left: -10, right: 10 }}>
                  <CartesianGrid stroke={GRID} vertical={false} />
                  <XAxis dataKey="t" tick={{ fontSize: 11, fill: AXIS }} minTickGap={30} />
                  <YAxis tick={{ fontSize: 11, fill: AXIS }} domain={[0, 'auto']} />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="altitude" name="Altitud (m)" stroke={SERIES_1} strokeWidth={2} dot={false} isAnimationActive={false} />
                  <Line type="monotone" dataKey="battery" name="Batería (%)" stroke={SERIES_2} strokeWidth={2} dot={false} isAnimationActive={false} />
                </LineChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
}
