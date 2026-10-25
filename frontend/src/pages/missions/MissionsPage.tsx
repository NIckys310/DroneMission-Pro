import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Card, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { clientApi, missionApi } from '../../api/endpoints';
import { useFetch } from '../../hooks/useFetch';
import { useNotify } from '../../hooks/useNotify';
import { useAuth } from '../../auth/AuthContext';
import PageHeader from '../../components/PageHeader';
import SearchToolbar from '../../components/SearchToolbar';
import StatusChip from '../../components/StatusChip';
import EntityFormDialog from '../../components/EntityFormDialog';
import { EmptyState, ErrorBox, Loading } from '../../components/Feedback';
import { missionStatus, missionType, options, priority } from '../../utils/labels';
import { formatDate, formatTime } from '../../utils/format';
import { emptyMission, missionFields } from './missionForm';

export default function MissionsPage() {
  const navigate = useNavigate();
  const notify = useNotify();
  const { can, hasRole } = useAuth();
  const [q, setQ] = useState('');
  const [status, setStatus] = useState('');
  const [type, setType] = useState('');
  const { data, loading, error, reload } = useFetch(() => missionApi.list({ q, status, type }), [q, status, type], 15000);
  const canManage = can('MISIONES_GESTIONAR');
  const clients = useFetch(() => (canManage ? clientApi.list() : Promise.resolve([])), [canManage]);
  const [creating, setCreating] = useState(false);

  const create = async (values: Record<string, unknown>) => {
    const mission = await missionApi.create(values);
    notify(`Misión ${mission.code} creada en borrador con su plan de vuelo`);
    setCreating(false);
    navigate(`/missions/${mission.id}`);
  };

  return (
    <>
      <PageHeader
        title={hasRole('PILOTO', 'CLIENTE') ? 'Mis misiones' : 'Misiones'}
        subtitle="Planificación, asignación y seguimiento de vuelos"
        actions={canManage && <Button variant="contained" startIcon={<AddIcon />} onClick={() => setCreating(true)}>Nueva misión</Button>} />
      <SearchToolbar search={q} onSearch={setQ} placeholder="Buscar por código, nombre, ubicación o cliente"
        filters={[
          { name: 'status', label: 'Estado', value: status, onChange: setStatus, options: options(missionStatus) },
          { name: 'type', label: 'Tipo', value: type, onChange: setType, options: options(missionType) },
        ]} />
      {error && <ErrorBox message={error} onRetry={reload} />}
      {loading ? <Loading /> : (
        <Card>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Código</TableCell><TableCell>Misión</TableCell><TableCell>Cliente</TableCell><TableCell>Fecha y hora</TableCell>
                  <TableCell>Drone / Piloto</TableCell><TableCell>Prioridad</TableCell><TableCell>Estado</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(data ?? []).map((m) => (
                  <TableRow key={m.id} hover sx={{ cursor: 'pointer' }} onClick={() => navigate(`/missions/${m.id}`)}>
                    <TableCell sx={{ fontWeight: 700, color: 'primary.main' }}>{m.code}</TableCell>
                    <TableCell><Typography variant="body2" fontWeight={600}>{m.name}</Typography><Typography variant="caption" color="text.secondary">{missionType[m.type]} · {m.location}</Typography></TableCell>
                    <TableCell>{m.client.name}</TableCell>
                    <TableCell>{formatDate(m.scheduledDate)}<Typography variant="caption" display="block" color="text.secondary">{formatTime(m.startTime)} – {formatTime(m.estimatedEndTime)}</Typography></TableCell>
                    <TableCell>{m.drone ? <><Typography variant="body2">{m.drone.code}</Typography><Typography variant="caption" color="text.secondary">{m.pilot?.fullName}</Typography></> : <Typography variant="caption" color="text.secondary">Sin asignar</Typography>}</TableCell>
                    <TableCell><StatusChip value={m.priority} dict={priority} variant="outlined" /></TableCell>
                    <TableCell><StatusChip value={m.status} dict={missionStatus} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          {data?.length === 0 && <EmptyState text="No hay misiones que coincidan" />}
        </Card>
      )}
      <EntityFormDialog open={creating} title="Nueva misión" fields={missionFields(clients.data ?? [])} initialValues={emptyMission}
        submitLabel="Crear misión" onSubmit={create} onClose={() => setCreating(false)} />
    </>
  );
}
