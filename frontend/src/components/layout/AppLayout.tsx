import { useState, type ReactNode } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import {
  AppBar, Avatar, Box, Chip, Divider, Drawer, IconButton, List, ListItemButton, ListItemIcon, ListItemText, Menu, MenuItem,
  Toolbar, Tooltip, Typography, useMediaQuery, useTheme,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DashboardOutlinedIcon from '@mui/icons-material/DashboardOutlined';
import FlightTakeoffIcon from '@mui/icons-material/FlightTakeoff';
import PrecisionManufacturingOutlinedIcon from '@mui/icons-material/PrecisionManufacturingOutlined';
import BadgeOutlinedIcon from '@mui/icons-material/BadgeOutlined';
import BusinessOutlinedIcon from '@mui/icons-material/BusinessOutlined';
import BuildOutlinedIcon from '@mui/icons-material/BuildOutlined';
import ReportProblemOutlinedIcon from '@mui/icons-material/ReportProblemOutlined';
import AssessmentOutlinedIcon from '@mui/icons-material/AssessmentOutlined';
import GroupOutlinedIcon from '@mui/icons-material/GroupOutlined';
import AdminPanelSettingsOutlinedIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import HistoryOutlinedIcon from '@mui/icons-material/HistoryOutlined';
import ViewInArIcon from '@mui/icons-material/ViewInAr';
import LogoutIcon from '@mui/icons-material/Logout';
import { useAuth } from '../../auth/AuthContext';
import { roleName } from '../../utils/labels';
import NotificationBell from './NotificationBell';
import InstallDesktopIcon from '@mui/icons-material/InstallDesktop';
import { Button } from '@mui/material';
import { useInstallPrompt } from '../../hooks/useInstallPrompt';

const DRAWER_WIDTH = 248;

interface NavItem { to: string; label: string; icon: ReactNode; permission?: string }

/** Menú lateral: cada opción se muestra solo si el usuario tiene el permiso. */
function useNavItems(): NavItem[] {
  const { hasRole } = useAuth();
  const ownMissions = hasRole('PILOTO', 'CLIENTE');
  return [
    { to: '/', label: 'Inicio', icon: <DashboardOutlinedIcon /> },
    { to: '/missions', label: ownMissions ? 'Mis misiones' : 'Misiones', icon: <FlightTakeoffIcon />, permission: 'MISIONES_VER' },
    { to: '/simulator', label: 'Simulador 3D', icon: <ViewInArIcon /> },
    { to: '/drones', label: 'Drones', icon: <PrecisionManufacturingOutlinedIcon />, permission: 'DRONES_VER' },
    { to: '/pilots', label: 'Pilotos', icon: <BadgeOutlinedIcon />, permission: 'PILOTOS_VER' },
    { to: '/clients', label: 'Clientes', icon: <BusinessOutlinedIcon />, permission: 'CLIENTES_VER' },
    { to: '/maintenance', label: 'Mantenimiento', icon: <BuildOutlinedIcon />, permission: 'MANTENIMIENTO_VER' },
    { to: '/incidents', label: 'Incidentes', icon: <ReportProblemOutlinedIcon />, permission: 'INCIDENTES_VER' },
    { to: '/reports', label: 'Reportes', icon: <AssessmentOutlinedIcon />, permission: 'REPORTES_VER' },
    { to: '/users', label: 'Usuarios', icon: <GroupOutlinedIcon />, permission: 'USUARIOS_GESTIONAR' },
    { to: '/roles', label: 'Roles y permisos', icon: <AdminPanelSettingsOutlinedIcon />, permission: 'ROLES_GESTIONAR' },
    { to: '/audit', label: 'Auditoría', icon: <HistoryOutlinedIcon />, permission: 'AUDITORIA_VER' },
  ];
}

export default function AppLayout() {
  const theme = useTheme();
  const isDesktop = useMediaQuery(theme.breakpoints.up('md'));
  const [mobileOpen, setMobileOpen] = useState(false);
  const [menuAnchor, setMenuAnchor] = useState<HTMLElement | null>(null);
  const { user, can, logout } = useAuth();
  const navigate = useNavigate();
  const items = useNavItems().filter((i) => !i.permission || can(i.permission));
  const { canInstall, install } = useInstallPrompt();

  const drawer = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: '#0b2a47', color: '#d6e4f2' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, px: 2.5, py: 2.2 }}>
        <Box component="img" src="/drone.svg" alt="" sx={{ width: 34, height: 34 }} />
        <Box>
          <Typography fontWeight={800} color="#fff" lineHeight={1.1}>DroneMission</Typography>
          <Typography variant="caption" sx={{ color: '#4dd0e1', fontWeight: 700, letterSpacing: 2 }}>PRO</Typography>
        </Box>
      </Box>
      <Divider sx={{ borderColor: 'rgba(255,255,255,0.08)' }} />
      <List sx={{ px: 1.2, py: 1.5, flex: 1 }}>
        {items.map((item) => (
          <ListItemButton
            key={item.to}
            component={NavLink}
            to={item.to}
            end={item.to === '/'}
            onClick={() => setMobileOpen(false)}
            sx={{
              borderRadius: 2, mb: 0.3, color: '#b8cce0',
              '& .MuiListItemIcon-root': { color: '#7fa6c9', minWidth: 38 },
              '&.active': { bgcolor: 'rgba(77,208,225,0.14)', color: '#fff', '& .MuiListItemIcon-root': { color: '#4dd0e1' } },
              '&:hover': { bgcolor: 'rgba(255,255,255,0.06)' },
            }}
          >
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primary={item.label} primaryTypographyProps={{ fontSize: 14, fontWeight: 500 }} />
          </ListItemButton>
        ))}
      </List>
      <Box sx={{ p: 2, fontSize: 11, color: '#6f8fae' }}>v1.0 · Proyecto Diseño de Software</Box>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <AppBar position="fixed" color="inherit" elevation={0}
        sx={{ width: { md: `calc(100% - ${DRAWER_WIDTH}px)` }, ml: { md: `${DRAWER_WIDTH}px` }, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'rgba(255,255,255,0.92)', backdropFilter: 'blur(6px)' }}>
        <Toolbar sx={{ gap: 1 }}>
          {!isDesktop && <IconButton onClick={() => setMobileOpen(true)} edge="start"><MenuIcon /></IconButton>}
          <Box sx={{ flex: 1 }} />
          {canInstall && (
            <Button size="small" variant="outlined" startIcon={<InstallDesktopIcon />} onClick={install} sx={{ mr: 1 }}>
              Instalar aplicación
            </Button>
          )}
          <NotificationBell />
          <Tooltip title="Mi cuenta">
            <IconButton onClick={(e) => setMenuAnchor(e.currentTarget)} sx={{ gap: 1, borderRadius: 2 }}>
              <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main', fontSize: 14 }}>{user?.fullName.charAt(0)}</Avatar>
              <Box sx={{ textAlign: 'left', display: { xs: 'none', sm: 'block' } }}>
                <Typography fontSize={13} fontWeight={600} color="text.primary" lineHeight={1.2}>{user?.fullName}</Typography>
                <Typography fontSize={11} color="text.secondary">{roleName[user?.role ?? '']}</Typography>
              </Box>
            </IconButton>
          </Tooltip>
          <Menu anchorEl={menuAnchor} open={!!menuAnchor} onClose={() => setMenuAnchor(null)}>
            <Box sx={{ px: 2, py: 1 }}>
              <Typography fontWeight={600}>{user?.fullName}</Typography>
              <Typography variant="body2" color="text.secondary">{user?.email}</Typography>
              <Chip size="small" label={roleName[user?.role ?? '']} color="primary" variant="outlined" sx={{ mt: 1 }} />
            </Box>
            <Divider />
            <MenuItem onClick={async () => { await logout(); navigate('/login'); }}>
              <ListItemIcon><LogoutIcon fontSize="small" /></ListItemIcon> Cerrar sesión
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { md: DRAWER_WIDTH }, flexShrink: { md: 0 } }}>
        <Drawer variant={isDesktop ? 'permanent' : 'temporary'} open={isDesktop || mobileOpen} onClose={() => setMobileOpen(false)}
          sx={{ '& .MuiDrawer-paper': { width: DRAWER_WIDTH, border: 'none' } }}>
          {drawer}
        </Drawer>
      </Box>

      <Box component="main" sx={{ flexGrow: 1, width: { md: `calc(100% - ${DRAWER_WIDTH}px)` }, p: { xs: 2, md: 3.5 }, mt: 8 }}>
        <Outlet />
      </Box>
    </Box>
  );
}
