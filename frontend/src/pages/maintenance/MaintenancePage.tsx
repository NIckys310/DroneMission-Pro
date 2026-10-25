import { useState } from 'react';
import { Button, Card, IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tooltip, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import CancelOutlinedIcon from '@mui/icons-material/CancelOutlined';
import { droneApi, maintenanceApi } from '../../api/endpoints';
import { errorMessage } from '../../api/client';
import { useFetch } from '../../hooks/useFetch';
import { useNotify } from '../../hooks/useNotify';
import { useAuth } from '../../auth/AuthContext';
import PageHeader from '../../components/PageHeader';
import SearchToolbar from '../../components/SearchToolbar';
import StatusChip from '../../components/StatusChip';
import EntityFormDialog from '../../components/EntityFormDialog';
import ConfirmDialog from '../../components/ConfirmDialog';
import { EmptyState, ErrorBox, Loading } from '../../components/Feedback';
import { droneStatus, maintenanceStatus, maintenanceType, options } from '../../utils/labels';
import { formatDate, formatMoney, today } from '../../utils/format';
import type { FieldConfig } from '../../utils/validation';
import type { Drone, Maintenance } from '../../types';

const fields = (drones: Drone[], isNew: boolean): FieldConfig[] => [
  { name: 'droneId', label: 'Drone', type: 'select', required: true, cols: 6, options: drones.filter((d) => d.active).map((d) => ({ value: d.id, label: `${d.code} · ${d.name}` })) },
  { name: 'type', label: 'Tipo', type: 'select', required: true, options: options(maintenanceType), cols: 6 },
  { name: 'date', label: 'Fecha', type: 'date', required: true, cols: 4 },
  { name: 'cost', label: 'Costo (COP)', type: 'number', required: true, min: 0, cols: 4 },
  {
    name: 'nextDate', label: 'Próximo mantenimiento', type: 'date', cols: 4,
    validate: (v, all) => (v && String(v) <= String(all.date) ? 'Debe ser posterior a la fecha' : null),
  },
  { name: 'description', label: 'Descripción', type: 'textarea', required: true, maxLength: 1000, cols: 12 },
  { name: 'observations', label: 'Observaciones', type: 'textarea', maxLength: 1000, cols: 12 },
  ...(isNew ? [{ name: 'startNow', label: 'Iniciar ahora (el drone pasa a EN MANTENIMIENTO)', type: 'checkbox' as const, cols: 12 }] : []),
];

const completeFields: FieldConfig[] = [
  { name: 'finalCost', label: 'Costo final (COP)', type: 'number', min: 0 },
  { name: 'nextDate', label: 'Próximo mantenimiento', type: 'date', validate: (v) => (v && String(v) <= today() ? 'Debe ser una fecha futura' : null) },
  { name: 'observations', label: 'Observaciones / reparaciones realizadas', type: 'textarea', cols: 12, maxLength: 1000 },
];

export default function MaintenancePage() {
  const notify = useNotify();
  const { can } = useAuth();
  const [status, setStatus] = useState('');
  const [type, setType] = useState('');
  const { data, loading, error, reload } = useFetch(() => maintenanceApi.list({ status, type }), [status, type]);
  const drones = useFetch(() => droneApi.list(), []);
  const [editing, setEditing] = useState<Maintenance | null | 'new'>(null);
  const [completing, setCompleting] = useState<Maintenance | null>(null);
  const [cancelling, setCancelling] = useState<Maintenance | null>(null);
  const canManage = can('MANTENIMIENTO_GESTIONAR');

  const act = async (fn: () => Promise<unknown>, ok: string) => {
    try { await fn(); notify(ok); reload(); drones.reload(true); } catch (e) { notify(errorMessage(e), 'error'); }
  };

  return (
    <>
      <PageHeader title="Mantenimiento" subtitle="Preventivo, correctivo, calibraciones y reparaciones de la flota"
        actions={canManage && <Button variant="contained" startIcon={<AddIcon />} onClick={() => setEditing('new')}>Registrar mantenimiento</Button>} />
      <SearchToolbar filters={[
        { name: 'status', label: 'Estado', value: status, onChange: setStatus, options: options(maintenanceStatus) },
        { name: 'type', label: 'Tipo', value: type, onChange: setType, options: options(maintenanceType) },
      ]} />
      {error && <ErrorBox message={error} onRetry={reload} />}
      {loading ? <Loading /> : (
        <Card>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow><TableCell>Fecha</TableCell><TableCell>Drone</TableCell><TableCell>Tipo</TableCell><TableCell>Descripción</TableCell><TableCell>Técnico</TableCell><TableCell align="right">Costo</TableCell><TableCell>Próximo</TableCell><TableCell>Estado</TableCell><TableCell align="right">Acciones</TableCell></TableRow>
              </TableHead>
              <TableBody>
                {(data ?? []).map((m) => (
                  <TableRow key={m.id} hover>
                    <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatDate(m.date)}</TableCell>
                    <TableCell><Typography variant="body2" fontWeight={600}>{m.droneCode}</Typography><StatusChip value={m.droneStatus} dict={droneStatus} variant="outlined" /></TableCell>
                    <TableCell>{maintenanceType[m.type]}</TableCell>
                    <TableCell sx={{ maxWidth: 280 }}>{m.description}{m.observations && <Typography variant="caption" display="block" color="text.secondary">{m.observations}</Typography>}</TableCell>
                    <TableCell>{m.technicianName}</TableCell>
                    <TableCell align="right">{formatMoney(m.cost)}</TableCell>
                    <TableCell>{formatDate(m.nextDate)}</TableCell>
                    <TableCell><StatusChip value={m.status} dict={maintenanceStatus} /></TableCell>
                    <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                      {canManage && m.status === 'PROGRAMADO' && <Tooltip title="Iniciar (drone a mantenimiento)"><IconButton color="warning" onClick={() => act(() => maintenanceApi.start(m.id), `Mantenimiento iniciado: ${m.droneCode} quedó EN MANTENIMIENTO`)}><PlayArrowIcon /></IconButton></Tooltip>}
                      {canManage && m.status === 'EN_PROCESO' && <Tooltip title="Completar (drone disponible)"><IconButton color="success" onClick={() => setCompleting(m)}><TaskAltIcon /></IconButton></Tooltip>}
                      {canManage && ['PROGRAMADO', 'EN_PROCESO'].includes(m.status) && <Tooltip title="Editar"><IconButton onClick={() => setEditing(m)}><EditOutlinedIcon /></IconButton></Tooltip>}
                      {canManage && ['PROGRAMADO', 'EN_PROCESO'].includes(m.status) && <Tooltip title="Cancelar"><IconButton color="error" onClick={() => setCancelling(m)}><CancelOutlinedIcon /></IconButton></Tooltip>}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          {data?.length === 0 && <EmptyState text="No hay mantenimientos" />}
        </Card>
      )}
      <EntityFormDialog open={editing !== null} title={editing === 'new' ? 'Registrar mantenimiento' : 'Editar mantenimiento'}
        fields={fields(drones.data ?? [], editing === 'new')}
        initialValues={editing && editing !== 'new' ? { ...editing } : { type: 'PREVENTIVO', date: today(), cost: 0, startNow: false }}
        onClose={() => setEditing(null)}
        onSubmit={async (v) => {
          if (editing === 'new') await maintenanceApi.create(v); else if (editing) await maintenanceApi.update(editing.id, v);
          notify('Mantenimiento guardado');
          setEditing(null);
          reload();
          drones.reload(true);
        }} />
      <EntityFormDialog open={!!completing} title={`Completar mantenimiento de ${completing?.droneCode}`} fields={completeFields} maxWidth="sm"
        initialValues={{ finalCost: completing?.cost, nextDate: completing?.nextDate }} submitLabel="Completar"
        onClose={() => setCompleting(null)}
        onSubmit={async (v) => { await maintenanceApi.complete(completing!.id, v); notify(`Mantenimiento completado: ${completing?.droneCode} está DISPONIBLE`); setCompleting(null); reload(); }} />
      <ConfirmDialog open={!!cancelling} danger title="Cancelar mantenimiento" confirmLabel="Cancelar mantenimiento"
        message="Si estaba en proceso, el drone vuelve a estar disponible." onClose={() => setCancelling(null)}
        onConfirm={async () => { await act(() => maintenanceApi.cancel(cancelling!.id), 'Mantenimiento cancelado'); setCancelling(null); }} />
    </>
  );
}
