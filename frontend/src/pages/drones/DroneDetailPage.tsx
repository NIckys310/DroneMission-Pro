import { useState } from 'react';
import { Link as RouterLink, useParams } from 'react-router-dom';
import {
  Button, Card, CardContent, CardHeader, Grid2 as Grid, Link, MenuItem, Table, TableBody, TableCell, TableHead, TableRow, TextField,
  Dialog, DialogActions, DialogContent, DialogTitle, Alert,
} from '@mui/material';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import BatteryChargingFullIcon from '@mui/icons-material/BatteryChargingFull';
import SettingsSuggestOutlinedIcon from '@mui/icons-material/SettingsSuggestOutlined';
import { droneApi } from '../../api/endpoints';
import { errorMessage } from '../../api/client';
import { useFetch } from '../../hooks/useFetch';
import { useNotify } from '../../hooks/useNotify';
import { useAuth } from '../../auth/AuthContext';
import PageHeader from '../../components/PageHeader';
import StatusChip from '../../components/StatusChip';
import BatteryIndicator from '../../components/BatteryIndicator';
import InfoItem from '../../components/InfoItem';
import EntityFormDialog from '../../components/EntityFormDialog';
import { EmptyState, ErrorBox, Loading } from '../../components/Feedback';
import { droneStatus, droneType, maintenanceStatus, maintenanceType, missionStatus } from '../../utils/labels';
import { formatDate, formatMoney } from '../../utils/format';
import { droneFields, droneToForm } from './droneForm';

export default function DroneDetailPage() {
  const id = Number(useParams().id);
  const notify = useNotify();
  const { can } = useAuth();
  const drone = useFetch(() => droneApi.get(id), [id]);
  const history = useFetch(() => droneApi.history(id), [id]);
  const [editOpen, setEditOpen] = useState(false);
  const [statusOpen, setStatusOpen] = useState(false);
  const [newStatus, setNewStatus] = useState('DISPONIBLE');
  const [reason, setReason] = useState('');
  const [statusError, setStatusError] = useState<string | null>(null);

  if (drone.loading) return <Loading />;
  if (drone.error || !drone.data) return <ErrorBox message={drone.error ?? 'No encontrado'} />;
  const d = drone.data;
  const technical = can('DRONES_ESTADO_TECNICO');

  const run = async (action: () => Promise<unknown>, ok: string) => {
    try {
      await action();
      notify(ok);
      drone.reload();
      history.reload();
    } catch (e) {
      notify(errorMessage(e), 'error');
    }
  };

  const changeStatus = async () => {
    try {
      await droneApi.changeStatus(id, newStatus, reason);
      notify('Estado actualizado');
      setStatusOpen(false);
      drone.reload();
    } catch (e) {
      setStatusError(errorMessage(e));
    }
  };

  return (
    <>
      <PageHeader title={`${d.code} · ${d.name}`} subtitle={`${d.manufacturer} ${d.model} · Serie ${d.serialNumber}`}
        breadcrumbs={[{ label: 'Drones', to: '/drones' }, { label: d.code }]}
        actions={<>
          {technical && <Button startIcon={<BatteryChargingFullIcon />} onClick={() => run(() => droneApi.charge(id), 'Batería cargada al 100 %')}>Cargar batería</Button>}
          {technical && <Button startIcon={<SettingsSuggestOutlinedIcon />} onClick={() => { setStatusError(null); setReason(''); setStatusOpen(true); }}>Estado técnico</Button>}
          {can('DRONES_GESTIONAR') && <Button variant="contained" startIcon={<EditOutlinedIcon />} onClick={() => setEditOpen(true)}>Editar</Button>}
        </>} />

      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Estado" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <CardContent sx={{ display: 'grid', gap: 2 }}>
              <InfoItem label="Estado" value={<StatusChip value={d.status} dict={droneStatus} size="medium" />} />
              <InfoItem label="Batería" value={<BatteryIndicator level={d.batteryLevel} width={220} />} />
              <InfoItem label="Horas de vuelo" value={`${d.flightHours} h`} />
              <InfoItem label="Activo" value={d.active ? 'Sí' : 'No (dado de baja)'} />
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 8 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Ficha técnica" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <CardContent>
              <Grid container spacing={2}>
                <Grid size={{ xs: 6, sm: 4 }}><InfoItem label="Tipo" value={droneType[d.type]} /></Grid>
                <Grid size={{ xs: 6, sm: 4 }}><InfoItem label="Capacidad batería" value={`${d.batteryCapacityMah} mAh`} /></Grid>
                <Grid size={{ xs: 6, sm: 4 }}><InfoItem label="Velocidad máxima" value={`${d.maxSpeedKmh} km/h`} /></Grid>
                <Grid size={{ xs: 6, sm: 4 }}><InfoItem label="Autonomía" value={`${d.autonomyMinutes} min`} /></Grid>
                <Grid size={{ xs: 6, sm: 4 }}><InfoItem label="Adquisición" value={formatDate(d.acquiredOn)} /></Grid>
                <Grid size={{ xs: 6, sm: 4 }}><InfoItem label="Último mantenimiento" value={formatDate(d.lastMaintenanceOn)} /></Grid>
                <Grid size={{ xs: 6, sm: 4 }}><InfoItem label="Próximo mantenimiento" value={<>{formatDate(d.nextMaintenanceOn)} {d.maintenanceDue && <StatusChip value="vencido" dict={{ vencido: { label: 'Vencido', color: 'error' } }} />}</>} /></Grid>
                <Grid size={12}><InfoItem label="Descripción" value={d.description} /></Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, lg: 6 }}>
          <Card>
            <CardHeader title="Historial de misiones" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <Table size="small">
              <TableHead><TableRow><TableCell>Misión</TableCell><TableCell>Fecha</TableCell><TableCell>Piloto</TableCell><TableCell>Estado</TableCell></TableRow></TableHead>
              <TableBody>
                {history.data?.missions.map((m) => (
                  <TableRow key={`${m.missionId}-${m.assignedAt}`}>
                    <TableCell>{can('MISIONES_VER') ? <Link component={RouterLink} to={`/missions/${m.missionId}`}>{m.code}</Link> : m.code} · {m.name}</TableCell>
                    <TableCell>{formatDate(m.scheduledDate)}</TableCell>
                    <TableCell>{m.pilotName}</TableCell>
                    <TableCell><StatusChip value={m.status} dict={missionStatus} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            {history.data?.missions.length === 0 && <EmptyState text="Sin misiones" />}
          </Card>
        </Grid>
        <Grid size={{ xs: 12, lg: 6 }}>
          <Card>
            <CardHeader title="Historial de mantenimiento" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <Table size="small">
              <TableHead><TableRow><TableCell>Fecha</TableCell><TableCell>Tipo</TableCell><TableCell>Técnico</TableCell><TableCell align="right">Costo</TableCell><TableCell>Estado</TableCell></TableRow></TableHead>
              <TableBody>
                {history.data?.maintenance.map((m) => (
                  <TableRow key={m.id}>
                    <TableCell>{formatDate(m.date)}</TableCell>
                    <TableCell>{maintenanceType[m.type]}</TableCell>
                    <TableCell>{m.technicianName}</TableCell>
                    <TableCell align="right">{formatMoney(m.cost)}</TableCell>
                    <TableCell><StatusChip value={m.status} dict={maintenanceStatus} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            {history.data?.maintenance.length === 0 && <EmptyState text="Sin mantenimientos" />}
          </Card>
        </Grid>
      </Grid>

      <EntityFormDialog open={editOpen} title="Editar drone" fields={droneFields} initialValues={droneToForm(d)}
        onSubmit={async (v) => { await droneApi.update(id, v); notify('Drone actualizado'); setEditOpen(false); drone.reload(); }}
        onClose={() => setEditOpen(false)} />

      <Dialog open={statusOpen} onClose={() => setStatusOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Cambiar estado técnico</DialogTitle>
        <DialogContent sx={{ display: 'grid', gap: 2, pt: '8px !important' }}>
          {statusError && <Alert severity="error">{statusError}</Alert>}
          <Alert severity="info">"En misión" y "En mantenimiento" los asigna el sistema automáticamente.</Alert>
          <TextField select label="Nuevo estado" value={newStatus} onChange={(e) => setNewStatus(e.target.value)}>
            <MenuItem value="DISPONIBLE">Disponible</MenuItem>
            <MenuItem value="FUERA_DE_SERVICIO">Fuera de servicio</MenuItem>
          </TextField>
          <TextField label="Motivo" value={reason} onChange={(e) => setReason(e.target.value)} multiline minRows={2} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStatusOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={changeStatus}>Guardar</Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
