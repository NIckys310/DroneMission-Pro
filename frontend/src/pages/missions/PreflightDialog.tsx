import { useEffect, useState } from 'react';
import { Alert, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, Stack, Typography } from '@mui/material';
import type { Mission } from '../../types';

const CHECKS = [
  'Batería del drone verificada y asegurada',
  'Hélices y motores inspeccionados visualmente',
  'Señal GPS y enlace de radio confirmados',
  'Condiciones climáticas aptas para el vuelo',
  'Zona de despegue despejada y señalizada',
  'Plan de vuelo revisado y cargado en el drone virtual',
];

/** Lista de chequeo previa al vuelo. Al confirmarla el piloto pasa la misión a EN_PREPARACION. */
export default function PreflightDialog({ open, mission, onClose, onConfirm }: { open: boolean; mission: Mission; onClose: () => void; onConfirm: () => Promise<void> }) {
  const [checked, setChecked] = useState<boolean[]>([]);
  const [busy, setBusy] = useState(false);
  useEffect(() => { if (open) setChecked(CHECKS.map(() => false)); }, [open]);
  const all = checked.length > 0 && checked.every(Boolean);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Chequeo prevuelo · {mission.code}</DialogTitle>
      <DialogContent dividers>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
          Drone {mission.drone?.code} ({mission.drone?.batteryLevel} % de batería) · Piloto {mission.pilot?.fullName}
        </Typography>
        <Stack>
          {CHECKS.map((label, i) => (
            <FormControlLabel key={label} label={label}
              control={<Checkbox checked={checked[i] ?? false} onChange={(e) => setChecked((c) => c.map((v, idx) => (idx === i ? e.target.checked : v)))} />} />
          ))}
        </Stack>
        <Alert severity="info" sx={{ mt: 2 }}>El sistema volverá a validar licencia, disponibilidad y batería antes de aprobar el vuelo.</Alert>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancelar</Button>
        <Button variant="contained" disabled={!all || busy}
          onClick={async () => { setBusy(true); try { await onConfirm(); } finally { setBusy(false); } }}>
          Confirmar chequeo
        </Button>
      </DialogActions>
    </Dialog>
  );
}
