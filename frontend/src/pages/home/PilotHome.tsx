import { Link as RouterLink } from 'react-router-dom';
import { Alert, Box, Button, Card, CardContent, Chip, Grid2 as Grid, Stack, Typography } from '@mui/material';
import FlightTakeoffIcon from '@mui/icons-material/FlightTakeoff';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import BadgeOutlinedIcon from '@mui/icons-material/BadgeOutlined';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import { missionApi, pilotApi } from '../../api/endpoints';
import { useFetch } from '../../hooks/useFetch';
import PageHeader from '../../components/PageHeader';
import StatCard from '../../components/StatCard';
import StatusChip from '../../components/StatusChip';
import { EmptyState, ErrorBox, Loading } from '../../components/Feedback';
import { missionStatus, missionType, pilotStatus } from '../../utils/labels';
import { formatDate, formatTime } from '../../utils/format';

const ACTIVE = ['PROGRAMADA', 'EN_PREPARACION', 'EN_VUELO', 'PAUSADA'];

/** Inicio del piloto: su perfil y las misiones que debe volar. */
export default function PilotHome() {
  const profile = useFetch(pilotApi.me, []);
  const missions = useFetch(() => missionApi.list(), [], 15000);

  if (profile.loading || missions.loading) return <Loading />;
  if (profile.error) return <ErrorBox message={profile.error} />;
  const p = profile.data!;
  const list = missions.data ?? [];
  const active = list.filter((m) => ACTIVE.includes(m.status)).sort((a, b) => a.scheduledDate.localeCompare(b.scheduledDate));
  const finished = list.filter((m) => m.status === 'FINALIZADA').length;

  return (
    <>
      <PageHeader title={`Hola, ${p.firstName}`} subtitle="Estas son tus misiones asignadas." />
      {!p.licenseValid && <Alert severity="error" sx={{ mb: 2 }}>Tu licencia está vencida: no puedes iniciar misiones hasta renovarla.</Alert>}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}><StatCard label="Estado" value={<StatusChip value={p.status} dict={pilotStatus} />} icon={<BadgeOutlinedIcon />} /></Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}><StatCard label="Horas de vuelo" value={`${p.flightHours} h`} icon={<AccessTimeIcon />} color="#00838f" /></Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}><StatCard label="Misiones por volar" value={active.length} icon={<FlightTakeoffIcon />} color="#0277bd" /></Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}><StatCard label="Misiones finalizadas" value={finished} icon={<TaskAltIcon />} color="#2e7d32" caption={`Licencia ${p.licenseNumber} · vence ${formatDate(p.licenseExpiresOn)}`} /></Grid>
      </Grid>

      <Typography variant="h6" sx={{ mb: 1.5 }}>Próximas y en curso</Typography>
      {active.length === 0 && <Card><EmptyState text="No tienes misiones pendientes" /></Card>}
      <Grid container spacing={2}>
        {active.map((m) => (
          <Grid key={m.id} size={{ xs: 12, md: 6 }}>
            <Card sx={{ borderLeft: '4px solid', borderLeftColor: m.status === 'EN_VUELO' ? 'primary.main' : 'divider' }}>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', gap: 1 }}>
                  <Box>
                    <Typography variant="caption" color="text.secondary">{m.code} · {missionType[m.type]}</Typography>
                    <Typography variant="h6">{m.name}</Typography>
                  </Box>
                  <StatusChip value={m.status} dict={missionStatus} />
                </Box>
                <Stack direction="row" spacing={1} sx={{ my: 1.5, flexWrap: 'wrap', gap: 1 }}>
                  <Chip size="small" variant="outlined" label={`${formatDate(m.scheduledDate)} · ${formatTime(m.startTime)}`} />
                  <Chip size="small" variant="outlined" label={m.location} />
                  {m.drone && <Chip size="small" variant="outlined" label={`${m.drone.code} · ${m.drone.batteryLevel}%`} />}
                </Stack>
                <Button component={RouterLink} to={`/missions/${m.id}`} variant="contained" startIcon={<FlightTakeoffIcon />}>
                  Abrir centro de control
                </Button>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </>
  );
}
