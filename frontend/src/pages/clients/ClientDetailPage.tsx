import { Link as RouterLink, useParams } from 'react-router-dom';
import { Card, CardContent, CardHeader, Grid2 as Grid, Link, Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material';
import { clientApi, missionApi } from '../../api/endpoints';
import { useFetch } from '../../hooks/useFetch';
import PageHeader from '../../components/PageHeader';
import InfoItem from '../../components/InfoItem';
import StatusChip from '../../components/StatusChip';
import { EmptyState, ErrorBox, Loading } from '../../components/Feedback';
import { clientType, missionStatus, missionType } from '../../utils/labels';
import { formatDate } from '../../utils/format';

export default function ClientDetailPage() {
  const id = Number(useParams().id);
  const client = useFetch(() => clientApi.get(id), [id]);
  const missions = useFetch(() => missionApi.list({ clientId: id }), [id]);

  if (client.loading) return <Loading />;
  if (client.error || !client.data) return <ErrorBox message={client.error ?? 'No encontrado'} />;
  const c = client.data;

  return (
    <>
      <PageHeader title={c.name} subtitle={`${clientType[c.type]} · ${c.document}`} breadcrumbs={[{ label: 'Clientes', to: '/clients' }, { label: c.name }]} />
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Datos de contacto" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <CardContent sx={{ display: 'grid', gap: 2 }}>
              <InfoItem label="Correo" value={c.email} />
              <InfoItem label="Teléfono" value={c.phone} />
              <InfoItem label="Dirección" value={`${c.address}, ${c.city}`} />
              <InfoItem label="Usuario del portal" value={c.userEmail ?? 'Sin usuario'} />
              <InfoItem label="Estado" value={c.active ? 'Activo' : 'Inactivo'} />
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 8 }}>
          <Card>
            <CardHeader title={`Misiones (${missions.data?.length ?? 0})`} titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
            <Table size="small">
              <TableHead><TableRow><TableCell>Misión</TableCell><TableCell>Tipo</TableCell><TableCell>Fecha</TableCell><TableCell>Estado</TableCell></TableRow></TableHead>
              <TableBody>
                {missions.data?.map((m) => (
                  <TableRow key={m.id} hover>
                    <TableCell><Link component={RouterLink} to={`/missions/${m.id}`} fontWeight={600}>{m.code}</Link> · {m.name}</TableCell>
                    <TableCell>{missionType[m.type]}</TableCell><TableCell>{formatDate(m.scheduledDate)}</TableCell>
                    <TableCell><StatusChip value={m.status} dict={missionStatus} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            {missions.data?.length === 0 && <EmptyState text="El cliente no tiene misiones" />}
          </Card>
        </Grid>
      </Grid>
    </>
  );
}
