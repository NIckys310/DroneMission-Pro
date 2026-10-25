import { useEffect, useState } from 'react';
import { Alert, Button, Card, Checkbox, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tooltip, Typography } from '@mui/material';
import { userApi } from '../../api/endpoints';
import { errorMessage } from '../../api/client';
import { useFetch } from '../../hooks/useFetch';
import { useNotify } from '../../hooks/useNotify';
import PageHeader from '../../components/PageHeader';
import { Loading } from '../../components/Feedback';
import { roleName } from '../../utils/labels';

/** Matriz rol × permiso (tabla rol_permisos). Los permisos del administrador no se editan. */
export default function RolesPage() {
  const notify = useNotify();
  const roles = useFetch(userApi.roles, []);
  const permissions = useFetch(userApi.permissions, []);
  const [matrix, setMatrix] = useState<Record<number, Set<string>>>({});

  useEffect(() => {
    if (roles.data) setMatrix(Object.fromEntries(roles.data.map((r) => [r.id, new Set(r.permissions)])));
  }, [roles.data]);

  if (roles.loading || permissions.loading) return <Loading />;

  const toggle = (roleId: number, code: string) => setMatrix((m) => {
    const next = new Set(m[roleId]);
    if (next.has(code)) next.delete(code); else next.add(code);
    return { ...m, [roleId]: next };
  });

  const save = async () => {
    try {
      for (const role of roles.data ?? []) {
        if (role.name !== 'ADMINISTRADOR') await userApi.updateRolePermissions(role.id, [...matrix[role.id]]);
      }
      notify('Permisos actualizados (aplican en la próxima petición de cada usuario)');
      roles.reload(true);
    } catch (e) {
      notify(errorMessage(e), 'error');
    }
  };

  return (
    <>
      <PageHeader title="Roles y permisos" subtitle="Qué puede hacer cada rol en el sistema" actions={<Button variant="contained" onClick={save}>Guardar cambios</Button>} />
      <Alert severity="info" sx={{ mb: 2 }}>El backend valida cada endpoint con estos permisos (@PreAuthorize). El menú también se adapta a ellos.</Alert>
      <Card>
        <TableContainer>
          <Table size="small" stickyHeader>
            <TableHead>
              <TableRow>
                <TableCell>Permiso</TableCell>
                {roles.data?.map((r) => <TableCell key={r.id} align="center">{roleName[r.name]}</TableCell>)}
              </TableRow>
            </TableHead>
            <TableBody>
              {permissions.data?.map((p) => (
                <TableRow key={p.id} hover>
                  <TableCell><Typography variant="body2" fontWeight={600}>{p.code}</Typography><Typography variant="caption" color="text.secondary">{p.description}</Typography></TableCell>
                  {roles.data?.map((r) => (
                    <TableCell key={r.id} align="center">
                      <Tooltip title={r.name === 'ADMINISTRADOR' ? 'El administrador tiene todos los permisos' : ''}>
                        <span><Checkbox size="small" checked={!!matrix[r.id]?.has(p.code)} disabled={r.name === 'ADMINISTRADOR'} onChange={() => toggle(r.id, p.code)} /></span>
                      </Tooltip>
                    </TableCell>
                  ))}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>
    </>
  );
}
