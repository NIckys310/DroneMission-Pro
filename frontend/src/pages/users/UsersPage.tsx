import { useState } from 'react';
import {
  Alert, Button, Card, CardHeader, Chip, IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tooltip, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import BlockIcon from '@mui/icons-material/Block';
import HowToRegIcon from '@mui/icons-material/HowToReg';
import PersonOffOutlinedIcon from '@mui/icons-material/PersonOffOutlined';
import LockClockOutlinedIcon from '@mui/icons-material/LockClockOutlined';
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
import { passwordRules, USERNAME_REGEX } from '../../utils/password';
import type { FieldConfig } from '../../utils/validation';
import type { Client, Pilot, User } from '../../types';

const activeDict = { true: { label: 'Activo', color: 'success' as const }, false: { label: 'Inactivo', color: 'default' as const } };

const passwordCheck = (v: unknown, all: Record<string, unknown>) => {
  if (!v) return null;
  const failed = passwordRules(String(v), [String(all.username ?? ''), String(all.firstName ?? '')]).filter((r) => !r.ok);
  return failed.length ? `Falta: ${failed.map((r) => r.label.toLowerCase()).join(', ')}` : null;
};

const linkFields = (pilots: Pilot[], clients: Client[]): FieldConfig[] => [
  {
    name: 'pilotId', label: 'Ficha de piloto vinculada', type: 'select', helperText: 'Solo para el rol Piloto',
    options: pilots.map((p) => ({ value: p.id, label: p.fullName })), validate: (v, all) => (v && all.role !== 'PILOTO' ? 'Solo aplica al rol Piloto' : null),
  },
  {
    name: 'clientId', label: 'Cliente vinculado', type: 'select', helperText: 'Solo para el rol Cliente',
    options: clients.map((c) => ({ value: c.id, label: c.name })), validate: (v, all) => (v && all.role !== 'CLIENTE' ? 'Solo aplica al rol Cliente' : null),
  },
];

const userFields = (isNew: boolean, pilots: Pilot[], clients: Client[]): FieldConfig[] => [
  { name: 'firstName', label: 'Nombre', required: true, maxLength: 80 },
  { name: 'lastName', label: 'Apellidos', required: true, maxLength: 80 },
  { name: 'username', label: 'Nombre de usuario', required: true, pattern: { regex: USERNAME_REGEX, message: '3 a 30 caracteres: letras, números, punto, guion o guion bajo' } },
  { name: 'email', label: 'Correo', type: 'email', required: true },
  { name: 'role', label: 'Rol', type: 'select', required: true, options: options(roleName) },
  {
    name: 'password', label: isNew ? 'Contraseña' : 'Nueva contraseña (opcional)', type: 'password', required: isNew, maxLength: 64,
    helperText: 'Mín. 8 caracteres con mayúscula, minúscula y número', validate: passwordCheck,
  },
  { name: 'active', label: 'Usuario activo', type: 'checkbox' },
  ...linkFields(pilots, clients),
];

const approveFields = (pilots: Pilot[], clients: Client[]): FieldConfig[] => [
  { name: 'role', label: 'Rol que tendrá', type: 'select', required: true, options: options(roleName), cols: 12 },
  ...linkFields(pilots, clients),
];

export default function UsersPage() {
  const notify = useNotify();
  const { data, loading, error, reload } = useFetch(userApi.list, []);
  const pilots = useFetch(() => pilotApi.list(), []);
  const clients = useFetch(() => clientApi.list(), []);
  const [editing, setEditing] = useState<User | null | 'new'>(null);
  const [approving, setApproving] = useState<User | null>(null);
  const [rejecting, setRejecting] = useState<User | null>(null);
  const [toDeactivate, setToDeactivate] = useState<User | null>(null);

  const pending = (data ?? []).filter((u) => u.pendingApproval);
  const accounts = (data ?? []).filter((u) => !u.pendingApproval);
  const isLocked = (u: User) => !!u.lockedUntil && new Date(u.lockedUntil) > new Date();

  return (
    <>
      <PageHeader title="Usuarios" subtitle="Cuentas de acceso, aprobación de registros y roles (solo administrador)"
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setEditing('new')}>Nuevo usuario</Button>} />
      {error && <ErrorBox message={error} onRetry={reload} />}
      {loading ? <Loading /> : (
        <>
          {pending.length > 0 && (
            <Card sx={{ mb: 2, borderColor: 'warning.main' }}>
              <CardHeader title={`Cuentas pendientes de aprobación (${pending.length})`}
                subheader="Personas que se registraron. No pueden entrar hasta que apruebes su cuenta y le asignes un rol."
                titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
              <Table size="small">
                <TableHead><TableRow><TableCell>Persona</TableCell><TableCell>Usuario</TableCell><TableCell>Registrada</TableCell><TableCell align="right">Acciones</TableCell></TableRow></TableHead>
                <TableBody>
                  {pending.map((u) => (
                    <TableRow key={u.id}>
                      <TableCell><Typography variant="body2" fontWeight={600}>{u.fullName}</Typography><Typography variant="caption" color="text.secondary">{u.email}</Typography></TableCell>
                      <TableCell>{u.username}</TableCell>
                      <TableCell>{formatDateTime(u.createdAt)}</TableCell>
                      <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                        <Button size="small" variant="contained" color="success" startIcon={<HowToRegIcon />} onClick={() => setApproving(u)}>Aprobar</Button>
                        <Button size="small" color="error" startIcon={<PersonOffOutlinedIcon />} sx={{ ml: 1 }} onClick={() => setRejecting(u)}>Rechazar</Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Card>
          )}
          {pending.length === 0 && <Alert severity="info" sx={{ mb: 2 }}>No hay cuentas nuevas esperando aprobación.</Alert>}

          <Card>
            <TableContainer>
              <Table>
                <TableHead><TableRow><TableCell>Persona</TableCell><TableCell>Usuario</TableCell><TableCell>Rol</TableCell><TableCell>Estado</TableCell><TableCell>Último acceso</TableCell><TableCell align="right">Acciones</TableCell></TableRow></TableHead>
                <TableBody>
                  {accounts.map((u) => (
                    <TableRow key={u.id} hover>
                      <TableCell><Typography variant="body2" fontWeight={600}>{u.fullName}</Typography><Typography variant="caption" color="text.secondary">{u.email}</Typography></TableCell>
                      <TableCell>{u.username}</TableCell>
                      <TableCell>{roleName[u.role]}</TableCell>
                      <TableCell>
                        <StatusChip value={String(u.active)} dict={activeDict} />
                        {isLocked(u) && (
                          <Tooltip title={`Bloqueada por intentos fallidos hasta ${formatDateTime(u.lockedUntil)}. Para desbloquearla, asígnale una nueva contraseña.`}>
                            <Chip size="small" color="warning" icon={<LockClockOutlinedIcon />} label="Bloqueada" sx={{ ml: 0.5 }} />
                          </Tooltip>
                        )}
                      </TableCell>
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
        </>
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
      <EntityFormDialog open={!!approving} title={`Aprobar cuenta de ${approving?.fullName}`} maxWidth="sm" submitLabel="Aprobar cuenta"
        fields={approveFields(pilots.data ?? [], clients.data ?? [])} initialValues={{ role: 'CLIENTE' }}
        onClose={() => setApproving(null)}
        onSubmit={async (v) => {
          await userApi.approve(approving!.id, v);
          notify(`Cuenta de ${approving?.username} aprobada: ya puede iniciar sesión`);
          setApproving(null);
          reload();
        }} />
      <ConfirmDialog open={!!rejecting} danger title="Rechazar registro" confirmLabel="Rechazar"
        message={`¿Rechazar y eliminar la cuenta de ${rejecting?.fullName} (${rejecting?.username})?`} onClose={() => setRejecting(null)}
        onConfirm={async () => {
          try { await userApi.reject(rejecting!.id); notify('Registro rechazado'); reload(); } catch (e) { notify(errorMessage(e), 'error'); }
          setRejecting(null);
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
