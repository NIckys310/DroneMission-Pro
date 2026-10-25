import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Card, Chip, IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tooltip, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import VisibilityOutlinedIcon from '@mui/icons-material/VisibilityOutlined';
import BlockIcon from '@mui/icons-material/Block';
import { pilotApi } from '../../api/endpoints';
import { errorMessage } from '../../api/client';
import { useFetch } from '../../hooks/useFetch';
import { useNotify } from '../../hooks/useNotify';
import { useAuth } from '../../auth/AuthContext';
import PageHeader from '../../components/PageHeader';
import SearchToolbar from '../../components/SearchToolbar';
import StatusChip from '../../components/StatusChip';
import ConfirmDialog from '../../components/ConfirmDialog';
import { EmptyState, ErrorBox, Loading } from '../../components/Feedback';
import { options, pilotStatus } from '../../utils/labels';
import { formatDate } from '../../utils/format';
import type { Pilot } from '../../types';
import PilotFormDialog from './PilotFormDialog';

export default function PilotsPage() {
  const navigate = useNavigate();
  const notify = useNotify();
  const { can } = useAuth();
  const [q, setQ] = useState('');
  const [status, setStatus] = useState('');
  const { data, loading, error, reload } = useFetch(() => pilotApi.list({ q, status }), [q, status]);
  const [editing, setEditing] = useState<Pilot | null | 'new'>(null);
  const [toDeactivate, setToDeactivate] = useState<Pilot | null>(null);
  const canManage = can('PILOTOS_GESTIONAR');

  const save = async (values: Record<string, unknown>) => {
    if (editing === 'new') await pilotApi.create(values);
    else if (editing) await pilotApi.update(editing.id, values);
    notify(editing === 'new' ? 'Piloto creado' : 'Piloto actualizado');
    setEditing(null);
    reload();
  };

  return (
    <>
      <PageHeader title="Pilotos" subtitle="Pilotos certificados, licencias y disponibilidad"
        actions={canManage && <Button variant="contained" startIcon={<AddIcon />} onClick={() => setEditing('new')}>Nuevo piloto</Button>} />
      <SearchToolbar search={q} onSearch={setQ} placeholder="Buscar por nombre, documento o licencia"
        filters={[{ name: 'status', label: 'Estado', value: status, onChange: setStatus, options: options(pilotStatus) }]} />
      {error && <ErrorBox message={error} onRetry={reload} />}
      {loading ? <Loading /> : (
        <Card>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Piloto</TableCell><TableCell>Licencia</TableCell><TableCell>Vencimiento</TableCell><TableCell>Estado</TableCell>
                  <TableCell align="right">Horas</TableCell><TableCell align="right">Misiones</TableCell><TableCell>Certificaciones</TableCell><TableCell align="right">Acciones</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(data ?? []).map((p) => (
                  <TableRow key={p.id} hover sx={{ opacity: p.active ? 1 : 0.55 }}>
                    <TableCell><Typography variant="body2" fontWeight={600}>{p.fullName}</Typography><Typography variant="caption" color="text.secondary">{p.email}</Typography></TableCell>
                    <TableCell>{p.licenseNumber}</TableCell>
                    <TableCell>
                      {formatDate(p.licenseExpiresOn)}
                      {!p.licenseValid && <Chip size="small" color="error" label="Vencida" sx={{ ml: 1 }} />}
                    </TableCell>
                    <TableCell><StatusChip value={p.status} dict={pilotStatus} /></TableCell>
                    <TableCell align="right">{p.flightHours}</TableCell>
                    <TableCell align="right">{p.missionCount}</TableCell>
                    <TableCell>{p.certifications.length}</TableCell>
                    <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                      <Tooltip title="Ver detalle"><IconButton onClick={() => navigate(`/pilots/${p.id}`)}><VisibilityOutlinedIcon /></IconButton></Tooltip>
                      {canManage && <Tooltip title="Editar"><IconButton onClick={() => setEditing(p)}><EditOutlinedIcon /></IconButton></Tooltip>}
                      {canManage && p.active && <Tooltip title="Desactivar"><IconButton color="error" onClick={() => setToDeactivate(p)}><BlockIcon /></IconButton></Tooltip>}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          {data?.length === 0 && <EmptyState text="No hay pilotos que coincidan" />}
        </Card>
      )}
      <PilotFormDialog open={editing !== null} pilot={editing === 'new' ? null : editing} onSubmit={save} onClose={() => setEditing(null)} />
      <ConfirmDialog open={!!toDeactivate} danger title="Desactivar piloto" confirmLabel="Desactivar"
        message={`¿Desactivar a ${toDeactivate?.fullName}? No podrá ser asignado a nuevas misiones.`}
        onConfirm={async () => {
          try { await pilotApi.deactivate(toDeactivate!.id); notify('Piloto desactivado'); reload(); } catch (e) { notify(errorMessage(e), 'error'); }
          setToDeactivate(null);
        }} onClose={() => setToDeactivate(null)} />
    </>
  );
}
