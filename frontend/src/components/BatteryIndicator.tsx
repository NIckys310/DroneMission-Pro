import { Box, LinearProgress, Tooltip, Typography } from '@mui/material';
import BatteryAlertIcon from '@mui/icons-material/BatteryAlert';
import Battery50Icon from '@mui/icons-material/Battery50';
import BatteryFullIcon from '@mui/icons-material/BatteryFull';

/** Indicador visual del nivel de batería (rojo < 30 %, ámbar < 60 %, verde). */
export default function BatteryIndicator({ level, width = 110 }: { level: number; width?: number }) {
  const color = level < 30 ? 'error' : level < 60 ? 'warning' : 'success';
  const Icon = level < 30 ? BatteryAlertIcon : level < 60 ? Battery50Icon : BatteryFullIcon;
  return (
    <Tooltip title={`Batería ${level} %`}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75, width }}>
        <Icon fontSize="small" color={color} sx={{ transform: 'rotate(90deg)' }} />
        <LinearProgress variant="determinate" value={level} color={color} sx={{ flex: 1, height: 6, borderRadius: 3 }} />
        <Typography variant="caption" sx={{ minWidth: 32, textAlign: 'right', fontWeight: 600 }}>{level}%</Typography>
      </Box>
    </Tooltip>
  );
}
