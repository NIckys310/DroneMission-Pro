import { Avatar, Box, Card, CardContent, Typography } from '@mui/material';
import type { ReactNode } from 'react';

interface Props {
  label: string;
  value: ReactNode;
  icon: ReactNode;
  color?: string;
  caption?: string;
}

/** Tarjeta de indicador (KPI) del dashboard. */
export default function StatCard({ label, value, icon, color = '#0e5a9c', caption }: Props) {
  return (
    <Card sx={{ height: '100%' }}>
      <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <Avatar variant="rounded" sx={{ bgcolor: `${color}18`, color, width: 46, height: 46 }}>{icon}</Avatar>
        <Box sx={{ minWidth: 0 }}>
          <Typography variant="body2" color="text.secondary" noWrap>{label}</Typography>
          <Typography variant="h5" sx={{ lineHeight: 1.2 }}>{value}</Typography>
          {caption && <Typography variant="caption" color="text.secondary">{caption}</Typography>}
        </Box>
      </CardContent>
    </Card>
  );
}
