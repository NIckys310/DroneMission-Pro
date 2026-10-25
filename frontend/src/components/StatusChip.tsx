import { Chip, type ChipProps } from '@mui/material';
import type { ChipColor } from '../utils/labels';

interface Props {
  value: string;
  dict: Record<string, { label: string; color: ChipColor }>;
  size?: ChipProps['size'];
  variant?: ChipProps['variant'];
}

/** Chip de color para cualquier estado (misión, drone, piloto, mantenimiento...). */
export default function StatusChip({ value, dict, size = 'small', variant = 'filled' }: Props) {
  const meta = dict[value] ?? { label: value, color: 'default' as ChipColor };
  return <Chip label={meta.label} color={meta.color} size={size} variant={variant} />;
}
