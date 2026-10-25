import { Card, CardContent, CardHeader, Chip, Grid2 as Grid, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow } from '@mui/material';
import FlightMap from '../../components/FlightMap';
import InfoItem from '../../components/InfoItem';
import { droneType, waypointAction } from '../../utils/labels';
import { formatNumber } from '../../utils/format';
import type { FlightPlan } from '../../types';

/** Plan de vuelo programado con anticipación (generado por la estrategia del tipo de misión). */
export default function FlightPlanPanel({ plan }: { plan: FlightPlan }) {
  return (
    <Grid container spacing={2}>
      <Grid size={{ xs: 12, lg: 5 }}>
        <Card sx={{ mb: 2 }}>
          <CardHeader title="Estrategia de vuelo" subheader="Patrón Strategy: depende del tipo de misión" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
          <CardContent>
            <Grid container spacing={2}>
              <Grid size={12}><InfoItem label="Patrón" value={<Chip label={plan.strategy} color="primary" />} /></Grid>
              <Grid size={6}><InfoItem label="Distancia estimada" value={`${formatNumber(plan.estimatedDistanceM)} m`} /></Grid>
              <Grid size={6}><InfoItem label="Duración estimada" value={`${plan.estimatedDurationMin} min`} /></Grid>
              <Grid size={6}><InfoItem label="Velocidad de crucero" value={`${plan.cruiseSpeedKmh} km/h`} /></Grid>
              <Grid size={6}><InfoItem label="Consumo estimado" value={`${plan.estimatedBatteryUsePercent} % de batería`} /></Grid>
              <Grid size={6}><InfoItem label="Batería mínima" value={`${plan.minimumBatteryPercent} %`} /></Grid>
              <Grid size={6}><InfoItem label="Puntos de ruta" value={plan.waypoints.length} /></Grid>
              <Grid size={12}>
                <InfoItem label="Drones compatibles" value={<Stack direction="row" spacing={0.5} flexWrap="wrap">{plan.compatibleDroneTypes.map((t) => <Chip key={t} size="small" variant="outlined" label={droneType[t]} />)}</Stack>} />
              </Grid>
            </Grid>
          </CardContent>
        </Card>
        <Card>
          <TableContainer sx={{ maxHeight: 300 }}>
            <Table size="small" stickyHeader>
              <TableHead><TableRow><TableCell>#</TableCell><TableCell>Acción</TableCell><TableCell>Latitud</TableCell><TableCell>Longitud</TableCell><TableCell align="right">Alt.</TableCell></TableRow></TableHead>
              <TableBody>
                {plan.waypoints.map((w) => (
                  <TableRow key={w.sequence}>
                    <TableCell>{w.sequence}</TableCell><TableCell>{waypointAction[w.action]}</TableCell>
                    <TableCell>{w.latitude.toFixed(5)}</TableCell><TableCell>{w.longitude.toFixed(5)}</TableCell><TableCell align="right">{w.altitudeM} m</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Card>
      </Grid>
      <Grid size={{ xs: 12, lg: 7 }}>
        <Card>
          <CardHeader title="Ruta programada" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
          <CardContent sx={{ pt: 0 }}><FlightMap waypoints={plan.waypoints} height={440} /></CardContent>
        </Card>
      </Grid>
    </Grid>
  );
}
