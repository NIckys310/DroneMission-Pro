import { useState } from 'react';
import { Card, Chip, Table, TableBody, TableCell, TableContainer, TableHead, TableRow } from '@mui/material';
import { auditApi } from '../../api/endpoints';
import { useFetch } from '../../hooks/useFetch';
import PageHeader from '../../components/PageHeader';
import SearchToolbar from '../../components/SearchToolbar';
import { ErrorBox, Loading } from '../../components/Feedback';
import { formatDateTime } from '../../utils/format';

/** Bitácora de auditoría: quién hizo qué y cuándo. */
export default function AuditPage() {
  const { data, loading, error, reload } = useFetch(() => auditApi.list(500), []);
  const [q, setQ] = useState('');
  const [entity, setEntity] = useState('');
  const entities = [...new Set((data ?? []).map((a) => a.entity))].sort();
  const rows = (data ?? []).filter((a) => (!entity || a.entity === entity)
    && (!q || `${a.userEmail} ${a.action} ${a.description}`.toLowerCase().includes(q.toLowerCase())));

  return (
    <>
      <PageHeader title="Auditoría" subtitle="Registro de las operaciones importantes del sistema" />
      <SearchToolbar search={q} onSearch={setQ} placeholder="Buscar por usuario, acción o descripción"
        filters={[{ name: 'entity', label: 'Entidad', value: entity, onChange: setEntity, options: entities.map((e) => ({ value: e, label: e })) }]} />
      {error && <ErrorBox message={error} onRetry={reload} />}
      {loading ? <Loading /> : (
        <Card>
          <TableContainer>
            <Table size="small">
              <TableHead><TableRow><TableCell>Fecha</TableCell><TableCell>Usuario</TableCell><TableCell>Acción</TableCell><TableCell>Entidad</TableCell><TableCell>Descripción</TableCell></TableRow></TableHead>
              <TableBody>
                {rows.map((a) => (
                  <TableRow key={a.id} hover>
                    <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatDateTime(a.date)}</TableCell>
                    <TableCell>{a.userEmail}</TableCell>
                    <TableCell><Chip size="small" label={a.action} color={a.action.includes('FALLIDO') ? 'error' : 'default'} /></TableCell>
                    <TableCell>{a.entity}{a.entityId ? ` #${a.entityId}` : ''}</TableCell>
                    <TableCell>{a.description}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Card>
      )}
    </>
  );
}
