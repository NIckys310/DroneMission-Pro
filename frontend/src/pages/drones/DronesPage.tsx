import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Button, Card, IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tooltip, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import VisibilityOutlinedIcon from '@mui/icons-material/VisibilityOutlined';
import BlockIcon from '@mui/icons-material/Block';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { droneApi } from '../../api/endpoints';
import { errorMessage } from '../../api/client';
import { useFetch } from '../../hooks/useFetch';
import { useNotify } from '../../hooks/useNotify';
import { useAuth } from '../../auth/AuthContext';
import PageHeader from '../../components/PageHeader';
import SearchToolbar from '../../components/SearchToolbar';
import StatusChip from '../../components/StatusChip';
import BatteryIndicator from '../../components/BatteryIndicator';
import EntityFormDialog from '../../components/EntityFormDialog';
import ConfirmDialog from '../../components/ConfirmDialog';
import { EmptyState, ErrorBox, Loading } from '../../components/Feedback';
import { droneStatus, droneType, options } from '../../utils/labels';
import { formatDate } from '../../utils/format';
import type { Drone } from '../../types';
import { droneFields, droneToForm, emptyDrone } from './droneForm';

export default function DronesPage() {
  const navigate = useNavigate();
  const notify = useNotify();
  const { can } = useAuth();
  const [q, setQ] = useState('');
  const [status, setStatus] = useState('');
  const [type, setType] = useState('');
  const { data, loading, error, reload } = useFetch(() => droneApi.list({ q, status, type }), [q, status, type]);
  const [editing, setEditing] = useState<Drone | null | 'new'>(null);
  const [toDeactivate, setToDeactivate] = useState<Drone | null>(null);
  const canManage = can('DRONES_GESTIONAR');

  const save = async (values: Record<string, unknown>) => {
    if (editing === 'new') {
      const created = await droneApi.create(values);
      notify(`Drone ${created.code} creado`);
    } else if (editing) {
      await droneApi.update(editing.id, values);
      notify('Drone actualizado');
    }
    setEditing(null);
    reload();
  };

  const deactivate = async () => {
    try {
      await droneApi.deactivate(toDeactivate!.id);
      notify('Drone desactivado');
      reload();
    } catch (e) {
      notify(errorMessage(e), 'error');
    }
    setToDeactivate(null);
  };

  return (
    <>
      <PageHeader title="Drones" subtitle="Flota de drones, estado técnico y batería"
        actions={canManage && <Button variant="contained" startIcon={<AddIcon />} onClick={() => setEditing('new')}>Nuevo drone</Button>} />
      <SearchToolbar search={q} onSearch={setQ} placeholder="Buscar por código, nombre, modelo o fabricante"
        filters={[
          { name: 'status', label: 'Estado', value: status, onChange: setStatus, options: options(droneStatus) },
          { name: 'type', label: 'Tipo', value: type, onChange: setType, options: options(droneType) },
        ]} />
      {error && <ErrorBox message={error} onRetry={reload} />}
      {loading ? <Loading /> : (
        <Card>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Código</TableCell><TableCell>Drone</TableCell><TableCell>Tipo</TableCell><TableCell>Estado</TableCell>
                  <TableCell>Batería</TableCell><TableCell align="right">Horas</TableCell><TableCell>Próx. mantenimiento</TableCell><TableCell align="right">Acciones</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(data ?? []).map((d) => (
                  <TableRow key={d.id} hover sx={{ opacity: d.active ? 1 : 0.55 }}>
                    <TableCell sx={{ fontWeight: 700 }}>{d.code}</TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight={600}>{d.name}</Typography>
                      <Typography variant="caption" color="text.secondary">{d.manufacturer} {d.model}</Typography>
                    </TableCell>
                    <TableCell>{droneType[d.type]}</TableCell>
                    <TableCell><StatusChip value={d.status} dict={droneStatus} />{!d.active && <Typography variant="caption" display="block">Dado de baja</Typography>}</TableCell>
                    <TableCell><BatteryIndicator level={d.batteryLevel} /></TableCell>
                    <TableCell align="right">{d.flightHours}</TableCell>
                    <TableCell>
                      {formatDate(d.nextMaintenanceOn)}
                      {d.maintenanceDue && <Tooltip title="Mantenimiento vencido"><WarningAmberIcon color="warning" fontSize="small" sx={{ ml: 0.5, verticalAlign: 'middle' }} /></Tooltip>}
                    </TableCell>
                    <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                      <Tooltip title="Ver detalle"><IconButton onClick={() => navigate(`/drones/${d.id}`)}><VisibilityOutlinedIcon /></IconButton></Tooltip>
                      {canManage && <Tooltip title="Editar"><IconButton onClick={() => setEditing(d)}><EditOutlinedIcon /></IconButton></Tooltip>}
                      {canManage && d.active && <Tooltip title="Desactivar"><IconButton color="error" onClick={() => setToDeactivate(d)}><BlockIcon /></IconButton></Tooltip>}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          {data?.length === 0 && <EmptyState text="No hay drones que coincidan con la búsqueda" />}
        </Card>
      )}

      <EntityFormDialog open={editing !== null} title={editing === 'new' ? 'Nuevo drone' : 'Editar drone'} fields={droneFields}
        initialValues={editing && editing !== 'new' ? droneToForm(editing) : emptyDrone} onSubmit={save} onClose={() => setEditing(null)} />
      <ConfirmDialog open={!!toDeactivate} danger title="Desactivar drone" confirmLabel="Desactivar"
        message={`¿Desactivar el drone ${toDeactivate?.code}? Se conserva su historial pero no podrá asignarse a misiones.`}
        onConfirm={deactivate} onClose={() => setToDeactivate(null)} />
    </>
  );
}
