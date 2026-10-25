import { Alert, Box, Step, StepLabel, Stepper } from '@mui/material';
import type { MissionStatus } from '../../types';
import { missionStatus } from '../../utils/labels';

const FLOW: MissionStatus[] = ['BORRADOR', 'PENDIENTE', 'PROGRAMADA', 'EN_PREPARACION', 'EN_VUELO', 'FINALIZADA'];

/** Muestra en qué punto de la máquina de estados está la misión. */
export default function LifecycleStepper({ status }: { status: MissionStatus }) {
  if (status === 'CANCELADA') {
    return <Alert severity="error" variant="outlined">Misión cancelada: no puede reabrirse ni iniciarse.</Alert>;
  }
  const effective = status === 'PAUSADA' ? 'EN_VUELO' : status;
  const active = FLOW.indexOf(effective);
  return (
    <Box sx={{ overflowX: 'auto', py: 1 }}>
      <Stepper activeStep={status === 'FINALIZADA' ? FLOW.length : active} alternativeLabel sx={{ minWidth: 640 }}>
        {FLOW.map((s) => (
          <Step key={s}>
            <StepLabel error={status === 'PAUSADA' && s === 'EN_VUELO'}
              optional={status === 'PAUSADA' && s === 'EN_VUELO' ? <Box component="span" sx={{ fontSize: 11, color: 'warning.main', fontWeight: 600 }}>Pausada</Box> : undefined}>
              {missionStatus[s].label}
            </StepLabel>
          </Step>
        ))}
      </Stepper>
    </Box>
  );
}
