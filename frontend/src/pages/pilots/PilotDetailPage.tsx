import { useState } from 'react';
import { Link as RouterLink, useParams } from 'react-router-dom';
import { Alert, Button, Card, CardContent, CardHeader, Grid2 as Grid, Link, Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import { pilotApi } from '../../api/endpoints';
import { useFetch } from '../../hooks/useFetch';
import { useNotify } from '../../hooks/useNotify';
import { useAuth } from '../../auth/AuthContext';
import PageHeader from '../../components/PageHeader';
import StatusChip from '../../components/StatusChip';
import InfoItem from '../../components/InfoItem';
import { EmptyState, ErrorBox, Loading } from '../../components/Feedback';
import { missionStatus, pilotStatus } from '../../utils/labels';
import { formatDate } from '../../utils/format';
import PilotFormDialog from './PilotFormDialog';

export default function PilotDetailPage() {
  const id = Number(useParams().id);
  const notify = useNotify();
  const { can } = useAuth();
  const pilot = useFetch(() => pilotApi.get(id), [id]);
  const missions = useFetch(() => pilotApi.missions(id), [id]);
  const [editOpen, setEditOpen] = useState(false);

  if (pilot.loading) return <Loading />;
  if (pilot.error || !pilot.data) return <ErrorBox message={pilot.error ?? 'No encontrado'} />;
  const p = pilot.data;

  return (
    <>
      <PageHeader title={p.fullName} subtitle={`Licencia ${p.licenseNumber}`} breadcrumbs={[{ label: 'Pilotos', to: '/pilots' }, { label: p.fullName }]}
        actions={can('PILOTOS_GESTIONAR') && <Button variant="contained" startIcon={<EditOutlinedIcon />} onClick={() => setEditOpen(true)}>Editar</Button>} />
      {!p.licenseValid && <Alert severity="error" sx={{ mb: 2 }}>La licencia venció el {formatDate(p.licenseExpiresOn)}. El sistema impide asignarle misiones.</Alert>}
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 5 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Datos del piloto" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <CardContent>
              <Grid container spacing={2}>
                <Grid size={6}><InfoItem label="Estado" value={<StatusChip value={p.status} dict={pilotStatus} />} /></Grid>
                <Grid size={6}><InfoItem label="Horas de vuelo" value={`${p.flightHours} h`} /></Grid>
                <Grid size={6}><InfoItem label="Documento" value={p.document} /></Grid>
                <Grid size={6}><InfoItem label="Teléfono" value={p.phone} /></Grid>
                <Grid size={12}><InfoItem label="Correo" value={p.email} /></Grid>
                <Grid size={6}><InfoItem label="Vence licencia" value={formatDate(p.licenseExpiresOn)} /></Grid>
                <Grid size={6}><InfoItem label="Usuario del sistema" value={p.userEmail ?? 'Sin usuario'} /></Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 7 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Certificaciones" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <Table size="small">
              <TableHead><TableRow><TableCell>Certificación</TableCell><TableCell>Entidad</TableCell><TableCell>Emisión</TableCell><TableCell>Vence</TableCell></TableRow></TableHead>
              <TableBody>
                {p.certifications.map((c) => (
                  <TableRow key={c.id ?? c.name}><TableCell>{c.name}</TableCell><TableCell>{c.issuer}</TableCell><TableCell>{formatDate(c.issuedOn)}</TableCell><TableCell>{c.expiresOn ? formatDate(c.expiresOn) : 'No vence'}</TableCell></TableRow>
                ))}
              </TableBody>
            </Table>
            {p.certifications.length === 0 && <EmptyState text="Sin certificaciones" />}
          </Card>
        </Grid>
        <Grid size={12}>
          <Card>
            <CardHeader title="Historial de misiones" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <Table size="small">
              <TableHead><TableRow><TableCell>Misión</TableCell><TableCell>Fecha</TableCell><TableCell>Drone</TableCell><TableCell>Estado</TableCell></TableRow></TableHead>
              <TableBody>
                {missions.data?.map((m) => (
                  <TableRow key={m.missionId}>
                    <TableCell>{can('MISIONES_VER') ? <Link component={RouterLink} to={`/missions/${m.missionId}`}>{m.code}</Link> : m.code} · {m.name}</TableCell>
                    <TableCell>{formatDate(m.scheduledDate)}</TableCell><TableCell>{m.droneCode}</TableCell>
                    <TableCell><StatusChip value={m.status} dict={missionStatus} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            {missions.data?.length === 0 && <EmptyState text="Sin misiones" />}
          </Card>
        </Grid>
      </Grid>
      <PilotFormDialog open={editOpen} pilot={p} onClose={() => setEditOpen(false)}
        onSubmit={async (v) => { await pilotApi.update(id, v); notify('Piloto actualizado'); setEditOpen(false); pilot.reload(); }} />
    </>
  );
}
