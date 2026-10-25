import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import { useAuth } from './AuthContext';
import ForbiddenPage from '../pages/ForbiddenPage';

/** Exige sesión iniciada; si se indica "permission", también exige ese permiso. */
export default function ProtectedRoute({ permission }: { permission?: string }) {
  const { user, loading, can } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <Box sx={{ display: 'grid', placeItems: 'center', height: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  if (permission && !can(permission)) {
    return <ForbiddenPage />;
  }
  return <Outlet />;
}
