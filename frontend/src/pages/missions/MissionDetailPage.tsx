import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Alert, Box, Button, Card, CardContent, CardHeader, Chip, Grid2 as Grid, IconButton, Link, Stack, Tab, Tabs, Table, TableBody,
  TableCell, TableHead, TableRow, Typography, Tooltip,
} from '@mui/material';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import SendOutlinedIcon from '@mui/icons-material/SendOutlined';
import AssignmentIndOutlinedIcon from '@mui/icons-material/AssignmentIndOutlined';
import CancelOutlinedIcon from '@mui/icons-material/CancelOutlined';
import FactCheckOutlinedIcon from '@mui/icons-material/FactCheckOutlined';
import FlightTakeoffIcon from '@mui/icons-material/FlightTakeoff';
import PauseCircleOutlineIcon from '@mui/icons-material/PauseCircleOutline';
import PlayCircleOutlineIcon from '@mui/icons-material/PlayCircleOutline';
import FlightLandIcon from '@mui/icons-material/FlightLand';
import NoteAddOutlinedIcon from '@mui/icons-material/NoteAddOutlined';
import ReportProblemOutlinedIcon from '@mui/icons-material/ReportProblemOutlined';
import AddPhotoAlternateOutlinedIcon from '@mui/icons-material/AddPhotoAlternateOutlined';
import PictureAsPdfOutlinedIcon from '@mui/icons-material/PictureAsPdfOutlined';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import DownloadIcon from '@mui/icons-material/Download';
import { clientApi, evidenceApi, missionApi } from '../../api/endpoints';
import { downloadFile, errorMessage } from '../../api/client';
import { useFetch } from '../../hooks/useFetch';
import { useNotify } from '../../hooks/useNotify';
import { useAuth } from '../../auth/AuthContext';
import PageHeader from '../../components/PageHeader';
import StatusChip from '../../components/StatusChip';
import InfoItem from '../../components/InfoItem';
import BatteryIndicator from '../../components/BatteryIndicator';
import EntityFormDialog from '../../components/EntityFormDialog';
import ConfirmDialog from '../../components/ConfirmDialog';
import AuthImage from '../../components/AuthImage';
import { EmptyState, ErrorBox, Loading } from '../../components/Feedback';
import {
  droneStatus, evidenceType, incidentStatus, incidentType, missionStatus, missionType, pilotStatus, priority, severity,
} from '../../utils/labels';
import { formatDate, formatDateTime, formatTime } from '../../utils/format';
import type { Mission, MissionAction } from '../../types';
import LifecycleStepper from './LifecycleStepper';
import AssignDialog from './AssignDialog';
import PreflightDialog from './PreflightDialog';
import EvidenceDialog from './EvidenceDialog';
import TelemetryPanel from './TelemetryPanel';
import FlightPlanPanel from './FlightPlanPanel';
import { missionFields, missionToForm } from './missionForm';
import { incidentFields } from './incidentForm';

type DialogName = 'edit' | 'delete' | 'assign' | 'cancel' | 'prepare' | 'finish' | 'observe' | 'incident' | 'evidence' | 'pause' | null;

export default function MissionDetailPage() {
  const id = Number(useParams().id);
  const navigate = useNavigate();
  const notify = useNotify();
  const { can } = useAuth();
  const [tab, setTab] = useState(0);
  const [dialog, setDialog] = useState<DialogName>(null);
  const mission = useFetch(() => missionApi.get(id), [id]);
  const m = mission.data;
  const flying = m ? ['EN_VUELO', 'PAUSADA', 'EN_PREPARACION'].includes(m.status) : false;
  const plan = useFetch(() => missionApi.flightPlan(id), [id, m?.type, m?.latitude, m?.longitude, m?.targetAltitudeM]);
  const incidents = useFetch(() => missionApi.incidents(id), [id], flying ? 5000 : undefined);
  const evidences = useFetch(() => missionApi.evidences(id), [id]);
  const history = useFetch(() => missionApi.history(id), [id, m?.status]);
  const clients = useFetch(() => (can('MISIONES_GESTIONAR') ? clientApi.list() : Promise.resolve([])), []);

  // El estado puede cambiar solo (pausa automática por incidente crítico): refresca en vuelo
  const reloadMission = mission.reload;
  useEffect(() => {
    if (!flying) return undefined;
    const t = setInterval(() => reloadMission(true), 5000);
    return () => clearInterval(t);
  }, [flying, reloadMission]);

  if (mission.loading) return <Loading />;
  if (mission.error || !m) return <ErrorBox message={mission.error ?? 'Misión no encontrada'} />;

  const has = (a: MissionAction) => m.availableActions.includes(a);
  const refreshAll = (updated?: Mission) => {
    if (updated) mission.setData(updated); else mission.reload(true);
    incidents.reload(true);
    evidences.reload(true);
    history.reload(true);
  };
  const run = async (action: () => Promise<Mission>, ok: string) => {
    try {
      const updated = await action();
      notify(ok);
      refreshAll(updated);
      setDialog(null);
    } catch (e) {
      notify(errorMessage(e), 'error');
      throw e;
    }
  };

  const actionButtons = (
    <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
      {has('EDIT') && <Button startIcon={<EditOutlinedIcon />} onClick={() => setDialog('edit')}>Editar</Button>}
      {has('DELETE') && <Button color="error" startIcon={<DeleteOutlineIcon />} onClick={() => setDialog('delete')}>Eliminar</Button>}
      {has('SUBMIT') && <Button variant="contained" startIcon={<SendOutlinedIcon />} onClick={() => run(() => missionApi.submit(id), 'Misión enviada a planificación').catch(() => undefined)}>Enviar a planificación</Button>}
      {has('ASSIGN') && <Button variant={m.status === 'PENDIENTE' ? 'contained' : 'outlined'} startIcon={<AssignmentIndOutlinedIcon />} onClick={() => setDialog('assign')}>{m.drone ? 'Reasignar recursos' : 'Asignar recursos'}</Button>}
      {has('PREPARE') && <Button variant="contained" startIcon={<FactCheckOutlinedIcon />} onClick={() => setDialog('prepare')}>Chequeo prevuelo</Button>}
      {has('START') && <Button variant="contained" color="primary" startIcon={<FlightTakeoffIcon />} onClick={() => run(() => missionApi.start(id), '¡Despegue! El drone virtual está en vuelo').then(() => setTab(1)).catch(() => undefined)}>Despegar</Button>}
      {has('PAUSE') && <Button variant="outlined" color="warning" startIcon={<PauseCircleOutlineIcon />} onClick={() => setDialog('pause')}>Pausar</Button>}
      {has('RESUME') && <Button variant="contained" startIcon={<PlayCircleOutlineIcon />} onClick={() => run(() => missionApi.resume(id), 'Vuelo reanudado').catch(() => undefined)}>Reanudar</Button>}
      {has('FINISH') && <Button variant="contained" color="success" startIcon={<FlightLandIcon />} onClick={() => setDialog('finish')}>Finalizar</Button>}
      {has('INCIDENT') && <Button startIcon={<ReportProblemOutlinedIcon />} onClick={() => setDialog('incident')}>Incidente</Button>}
      {has('EVIDENCE') && <Button startIcon={<AddPhotoAlternateOutlinedIcon />} onClick={() => setDialog('evidence')}>Evidencia</Button>}
      {has('OBSERVE') && <Button startIcon={<NoteAddOutlinedIcon />} onClick={() => setDialog('observe')}>Observación</Button>}
      {has('REPORT') && <Button variant="outlined" startIcon={<PictureAsPdfOutlinedIcon />} onClick={() => downloadFile(`/reports/missions/${id}/pdf`, `reporte-${m.code}.pdf`).catch((e) => notify(errorMessage(e), 'error'))}>Reporte PDF</Button>}
      {has('CANCEL') && <Button color="error" startIcon={<CancelOutlinedIcon />} onClick={() => setDialog('cancel')}>Cancelar misión</Button>}
    </Stack>
  );

  return (
    <>
      <PageHeader title={`${m.code} · ${m.name}`} subtitle={`${missionType[m.type]} · ${m.client.name} · ${formatDate(m.scheduledDate)} ${formatTime(m.startTime)}–${formatTime(m.estimatedEndTime)}`}
        breadcrumbs={[{ label: 'Misiones', to: '/missions' }, { label: m.code }]}
        actions={<Stack direction="row" spacing={1}><StatusChip value={m.status} dict={missionStatus} size="medium" /><StatusChip value={m.priority} dict={priority} size="medium" variant="outlined" /></Stack>} />

      <Card sx={{ mb: 2 }}>
        <CardContent>
          <LifecycleStepper status={m.status} />
          {m.availableActions.length > 0 && <Box sx={{ mt: 2 }}>{actionButtons}</Box>}
          {m.status === 'PROGRAMADA' && !has('PREPARE') && m.pilot && can('MISIONES_EJECUTAR') && (
            <Alert severity="info" sx={{ mt: 2 }}>Solo el piloto asignado ({m.pilot.fullName}) puede preparar y despegar esta misión.</Alert>
          )}
        </CardContent>
      </Card>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }} variant="scrollable" allowScrollButtonsMobile>
        <Tab label="Resumen" />
        <Tab label="Vuelo y telemetría" />
        <Tab label="Plan de vuelo" />
        <Tab label={`Incidentes (${incidents.data?.length ?? 0})`} />
        <Tab label={`Evidencias (${evidences.data?.length ?? 0})`} />
        <Tab label="Historial" />
      </Tabs>

      {tab === 0 && (
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 8 }}>
            <Card sx={{ height: '100%' }}>
              <CardHeader title="Información general" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
              <CardContent>
                <Grid container spacing={2}>
                  <Grid size={12}><InfoItem label="Objetivo" value={m.objective} /></Grid>
                  <Grid size={12}><InfoItem label="Descripción" value={m.description} /></Grid>
                  <Grid size={{ xs: 12, sm: 6 }}><InfoItem label="Ubicación" value={m.location} /></Grid>
                  <Grid size={{ xs: 6, sm: 3 }}><InfoItem label="Coordenadas" value={`${m.latitude.toFixed(5)}, ${m.longitude.toFixed(5)}`} /></Grid>
                  <Grid size={{ xs: 6, sm: 3 }}><InfoItem label="Altitud objetivo" value={`${m.targetAltitudeM} m`} /></Grid>
                  <Grid size={{ xs: 6, sm: 3 }}><InfoItem label="Inicio real" value={formatDateTime(m.actualStart)} /></Grid>
                  <Grid size={{ xs: 6, sm: 3 }}><InfoItem label="Fin real" value={formatDateTime(m.actualEnd)} /></Grid>
                  <Grid size={{ xs: 6, sm: 3 }}><InfoItem label="Creada por" value={m.createdBy} /></Grid>
                  <Grid size={{ xs: 6, sm: 3 }}><InfoItem label="Creada el" value={formatDateTime(m.createdAt)} /></Grid>
                  <Grid size={12}><InfoItem label="Observaciones" value={m.observations ? <Box sx={{ whiteSpace: 'pre-line' }}>{m.observations}</Box> : '—'} /></Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Stack spacing={2}>
              <Card>
                <CardHeader title="Cliente" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
                <CardContent sx={{ pt: 0 }}>
                  {can('CLIENTES_VER') ? <Link component="button" onClick={() => navigate(`/clients/${m.client.id}`)} fontWeight={600}>{m.client.name}</Link> : <Typography fontWeight={600}>{m.client.name}</Typography>}
                </CardContent>
              </Card>
              <Card>
                <CardHeader title="Drone asignado" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
                <CardContent sx={{ pt: 0, display: 'grid', gap: 1.5 }}>
                  {m.drone ? (
                    <>
                      <Typography fontWeight={600}>{m.drone.code} · {m.drone.name}</Typography>
                      <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}><StatusChip value={m.drone.status} dict={droneStatus} /><BatteryIndicator level={m.drone.batteryLevel} /></Box>
                    </>
                  ) : <Typography color="text.secondary" variant="body2">Sin drone asignado</Typography>}
                </CardContent>
              </Card>
              <Card>
                <CardHeader title="Piloto asignado" titleTypographyProps={{ variant: 'subtitle1', fontWeight: 700 }} />
                <CardContent sx={{ pt: 0, display: 'grid', gap: 1 }}>
                  {m.pilot ? (
                    <>
                      <Typography fontWeight={600}>{m.pilot.fullName}</Typography>
                      <Box><StatusChip value={m.pilot.status} dict={pilotStatus} /></Box>
                      <Typography variant="caption" color="text.secondary">Licencia vigente hasta {formatDate(m.pilot.licenseExpiresOn)}</Typography>
                    </>
                  ) : <Typography color="text.secondary" variant="body2">Sin piloto asignado</Typography>}
                </CardContent>
              </Card>
            </Stack>
          </Grid>
        </Grid>
      )}

      {tab === 1 && <TelemetryPanel mission={m} plan={plan.data} />}
      {tab === 2 && (plan.data ? <FlightPlanPanel plan={plan.data} /> : <Loading />)}

      {tab === 3 && (
        <Card>
          <Table>
            <TableHead><TableRow><TableCell>Fecha</TableCell><TableCell>Tipo</TableCell><TableCell>Severidad</TableCell><TableCell>Descripción</TableCell><TableCell>Registrado por</TableCell><TableCell>Estado</TableCell></TableRow></TableHead>
            <TableBody>
              {incidents.data?.map((i) => (
                <TableRow key={i.id}>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatDateTime(i.date)}</TableCell>
                  <TableCell>{incidentType[i.type]}</TableCell>
                  <TableCell><StatusChip value={i.severity} dict={severity} /></TableCell>
                  <TableCell>{i.description}{i.resolution && <Typography variant="caption" display="block" color="success.main">Resolución: {i.resolution}</Typography>}</TableCell>
                  <TableCell>{i.reportedBy}</TableCell>
                  <TableCell><StatusChip value={i.status} dict={incidentStatus} /></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          {incidents.data?.length === 0 && <EmptyState text="Sin incidentes registrados" />}
        </Card>
      )}

      {tab === 4 && (
        <>
          {evidences.data?.length === 0 && <Card><EmptyState text="Sin evidencias registradas" /></Card>}
          <Grid container spacing={2}>
            {evidences.data?.map((e) => {
              const isImage = e.type === 'FOTOGRAFIA' && e.url !== '-';
              return (
                <Grid key={e.id} size={{ xs: 12, sm: 6, lg: 4 }}>
                  <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                    {isImage && <AuthImage src={e.url} alt={e.name} />}
                    <CardContent sx={{ flex: 1 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1 }}>
                        <Typography fontWeight={600}>{e.name}</Typography>
                        <Chip size="small" label={evidenceType[e.type]} />
                      </Box>
                      <Typography variant="body2" color="text.secondary" sx={{ my: 1 }}>{e.description}</Typography>
                      <Typography variant="caption" color="text.secondary">{e.uploadedBy} · {formatDateTime(e.date)}{!e.visibleToClient && ' · interna'}</Typography>
                    </CardContent>
                    <Box sx={{ px: 1, pb: 1, display: 'flex', justifyContent: 'flex-end' }}>
                      {e.storedFile && <Tooltip title="Descargar"><IconButton onClick={() => downloadFile(e.url.replace(/^\/api/, ''), e.name)}><DownloadIcon /></IconButton></Tooltip>}
                      {!e.storedFile && e.url !== '-' && <Tooltip title="Abrir"><IconButton component="a" href={e.url} target="_blank" rel="noreferrer"><OpenInNewIcon /></IconButton></Tooltip>}
                      {can('MISIONES_GESTIONAR') && (
                        <Tooltip title="Eliminar"><IconButton color="error" onClick={async () => {
                          try { await evidenceApi.remove(e.id); notify('Evidencia eliminada'); evidences.reload(true); } catch (err) { notify(errorMessage(err), 'error'); }
                        }}><DeleteOutlineIcon /></IconButton></Tooltip>
                      )}
                    </Box>
                  </Card>
                </Grid>
              );
            })}
          </Grid>
        </>
      )}

      {tab === 5 && (
        <Card>
          <Table size="small">
            <TableHead><TableRow><TableCell>Fecha</TableCell><TableCell>Acción</TableCell><TableCell>Usuario</TableCell><TableCell>Detalle</TableCell></TableRow></TableHead>
            <TableBody>
              {history.data?.map((h) => (
                <TableRow key={h.id}>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatDateTime(h.date)}</TableCell>
                  <TableCell><Chip size="small" variant="outlined" label={h.action} /></TableCell>
                  <TableCell>{h.userEmail}</TableCell>
                  <TableCell>{h.description}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          {history.data?.length === 0 && <EmptyState text="Sin registros" />}
        </Card>
      )}

      {/* ---------------------------------------------------------------- diálogos */}
      <EntityFormDialog open={dialog === 'edit'} title={`Editar ${m.code}`} fields={missionFields(clients.data ?? [])} initialValues={missionToForm(m)}
        onClose={() => setDialog(null)} onSubmit={async (v) => { refreshAll(await missionApi.update(id, v)); notify('Misión actualizada'); setDialog(null); plan.reload(true); }} />
      <ConfirmDialog open={dialog === 'delete'} danger title="Eliminar borrador" confirmLabel="Eliminar" message={`¿Eliminar definitivamente la misión ${m.code}?`}
        onClose={() => setDialog(null)} onConfirm={async () => {
          try { await missionApi.remove(id); notify('Misión eliminada'); navigate('/missions'); } catch (e) { notify(errorMessage(e), 'error'); }
        }} />
      <AssignDialog open={dialog === 'assign'} mission={m} onClose={() => setDialog(null)}
        onAssigned={(updated) => { notify('Recursos asignados: la misión quedó PROGRAMADA'); refreshAll(updated); setDialog(null); }} />
      <PreflightDialog open={dialog === 'prepare'} mission={m} onClose={() => setDialog(null)}
        onConfirm={() => run(() => missionApi.prepare(id), 'Chequeo aprobado: plan de vuelo cargado en el drone').catch(() => undefined)} />
      <ConfirmDialog open={dialog === 'pause'} title="Pausar vuelo" confirmLabel="Pausar" inputLabel="Motivo (opcional)"
        message="El drone virtual se detendrá en su posición actual." onClose={() => setDialog(null)}
        onConfirm={(text) => run(() => missionApi.pause(id, text), 'Vuelo pausado').catch(() => undefined)} />
      <ConfirmDialog open={dialog === 'finish'} title="Finalizar misión" confirmLabel="Finalizar" inputLabel="Observaciones finales (opcional)"
        message="Se sumarán las horas de vuelo, se liberarán el drone y el piloto y se generará el reporte." onClose={() => setDialog(null)}
        onConfirm={(text) => run(() => missionApi.finish(id, text), 'Misión finalizada. Reporte generado.').catch(() => undefined)} />
      <ConfirmDialog open={dialog === 'cancel'} danger title="Cancelar misión" confirmLabel="Cancelar misión" inputLabel="Motivo de cancelación" inputRequired
        message="Una misión cancelada no puede reabrirse." onClose={() => setDialog(null)}
        onConfirm={(text) => run(() => missionApi.cancel(id, text), 'Misión cancelada').catch(() => undefined)} />
      <ConfirmDialog open={dialog === 'observe'} title="Registrar observación" confirmLabel="Guardar" inputLabel="Observación" inputRequired
        message="La observación queda registrada en la misión y en la auditoría." onClose={() => setDialog(null)}
        onConfirm={(text) => run(() => missionApi.observe(id, text), 'Observación registrada').catch(() => undefined)} />
      <EntityFormDialog open={dialog === 'incident'} title="Registrar incidente" fields={incidentFields} initialValues={{ severity: 'MEDIA' }} maxWidth="sm"
        onClose={() => setDialog(null)} onSubmit={async (v) => {
          await missionApi.addIncident(id, v);
          notify(v.severity === 'CRITICA' ? 'Incidente crítico registrado: operaciones fue notificado' : 'Incidente registrado', v.severity === 'CRITICA' ? 'warning' : 'success');
          setDialog(null);
          refreshAll();
        }} />
      <EvidenceDialog open={dialog === 'evidence'} missionId={id} onClose={() => setDialog(null)}
        onSaved={() => { notify('Evidencia registrada'); setDialog(null); evidences.reload(true); history.reload(true); }} />
    </>
  );
}
