import { Box, Paper, Typography } from '@mui/material';
import type { ReactNode } from 'react';

/** Fondo y marco común de las pantallas de login y recuperación. */
export default function AuthShell({ children }: { children: ReactNode }) {
  return (
    <Box sx={{ minHeight: '100vh', display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1.1fr 1fr' } }}>
      <Box sx={{
        display: { xs: 'none', md: 'flex' }, flexDirection: 'column', justifyContent: 'space-between', p: 6, color: '#fff',
        background: 'radial-gradient(circle at 20% 20%, #135c96 0%, #0b2a47 55%, #071a2d 100%)', position: 'relative', overflow: 'hidden',
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Box component="img" src="/drone.svg" alt="" sx={{ width: 44 }} />
          <Typography variant="h6" fontWeight={800}>DroneMission <Box component="span" sx={{ color: '#4dd0e1' }}>PRO</Box></Typography>
        </Box>
        <Box sx={{ position: 'relative', zIndex: 1 }}>
          <Typography variant="h3" fontWeight={800} sx={{ lineHeight: 1.15, mb: 2 }}>
            Operaciones con drones,<br />bajo control.
          </Typography>
          <Typography sx={{ color: '#b8cce0', maxWidth: 460 }}>
            Planifica misiones, asigna drones y pilotos, sigue el vuelo en tiempo real y genera reportes para tus clientes.
          </Typography>
        </Box>
        <Typography variant="caption" sx={{ color: '#6f8fae' }}>Proyecto académico · Diseño de Software</Typography>
        <Box component="svg" viewBox="0 0 400 400" sx={{ position: 'absolute', right: -80, bottom: -60, width: 420, opacity: 0.12 }}>
          <g fill="none" stroke="#4dd0e1" strokeWidth="2">
            <circle cx="200" cy="200" r="60" /><circle cx="200" cy="200" r="110" /><circle cx="200" cy="200" r="160" />
            <path d="M200 20v360M20 200h360" />
          </g>
        </Box>
      </Box>
      <Box sx={{ display: 'grid', placeItems: 'center', p: 3, bgcolor: 'background.default' }}>
        <Paper variant="outlined" sx={{ p: { xs: 3, sm: 5 }, width: '100%', maxWidth: 440 }}>{children}</Paper>
      </Box>
    </Box>
  );
}
