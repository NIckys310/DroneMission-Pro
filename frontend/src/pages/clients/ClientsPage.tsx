import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Card, IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tooltip, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import VisibilityOutlinedIcon from '@mui/icons-material/VisibilityOutlined';
import BlockIcon from '@mui/icons-material/Block';
import { clientApi } from '../../api/endpoints';
import { errorMessage } from '../../api/client';
import { useFetch } from '../../hooks/useFetch';
import { useNotify } from '../../hooks/useNotify';
import { useAuth } from '../../auth/AuthContext';
import PageHeader from '../../components/PageHeader';
import SearchToolbar from '../../components/SearchToolbar';
import EntityFormDialog from '../../components/EntityFormDialog';
import ConfirmDialog from '../../components/ConfirmDialog';
import StatusChip from '../../components/StatusChip';
import { EmptyState, ErrorBox, Loading } from '../../components/Feedback';
import { clientType } from '../../utils/labels';
import type { Client } from '../../types';
import { clientFields } from './clientForm';

const activeDict = { true: { label: 'Activo', color: 'success' as const }, false: { label: 'Inactivo', color: 'default' as const } };

export default function ClientsPage() {
  const navigate = useNavigate();
  const notify = useNotify();
  const { can } = useAuth();
  const [q, setQ] = useState('');
  const { data, loading, error, reload } = useFetch(() => clientApi.list({ q }), [q]);
  const [editing, setEditing] = useState<Client | null | 'new'>(null);
  const [toDeactivate, setToDeactivate] = useState<Client | null>(null);
  const canManage = can('CLIENTES_GESTIONAR');

  const save = async (values: Record<string, unknown>) => {
    if (editing === 'new') await clientApi.create(values);
    else if (editing) await clientApi.update(editing.id, { ...values, active: editing.active });
    notify(editing === 'new' ? 'Cliente creado' : 'Cliente actualizado');
    setEditing(null);
    reload();
  };

  return (
    <>
      <PageHeader title="Clientes" subtitle="Empresas y personas que contratan misiones"
        actions={canManage && <Button variant="contained" startIcon={<AddIcon />} onClick={() => setEditing('new')}>Nuevo cliente</Button>} />
      <SearchToolbar search={q} onSearch={setQ} placeholder="Buscar por nombre, documento o ciudad" />
      {error && <ErrorBox message={error} onRetry={reload} />}
      {loading ? <Loading /> : (
        <Card>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow><TableCell>Cliente</TableCell><TableCell>Tipo</TableCell><TableCell>Documento</TableCell><TableCell>Contacto</TableCell><TableCell>Ciudad</TableCell><TableCell align="right">Misiones</TableCell><TableCell>Estado</TableCell><TableCell align="right">Acciones</TableCell></TableRow>
              </TableHead>
              <TableBody>
                {(data ?? []).map((c) => (
                  <TableRow key={c.id} hover>
                    <TableCell sx={{ fontWeight: 600 }}>{c.name}</TableCell>
                    <TableCell>{clientType[c.type]}</TableCell>
                    <TableCell>{c.document}</TableCell>
                    <TableCell><Typography variant="body2">{c.email}</Typography><Typography variant="caption" color="text.secondary">{c.phone}</Typography></TableCell>
                    <TableCell>{c.city}</TableCell>
                    <TableCell align="right">{c.missionCount}</TableCell>
                    <TableCell><StatusChip value={String(c.active)} dict={activeDict} /></TableCell>
                    <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                      <Tooltip title="Ver detalle"><IconButton onClick={() => navigate(`/clients/${c.id}`)}><VisibilityOutlinedIcon /></IconButton></Tooltip>
                      {canManage && <Tooltip title="Editar"><IconButton onClick={() => setEditing(c)}><EditOutlinedIcon /></IconButton></Tooltip>}
                      {canManage && c.active && <Tooltip title="Desactivar"><IconButton color="error" onClick={() => setToDeactivate(c)}><BlockIcon /></IconButton></Tooltip>}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          {data?.length === 0 && <EmptyState text="No hay clientes que coincidan" />}
        </Card>
      )}
      <EntityFormDialog open={editing !== null} title={editing === 'new' ? 'Nuevo cliente' : 'Editar cliente'} fields={clientFields}
        initialValues={editing && editing !== 'new' ? { ...editing } : { type: 'EMPRESA' }} onSubmit={save} onClose={() => setEditing(null)} />
      <ConfirmDialog open={!!toDeactivate} danger title="Desactivar cliente" confirmLabel="Desactivar"
        message={`¿Desactivar a ${toDeactivate?.name}? No se podrán crear misiones nuevas para este cliente.`}
        onConfirm={async () => {
          try { await clientApi.deactivate(toDeactivate!.id); notify('Cliente desactivado'); reload(); } catch (e) { notify(errorMessage(e), 'error'); }
          setToDeactivate(null);
        }} onClose={() => setToDeactivate(null)} />
    </>
  );
}
