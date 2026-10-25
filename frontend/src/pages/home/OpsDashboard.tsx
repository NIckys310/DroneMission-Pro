import { Link as RouterLink } from 'react-router-dom';
import {
  Alert, Box, Card, CardContent, CardHeader, Grid2 as Grid, Link, List, ListItem, ListItemText, Stack, Table, TableBody,
  TableCell, TableHead, TableRow, Typography,
} from '@mui/material';
import PrecisionManufacturingOutlinedIcon from '@mui/icons-material/PrecisionManufacturingOutlined';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import FlightIcon from '@mui/icons-material/Flight';
import BuildOutlinedIcon from '@mui/icons-material/BuildOutlined';
import BadgeOutlinedIcon from '@mui/icons-material/BadgeOutlined';
import EventAvailableOutlinedIcon from '@mui/icons-material/EventAvailableOutlined';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import BlockIcon from '@mui/icons-material/Block';
import {
  Bar, BarChart, CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts';
import dayjs from 'dayjs';
import { dashboardApi } from '../../api/endpoints';
import { useFetch } from '../../hooks/useFetch';
import { useAuth } from '../../auth/AuthContext';
import PageHeader from '../../components/PageHeader';
import StatCard from '../../components/StatCard';
import StatusChip from '../../components/StatusChip';
import { EmptyState, ErrorBox, Loading } from '../../components/Feedback';
import { incidentType, maintenanceStatus, maintenanceType, missionStatus, missionType, severity } from '../../utils/labels';
import { formatDate, formatTime, fromNow } from '../../utils/format';
import { AXIS, GRID, SERIES_1, SERIES_2 } from './charts';

/** Dashboard central de operaciones (administrador, operador y técnico). */
export default function OpsDashboard() {
  const { data, loading, error, reload } = useFetch(dashboardApi.get, [], 15000);
  const { can } = useAuth();
  const canSeeMissions = can('MISIONES_VER');

  if (loading) return <Loading />;
  if (error || !data) return <ErrorBox message={error ?? 'Sin datos'} onRetry={reload} />;

  const s = data.missionsByStatus;
  const statusData = Object.entries(s).map(([k, v]) => ({ name: missionStatus[k].label, value: v }));
  const typeData = data.missionsByType.map((t) => ({ name: missionType[t.label], value: t.count }));
  const dayData = data.missionsPerDay.map((d) => ({ ...d, label: dayjs(d.date).format('DD/MM') }));
  const hoursData = data.flightHoursByDrone.map((h) => ({ name: h.drone, value: Number(h.hours) }));
  const missionLink = (id: number, code: string) =>
    canSeeMissions ? <Link component={RouterLink} to={`/missions/${id}`} fontWeight={600}>{code}</Link> : <b>{code}</b>;

  return (
    <>
      <PageHeader title="Centro de operaciones" subtitle={`Resumen al ${dayjs().format('DD [de] MMMM YYYY, HH:mm')} · se actualiza automáticamente`} />

      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}><StatCard label="Drones en flota" value={data.drones.total} icon={<PrecisionManufacturingOutlinedIcon />} caption={`${data.drones.totalFlightHours} h de vuelo acumuladas`} /></Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}><StatCard label="Drones disponibles" value={data.drones.available} icon={<CheckCircleOutlineIcon />} color="#2e7d32" caption={`${data.drones.outOfService} fuera de servicio`} /></Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}><StatCard label="Drones en misión" value={data.drones.inMission} icon={<FlightIcon />} color="#0277bd" /></Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}><StatCard label="En mantenimiento" value={data.drones.inMaintenance} icon={<BuildOutlinedIcon />} color="#ed8b00" caption={`${data.pendingMaintenance.length} mantenimientos pendientes`} /></Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}><StatCard label="Pilotos disponibles" value={`${data.pilots.available} / ${data.pilots.total}`} icon={<BadgeOutlinedIcon />} color="#00838f" caption={`${data.pilots.expiredLicenses} con licencia vencida`} /></Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}><StatCard label="Misiones programadas" value={s.PROGRAMADA + s.PENDIENTE} icon={<EventAvailableOutlinedIcon />} color="#5e35b1" caption={`${s.PENDIENTE} pendientes de asignación`} /></Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}><StatCard label="Misiones en curso" value={s.EN_PREPARACION + s.EN_VUELO + s.PAUSADA} icon={<FlightIcon />} color="#0e5a9c" caption={`${s.EN_VUELO} en vuelo · ${s.PAUSADA} pausadas`} /></Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}><StatCard label="Completadas / canceladas" value={`${s.FINALIZADA} / ${s.CANCELADA}`} icon={s.CANCELADA > s.FINALIZADA ? <BlockIcon /> : <TaskAltIcon />} color="#2e7d32" /></Grid>
      </Grid>

      {data.alerts.length > 0 && (
        <Card sx={{ mb: 2 }}>
          <CardHeader title="Alertas" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} sx={{ pb: 0 }} />
          <CardContent>
            <Stack spacing={1}>
              {data.alerts.map((a) => (
                <Alert key={a.message} severity={a.level} variant="outlined" action={<Link component={RouterLink} to={a.link} variant="body2">Ver</Link>}>{a.message}</Alert>
              ))}
            </Stack>
          </CardContent>
        </Card>
      )}

      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid size={{ xs: 12, lg: 7 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Misiones por día" subheader="Últimos 7 días y próximos 7" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <CardContent sx={{ height: 280 }}>
              <ResponsiveContainer>
                <LineChart data={dayData} margin={{ left: -20, right: 10 }}>
                  <CartesianGrid stroke={GRID} vertical={false} />
                  <XAxis dataKey="label" tick={{ fontSize: 11, fill: AXIS }} />
                  <YAxis allowDecimals={false} tick={{ fontSize: 11, fill: AXIS }} />
                  <Tooltip />
                  <Legend />
                  <Line type="linear" dataKey="scheduled" name="Programadas" stroke={SERIES_1} strokeWidth={2} dot={{ r: 4 }} />
                  <Line type="linear" dataKey="finished" name="Finalizadas" stroke={SERIES_2} strokeWidth={2} dot={{ r: 4 }} />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, lg: 5 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Misiones por estado" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <CardContent sx={{ height: 280 }}>
              <ResponsiveContainer>
                <BarChart data={statusData} layout="vertical" margin={{ left: 30, right: 16 }}>
                  <CartesianGrid stroke={GRID} horizontal={false} />
                  <XAxis type="number" allowDecimals={false} tick={{ fontSize: 11, fill: AXIS }} />
                  <YAxis type="category" dataKey="name" width={90} tick={{ fontSize: 11, fill: AXIS }} />
                  <Tooltip cursor={{ fill: 'rgba(42,120,214,0.06)' }} />
                  <Bar dataKey="value" name="Misiones" fill={SERIES_1} radius={[0, 4, 4, 0]} barSize={14} />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Misiones por tipo" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <CardContent sx={{ height: 250 }}>
              <ResponsiveContainer>
                <BarChart data={typeData} margin={{ left: -20 }}>
                  <CartesianGrid stroke={GRID} vertical={false} />
                  <XAxis dataKey="name" tick={{ fontSize: 11, fill: AXIS }} />
                  <YAxis allowDecimals={false} tick={{ fontSize: 11, fill: AXIS }} />
                  <Tooltip cursor={{ fill: 'rgba(42,120,214,0.06)' }} />
                  <Bar dataKey="value" name="Misiones" fill={SERIES_1} radius={[4, 4, 0, 0]} barSize={26} />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Horas de vuelo por drone" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <CardContent sx={{ height: 250 }}>
              <ResponsiveContainer>
                <BarChart data={hoursData} margin={{ left: -10 }}>
                  <CartesianGrid stroke={GRID} vertical={false} />
                  <XAxis dataKey="name" tick={{ fontSize: 11, fill: AXIS }} />
                  <YAxis tick={{ fontSize: 11, fill: AXIS }} unit=" h" />
                  <Tooltip cursor={{ fill: 'rgba(42,120,214,0.06)' }} formatter={(v: number) => [`${v} h`, 'Horas de vuelo']} />
                  <Bar dataKey="value" name="Horas" fill={SERIES_1} radius={[4, 4, 0, 0]} barSize={22} />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={2}>
        <Grid size={{ xs: 12, lg: 6 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Misiones en curso y próximas" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <Table size="small">
              <TableHead><TableRow><TableCell>Misión</TableCell><TableCell>Fecha</TableCell><TableCell>Recursos</TableCell><TableCell>Estado</TableCell></TableRow></TableHead>
              <TableBody>
                {[...data.activeMissions, ...data.upcomingMissions].map((m) => (
                  <TableRow key={m.id} hover>
                    <TableCell>{missionLink(m.id, m.code)}<Typography variant="caption" display="block" color="text.secondary">{m.name}</Typography></TableCell>
                    <TableCell>{formatDate(m.date)} · {formatTime(m.startTime)}</TableCell>
                    <TableCell><Typography variant="body2">{m.droneCode ?? '—'}</Typography><Typography variant="caption" color="text.secondary">{m.pilotName ?? 'Sin piloto'}</Typography></TableCell>
                    <TableCell><StatusChip value={m.status} dict={missionStatus} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            {data.activeMissions.length + data.upcomingMissions.length === 0 && <EmptyState text="No hay misiones activas ni próximas" />}
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 6, lg: 3 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Incidentes recientes" subheader={`${data.openIncidents} sin resolver`} titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <List dense>
              {data.recentIncidents.map((i) => (
                <ListItem key={i.id} secondaryAction={<StatusChip value={i.severity} dict={severity} />}>
                  <ListItemText primary={`${incidentType[i.type]} · ${i.missionCode}`} secondary={fromNow(i.date)} />
                </ListItem>
              ))}
            </List>
            {data.recentIncidents.length === 0 && <EmptyState text="Sin incidentes" />}
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 6, lg: 3 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Mantenimientos pendientes" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <List dense>
              {data.pendingMaintenance.map((m) => (
                <ListItem key={m.id} secondaryAction={<StatusChip value={m.status} dict={maintenanceStatus} />}>
                  <ListItemText primary={`${m.droneCode} · ${maintenanceType[m.type]}`} secondary={formatDate(m.date)} />
                </ListItem>
              ))}
            </List>
            {data.pendingMaintenance.length === 0 && <EmptyState text="Sin mantenimientos pendientes" />}
          </Card>
        </Grid>
      </Grid>
      <Box sx={{ height: 8 }} />
    </>
  );
}
