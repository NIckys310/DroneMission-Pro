import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid2 as Grid, List, ListItemButton, ListItemText, Radio, Typography,
} from '@mui/material';
import { missionApi } from '../../api/endpoints';
import { errorMessage } from '../../api/client';
import BatteryIndicator from '../../components/BatteryIndicator';
import { droneType } from '../../utils/labels';
import { formatDate } from '../../utils/format';
import type { AssignmentOptions, Mission } from '../../types';

interface Props { open: boolean; mission: Mission; onClose: () => void; onAssigned: (m: Mission) => void }

/**
 * Asignación de recursos. El backend indica qué drones y pilotos son elegibles
 * y por qué los demás no lo son (mismas reglas que valida al asignar).
 */
export default function AssignDialog({ open, mission, onClose, onAssigned }: Props) {
  const [opts, setOpts] = useState<AssignmentOptions | null>(null);
  const [droneId, setDroneId] = useState<number | null>(null);
  const [pilotId, setPilotId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!open) return;
    setError(null);
    setDroneId(mission.drone?.id ?? null);
    setPilotId(mission.pilot?.id ?? null);
    missionApi.assignmentOptions(mission.id).then(setOpts).catch((e) => setError(errorMessage(e)));
  }, [open, mission]);

  const submit = async () => {
    if (!droneId || !pilotId) return;
    setBusy(true);
    setError(null);
    try {
      onAssigned(await missionApi.assign(mission.id, droneId, pilotId));
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Asignar drone y piloto · {mission.code}</DialogTitle>
      <DialogContent dividers>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {opts && (
          <Alert severity="info" sx={{ mb: 2 }}>
            Misión de {mission.type.toLowerCase()} el {formatDate(mission.scheduledDate)}: batería mínima {opts.minimumBatteryPercent} %.
            Los recursos no elegibles muestran el motivo.
          </Alert>
        )}
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 6 }}>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>Drones</Typography>
            <List dense sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2, maxHeight: 380, overflow: 'auto' }}>
              {opts?.drones.map((d) => (
                <ListItemButton key={d.id} disabled={!d.eligible} selected={droneId === d.id} onClick={() => setDroneId(d.id)} sx={{ alignItems: 'flex-start', '&.Mui-disabled': { opacity: 0.75 } }}>
                  <Radio size="small" checked={droneId === d.id} disabled={!d.eligible} sx={{ mt: -0.5 }} />
                  <ListItemText
                    primary={<Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1 }}><span><b>{d.code}</b> {d.name}</span><BatteryIndicator level={d.batteryLevel} width={96} /></Box>}
                    secondary={d.eligible ? droneType[d.type] : <Typography component="span" variant="caption" color="error.main">{d.reason}</Typography>}
                  />
                </ListItemButton>
              ))}
            </List>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>Pilotos</Typography>
            <List dense sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2, maxHeight: 380, overflow: 'auto' }}>
              {opts?.pilots.map((p) => (
                <ListItemButton key={p.id} disabled={!p.eligible} selected={pilotId === p.id} onClick={() => setPilotId(p.id)} sx={{ alignItems: 'flex-start', '&.Mui-disabled': { opacity: 0.75 } }}>
                  <Radio size="small" checked={pilotId === p.id} disabled={!p.eligible} sx={{ mt: -0.5 }} />
                  <ListItemText
                    primary={<b>{p.fullName}</b>}
                    secondary={p.eligible ? `Licencia vigente hasta ${formatDate(p.licenseExpiresOn)}` : <Typography component="span" variant="caption" color="error.main">{p.reason}</Typography>}
                  />
                </ListItemButton>
              ))}
            </List>
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancelar</Button>
        <Button variant="contained" disabled={!droneId || !pilotId || busy} onClick={submit}>Asignar y programar</Button>
      </DialogActions>
    </Dialog>
  );
}
