import { useState } from 'react';
import { Button, Card, IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tooltip, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import BlockIcon from '@mui/icons-material/Block';
import { clientApi, pilotApi, userApi } from '../../api/endpoints';
import { errorMessage } from '../../api/client';
import { useFetch } from '../../hooks/useFetch';
import { useNotify } from '../../hooks/useNotify';
import PageHeader from '../../components/PageHeader';
import StatusChip from '../../components/StatusChip';
import EntityFormDialog from '../../components/EntityFormDialog';
import ConfirmDialog from '../../components/ConfirmDialog';
import { ErrorBox, Loading } from '../../components/Feedback';
import { options, roleName } from '../../utils/labels';
import { formatDateTime } from '../../utils/format';
import type { FieldConfig } from '../../utils/validation';
import type { Client, Pilot, User } from '../../types';

const activeDict = { true: { label: 'Activo', color: 'success' as const }, false: { label: 'Inactivo', color: 'default' as const } };

const userFields = (isNew: boolean, pilots: Pilot[], clients: Client[]): FieldConfig[] => [
  { name: 'firstName', label: 'Nombre', required: true, maxLength: 80 },
  { name: 'lastName', label: 'Apellidos', required: true, maxLength: 80 },
  { name: 'email', label: 'Correo', type: 'email', required: true },
  { name: 'role', label: 'Rol', type: 'select', required: true, options: options(roleName) },
  { name: 'password', label: isNew ? 'Contraseña' : 'Nueva contraseña (opcional)', type: 'password', required: isNew, minLength: 8, maxLength: 64 },
  { name: 'active', label: 'Usuario activo', type: 'checkbox' },
  {
    name: 'pilotId', label: 'Ficha de piloto vinculada', type: 'select', helperText: 'Solo para el rol Piloto',
    options: pilots.map((p) => ({ value: p.id, label: p.fullName })), validate: (v, all) => (v && all.role !== 'PILOTO' ? 'Solo aplica al rol Piloto' : null),
  },
  {
    name: 'clientId', label: 'Cliente vinculado', type: 'select', helperText: 'Solo para el rol Cliente',
    options: clients.map((c) => ({ value: c.id, label: c.name })), validate: (v, all) => (v && all.role !== 'CLIENTE' ? 'Solo aplica al rol Cliente' : null),
  },
];

export default function UsersPage() {
  const notify = useNotify();
  const { data, loading, error, reload } = useFetch(userApi.list, []);
  const pilots = useFetch(() => pilotApi.list(), []);
  const clients = useFetch(() => clientApi.list(), []);
  const [editing, setEditing] = useState<User | null | 'new'>(null);
  const [toDeactivate, setToDeactivate] = useState<User | null>(null);

  return (
    <>
      <PageHeader title="Usuarios" subtitle="Cuentas de acceso y roles (solo administrador)"
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setEditing('new')}>Nuevo usuario</Button>} />
      {error && <ErrorBox message={error} onRetry={reload} />}
      {loading ? <Loading /> : (
        <Card>
          <TableContainer>
            <Table>
              <TableHead><TableRow><TableCell>Usuario</TableCell><TableCell>Rol</TableCell><TableCell>Estado</TableCell><TableCell>Último acceso</TableCell><TableCell align="right">Acciones</TableCell></TableRow></TableHead>
              <TableBody>
                {(data ?? []).map((u) => (
                  <TableRow key={u.id} hover>
                    <TableCell><Typography variant="body2" fontWeight={600}>{u.fullName}</Typography><Typography variant="caption" color="text.secondary">{u.email}</Typography></TableCell>
                    <TableCell>{roleName[u.role]}</TableCell>
                    <TableCell><StatusChip value={String(u.active)} dict={activeDict} /></TableCell>
                    <TableCell>{formatDateTime(u.lastLoginAt)}</TableCell>
                    <TableCell align="right">
                      <Tooltip title="Editar"><IconButton onClick={() => setEditing(u)}><EditOutlinedIcon /></IconButton></Tooltip>
                      {u.active && <Tooltip title="Desactivar"><IconButton color="error" onClick={() => setToDeactivate(u)}><BlockIcon /></IconButton></Tooltip>}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Card>
      )}
      <EntityFormDialog open={editing !== null} title={editing === 'new' ? 'Nuevo usuario' : 'Editar usuario'}
        fields={userFields(editing === 'new', pilots.data ?? [], clients.data ?? [])}
        initialValues={editing && editing !== 'new' ? { ...editing, password: '' } : { role: 'OPERADOR', active: true }}
        onClose={() => setEditing(null)}
        onSubmit={async (v) => {
          if (editing === 'new') await userApi.create(v); else if (editing) await userApi.update(editing.id, v);
          notify('Usuario guardado');
          setEditing(null);
          reload();
        }} />
      <ConfirmDialog open={!!toDeactivate} danger title="Desactivar usuario" confirmLabel="Desactivar"
        message={`¿Desactivar a ${toDeactivate?.fullName}? No podrá iniciar sesión.`} onClose={() => setToDeactivate(null)}
        onConfirm={async () => {
          try { await userApi.deactivate(toDeactivate!.id); notify('Usuario desactivado'); reload(); } catch (e) { notify(errorMessage(e), 'error'); }
          setToDeactivate(null);
        }} />
    </>
  );
}
