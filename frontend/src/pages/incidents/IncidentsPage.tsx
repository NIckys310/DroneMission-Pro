import { useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { Button, Card, Link, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import { incidentApi } from '../../api/endpoints';
import { useFetch } from '../../hooks/useFetch';
import { useNotify } from '../../hooks/useNotify';
import { useAuth } from '../../auth/AuthContext';
import PageHeader from '../../components/PageHeader';
import SearchToolbar from '../../components/SearchToolbar';
import StatusChip from '../../components/StatusChip';
import EntityFormDialog from '../../components/EntityFormDialog';
import { EmptyState, ErrorBox, Loading } from '../../components/Feedback';
import { incidentStatus, incidentType, options, severity } from '../../utils/labels';
import { formatDateTime } from '../../utils/format';
import type { FieldConfig } from '../../utils/validation';
import type { Incident } from '../../types';

const statusFields: FieldConfig[] = [
  { name: 'status', label: 'Estado', type: 'select', required: true, options: options(incidentStatus), cols: 12 },
  {
    name: 'resolution', label: 'Resolución', type: 'textarea', cols: 12, maxLength: 1000,
    validate: (v, all) => (all.status === 'RESUELTO' && !v ? 'Describe cómo se resolvió' : null),
  },
];

export default function IncidentsPage() {
  const notify = useNotify();
  const { can } = useAuth();
  const [sev, setSev] = useState('');
  const [status, setStatus] = useState('');
  const [type, setType] = useState('');
  const { data, loading, error, reload } = useFetch(() => incidentApi.list({ severity: sev, status, type }), [sev, status, type], 15000);
  const [editing, setEditing] = useState<Incident | null>(null);

  return (
    <>
      <PageHeader title="Incidentes" subtitle="Eventos registrados por pilotos y alertas automáticas del sistema" />
      <SearchToolbar filters={[
        { name: 'severity', label: 'Severidad', value: sev, onChange: setSev, options: options(severity) },
        { name: 'status', label: 'Estado', value: status, onChange: setStatus, options: options(incidentStatus) },
        { name: 'type', label: 'Tipo', value: type, onChange: setType, options: options(incidentType) },
      ]} />
      {error && <ErrorBox message={error} onRetry={reload} />}
      {loading ? <Loading /> : (
        <Card>
          <TableContainer>
            <Table>
              <TableHead><TableRow><TableCell>Fecha</TableCell><TableCell>Misión</TableCell><TableCell>Tipo</TableCell><TableCell>Severidad</TableCell><TableCell>Descripción</TableCell><TableCell>Registrado por</TableCell><TableCell>Estado</TableCell><TableCell /></TableRow></TableHead>
              <TableBody>
                {(data ?? []).map((i) => (
                  <TableRow key={i.id} hover>
                    <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatDateTime(i.date)}</TableCell>
                    <TableCell><Link component={RouterLink} to={`/missions/${i.missionId}`} fontWeight={600}>{i.missionCode}</Link><Typography variant="caption" display="block" color="text.secondary">{i.missionName}</Typography></TableCell>
                    <TableCell>{incidentType[i.type]}</TableCell>
                    <TableCell><StatusChip value={i.severity} dict={severity} /></TableCell>
                    <TableCell sx={{ maxWidth: 320 }}>{i.description}{i.resolution && <Typography variant="caption" display="block" color="success.main">Resolución: {i.resolution}</Typography>}</TableCell>
                    <TableCell>{i.reportedBy}</TableCell>
                    <TableCell><StatusChip value={i.status} dict={incidentStatus} /></TableCell>
                    <TableCell>{can('INCIDENTES_GESTIONAR') && i.status !== 'RESUELTO' && <Button size="small" onClick={() => setEditing(i)}>Gestionar</Button>}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          {data?.length === 0 && <EmptyState text="No hay incidentes" />}
        </Card>
      )}
      <EntityFormDialog open={!!editing} title={`Incidente #${editing?.id} · ${editing?.missionCode}`} fields={statusFields} maxWidth="sm"
        initialValues={{ status: editing?.status, resolution: editing?.resolution }} onClose={() => setEditing(null)}
        onSubmit={async (v) => { await incidentApi.changeStatus(editing!.id, String(v.status), (v.resolution as string) ?? undefined); notify('Incidente actualizado'); setEditing(null); reload(); }} />
    </>
  );
}
