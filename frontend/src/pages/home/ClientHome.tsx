import { Link as RouterLink } from 'react-router-dom';
import { Button, Card, Grid2 as Grid, Table, TableBody, TableCell, TableHead, TableRow, Typography, Link } from '@mui/material';
import FlightTakeoffIcon from '@mui/icons-material/FlightTakeoff';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import EventAvailableOutlinedIcon from '@mui/icons-material/EventAvailableOutlined';
import PictureAsPdfOutlinedIcon from '@mui/icons-material/PictureAsPdfOutlined';
import { missionApi } from '../../api/endpoints';
import { downloadFile } from '../../api/client';
import { useFetch } from '../../hooks/useFetch';
import { useNotify } from '../../hooks/useNotify';
import PageHeader from '../../components/PageHeader';
import StatCard from '../../components/StatCard';
import StatusChip from '../../components/StatusChip';
import { EmptyState, Loading } from '../../components/Feedback';
import { missionStatus, missionType } from '../../utils/labels';
import { formatDate } from '../../utils/format';
import { useAuth } from '../../auth/AuthContext';

/** Inicio del cliente: estado de sus misiones y descarga de reportes. */
export default function ClientHome() {
  const { user } = useAuth();
  const notify = useNotify();
  const { data, loading } = useFetch(() => missionApi.list(), [], 15000);
  if (loading) return <Loading />;
  const list = data ?? [];
  const inProgress = list.filter((m) => ['EN_PREPARACION', 'EN_VUELO', 'PAUSADA'].includes(m.status)).length;
  const finished = list.filter((m) => m.status === 'FINALIZADA');

  return (
    <>
      <PageHeader title={`Bienvenido(a), ${user?.fullName}`} subtitle="Consulta el estado de tus misiones, sus evidencias y reportes." />
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, sm: 4 }}><StatCard label="Misiones contratadas" value={list.length} icon={<EventAvailableOutlinedIcon />} /></Grid>
        <Grid size={{ xs: 12, sm: 4 }}><StatCard label="En curso" value={inProgress} icon={<FlightTakeoffIcon />} color="#0277bd" /></Grid>
        <Grid size={{ xs: 12, sm: 4 }}><StatCard label="Finalizadas" value={finished.length} icon={<TaskAltIcon />} color="#2e7d32" /></Grid>
      </Grid>
      <Card>
        <Typography variant="subtitle1" fontWeight={700} sx={{ p: 2 }}>Mis misiones</Typography>
        <Table>
          <TableHead><TableRow><TableCell>Misión</TableCell><TableCell>Tipo</TableCell><TableCell>Fecha</TableCell><TableCell>Estado</TableCell><TableCell align="right">Reporte</TableCell></TableRow></TableHead>
          <TableBody>
            {list.map((m) => (
              <TableRow key={m.id} hover>
                <TableCell><Link component={RouterLink} to={`/missions/${m.id}`} fontWeight={600}>{m.code}</Link> · {m.name}</TableCell>
                <TableCell>{missionType[m.type]}</TableCell>
                <TableCell>{formatDate(m.scheduledDate)}</TableCell>
                <TableCell><StatusChip value={m.status} dict={missionStatus} /></TableCell>
                <TableCell align="right">
                  {m.status === 'FINALIZADA' && (
                    <Button size="small" startIcon={<PictureAsPdfOutlinedIcon />}
                      onClick={() => downloadFile(`/reports/missions/${m.id}/pdf`, `reporte-${m.code}.pdf`).catch(() => notify('No se pudo descargar', 'error'))}>
                      PDF
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        {list.length === 0 && <EmptyState text="Aún no tienes misiones" />}
      </Card>
    </>
  );
}
