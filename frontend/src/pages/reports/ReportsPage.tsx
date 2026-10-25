import { useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Button, Card, CardActions, CardContent, Chip, Grid2 as Grid, Link, Tab, Tabs, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography,
} from '@mui/material';
import PictureAsPdfOutlinedIcon from '@mui/icons-material/PictureAsPdfOutlined';
import PrecisionManufacturingOutlinedIcon from '@mui/icons-material/PrecisionManufacturingOutlined';
import BadgeOutlinedIcon from '@mui/icons-material/BadgeOutlined';
import BuildOutlinedIcon from '@mui/icons-material/BuildOutlined';
import { missionApi, reportApi } from '../../api/endpoints';
import { downloadFile, errorMessage } from '../../api/client';
import { useFetch } from '../../hooks/useFetch';
import { useNotify } from '../../hooks/useNotify';
import { useAuth } from '../../auth/AuthContext';
import PageHeader from '../../components/PageHeader';
import StatusChip from '../../components/StatusChip';
import { EmptyState, Loading } from '../../components/Feedback';
import { droneStatus, maintenanceStatus, maintenanceType, missionType, pilotStatus } from '../../utils/labels';
import { formatDate, formatDateTime, formatMoney } from '../../utils/format';

const typeLabel: Record<string, string> = { MISION: 'Misión', DRONES: 'Drones', PILOTOS: 'Pilotos', MANTENIMIENTO: 'Mantenimiento' };

export default function ReportsPage() {
  const notify = useNotify();
  const { can } = useAuth();
  const canGenerate = can('REPORTES_GENERAR');
  const [tab, setTab] = useState(0);
  const history = useFetch(reportApi.history, []);
  const finished = useFetch(() => missionApi.list({ status: 'FINALIZADA' }), []);
  const drones = useFetch(() => (canGenerate ? reportApi.drones() : Promise.resolve(null)), [canGenerate]);
  const pilots = useFetch(() => (canGenerate ? reportApi.pilots() : Promise.resolve(null)), [canGenerate]);
  const maintenance = useFetch(() => (canGenerate ? reportApi.maintenance() : Promise.resolve(null)), [canGenerate]);

  const download = async (url: string, name: string) => {
    try { await downloadFile(url, name); notify('Reporte generado'); history.reload(true); } catch (e) { notify(errorMessage(e), 'error'); }
  };

  const globalCards = [
    { key: 'drones', title: 'Reporte de drones', text: 'Flota, estados, batería y horas de vuelo.', icon: <PrecisionManufacturingOutlinedIcon color="primary" />, url: '/reports/drones/pdf' },
    { key: 'pilots', title: 'Reporte de pilotos', text: 'Horas, misiones y vigencia de licencias.', icon: <BadgeOutlinedIcon color="primary" />, url: '/reports/pilots/pdf' },
    { key: 'maintenance', title: 'Reporte de mantenimiento', text: 'Historial, costos y drones afectados.', icon: <BuildOutlinedIcon color="primary" />, url: '/reports/maintenance/pdf' },
  ];

  return (
    <>
      <PageHeader title="Reportes" subtitle="Generación y descarga de reportes en PDF" />
      {canGenerate && (
        <Grid container spacing={2} sx={{ mb: 3 }}>
          {globalCards.map((c) => (
            <Grid key={c.key} size={{ xs: 12, md: 4 }}>
              <Card sx={{ height: '100%' }}>
                <CardContent sx={{ display: 'flex', gap: 2 }}>
                  {c.icon}
                  <div><Typography fontWeight={700}>{c.title}</Typography><Typography variant="body2" color="text.secondary">{c.text}</Typography></div>
                </CardContent>
                <CardActions><Button startIcon={<PictureAsPdfOutlinedIcon />} onClick={() => download(c.url, `${c.key}.pdf`)}>Descargar PDF</Button></CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }} variant="scrollable">
        <Tab label="Reportes de misión" />
        <Tab label="Historial" />
        {canGenerate && <Tab label="Drones" />}
        {canGenerate && <Tab label="Pilotos" />}
        {canGenerate && <Tab label="Mantenimiento" />}
      </Tabs>

      {tab === 0 && (
        <Card>
          {finished.loading ? <Loading /> : (
            <Table>
              <TableHead><TableRow><TableCell>Misión</TableCell><TableCell>Tipo</TableCell><TableCell>Cliente</TableCell><TableCell>Finalizada</TableCell><TableCell align="right">Reporte</TableCell></TableRow></TableHead>
              <TableBody>
                {finished.data?.map((m) => (
                  <TableRow key={m.id} hover>
                    <TableCell><Link component={RouterLink} to={`/missions/${m.id}`} fontWeight={600}>{m.code}</Link> · {m.name}</TableCell>
                    <TableCell>{missionType[m.type]}</TableCell><TableCell>{m.client.name}</TableCell><TableCell>{formatDateTime(m.actualEnd)}</TableCell>
                    <TableCell align="right"><Button size="small" startIcon={<PictureAsPdfOutlinedIcon />} onClick={() => download(`/reports/missions/${m.id}/pdf`, `reporte-${m.code}.pdf`)}>PDF</Button></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          {finished.data?.length === 0 && <EmptyState text="Aún no hay misiones finalizadas" />}
        </Card>
      )}

      {tab === 1 && (
        <Card>
          <Table>
            <TableHead><TableRow><TableCell>Fecha</TableCell><TableCell>Tipo</TableCell><TableCell>Título</TableCell><TableCell>Resumen</TableCell><TableCell>Generado por</TableCell></TableRow></TableHead>
            <TableBody>
              {history.data?.map((r) => (
                <TableRow key={r.id}>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatDateTime(r.generatedAt)}</TableCell>
                  <TableCell><Chip size="small" label={typeLabel[r.type]} /></TableCell>
                  <TableCell>{r.missionId ? <Link component={RouterLink} to={`/missions/${r.missionId}`}>{r.title}</Link> : r.title}</TableCell>
                  <TableCell sx={{ maxWidth: 380 }}>{r.summary}</TableCell>
                  <TableCell>{r.generatedBy}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          {history.data?.length === 0 && <EmptyState text="Sin reportes generados" />}
        </Card>
      )}

      {tab === 2 && drones.data && (
        <Card>
          <CardContent><Typography>Total {drones.data.total} · Disponibles {drones.data.available} · En misión {drones.data.inMission} · En mantenimiento {drones.data.inMaintenance} · {drones.data.totalFlightHours} h de vuelo</Typography></CardContent>
          <TableContainer><Table size="small">
            <TableHead><TableRow><TableCell>Código</TableCell><TableCell>Nombre</TableCell><TableCell>Modelo</TableCell><TableCell>Estado</TableCell><TableCell align="right">Batería</TableCell><TableCell align="right">Horas</TableCell><TableCell>Próx. mant.</TableCell></TableRow></TableHead>
            <TableBody>{drones.data.drones.map((d) => (
              <TableRow key={d.code}><TableCell>{d.code}</TableCell><TableCell>{d.name}</TableCell><TableCell>{d.model}</TableCell><TableCell><StatusChip value={d.status} dict={droneStatus} /></TableCell><TableCell align="right">{d.batteryLevel}%</TableCell><TableCell align="right">{d.flightHours}</TableCell><TableCell>{formatDate(d.nextMaintenanceOn)}</TableCell></TableRow>
            ))}</TableBody>
          </Table></TableContainer>
        </Card>
      )}

      {tab === 3 && pilots.data && (
        <Card>
          <CardContent><Typography>Pilotos {pilots.data.total} · Disponibles {pilots.data.available} · Licencias vencidas {pilots.data.expiredLicenses} · {pilots.data.totalFlightHours} h de vuelo</Typography></CardContent>
          <TableContainer><Table size="small">
            <TableHead><TableRow><TableCell>Piloto</TableCell><TableCell>Licencia</TableCell><TableCell>Vence</TableCell><TableCell>Estado</TableCell><TableCell align="right">Horas</TableCell><TableCell align="right">Misiones</TableCell><TableCell align="right">Finalizadas</TableCell></TableRow></TableHead>
            <TableBody>{pilots.data.pilots.map((p) => (
              <TableRow key={p.licenseNumber}><TableCell>{p.fullName}</TableCell><TableCell>{p.licenseNumber}</TableCell><TableCell>{formatDate(p.licenseExpiresOn)} {!p.licenseValid && <Chip size="small" color="error" label="Vencida" />}</TableCell><TableCell><StatusChip value={p.status} dict={pilotStatus} /></TableCell><TableCell align="right">{p.flightHours}</TableCell><TableCell align="right">{p.missions}</TableCell><TableCell align="right">{p.finishedMissions}</TableCell></TableRow>
            ))}</TableBody>
          </Table></TableContainer>
        </Card>
      )}

      {tab === 4 && maintenance.data && (
        <Card>
          <CardContent><Typography>Total {maintenance.data.total} · Completados {maintenance.data.completed} · En proceso {maintenance.data.inProgress} · Costo total {formatMoney(maintenance.data.totalCost)}</Typography></CardContent>
          <TableContainer><Table size="small">
            <TableHead><TableRow><TableCell>Fecha</TableCell><TableCell>Drone</TableCell><TableCell>Tipo</TableCell><TableCell>Técnico</TableCell><TableCell align="right">Costo</TableCell><TableCell>Estado</TableCell></TableRow></TableHead>
            <TableBody>{maintenance.data.history.map((h, i) => (
              <TableRow key={i}><TableCell>{formatDate(h.date)}</TableCell><TableCell>{h.droneCode}</TableCell><TableCell>{maintenanceType[h.type]}</TableCell><TableCell>{h.technician}</TableCell><TableCell align="right">{formatMoney(h.cost)}</TableCell><TableCell><StatusChip value={h.status} dict={maintenanceStatus} /></TableCell></TableRow>
            ))}</TableBody>
          </Table></TableContainer>
        </Card>
      )}
    </>
  );
}
