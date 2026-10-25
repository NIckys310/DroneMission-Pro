import { Route, Routes } from 'react-router-dom';
import ProtectedRoute from './auth/ProtectedRoute';
import AppLayout from './components/layout/AppLayout';
import LoginPage from './pages/LoginPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import RegisterPage from './pages/RegisterPage';
import NotFoundPage from './pages/NotFoundPage';
import HomePage from './pages/home/HomePage';
import DronesPage from './pages/drones/DronesPage';
import DroneDetailPage from './pages/drones/DroneDetailPage';
import PilotsPage from './pages/pilots/PilotsPage';
import PilotDetailPage from './pages/pilots/PilotDetailPage';
import ClientsPage from './pages/clients/ClientsPage';
import ClientDetailPage from './pages/clients/ClientDetailPage';
import MissionsPage from './pages/missions/MissionsPage';
import MissionDetailPage from './pages/missions/MissionDetailPage';
import MaintenancePage from './pages/maintenance/MaintenancePage';
import IncidentsPage from './pages/incidents/IncidentsPage';
import ReportsPage from './pages/reports/ReportsPage';
import UsersPage from './pages/users/UsersPage';
import RolesPage from './pages/users/RolesPage';
import AuditPage from './pages/audit/AuditPage';

/** Rutas de la aplicación. Cada ruta exige el mismo permiso que su endpoint en el backend. */
export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route element={<ProtectedRoute permission="MISIONES_VER" />}>
            <Route path="/missions" element={<MissionsPage />} />
            <Route path="/missions/:id" element={<MissionDetailPage />} />
          </Route>
          <Route element={<ProtectedRoute permission="DRONES_VER" />}>
            <Route path="/drones" element={<DronesPage />} />
            <Route path="/drones/:id" element={<DroneDetailPage />} />
          </Route>
          <Route element={<ProtectedRoute permission="PILOTOS_VER" />}>
            <Route path="/pilots" element={<PilotsPage />} />
            <Route path="/pilots/:id" element={<PilotDetailPage />} />
          </Route>
          <Route element={<ProtectedRoute permission="CLIENTES_VER" />}>
            <Route path="/clients" element={<ClientsPage />} />
            <Route path="/clients/:id" element={<ClientDetailPage />} />
          </Route>
          <Route element={<ProtectedRoute permission="MANTENIMIENTO_VER" />}>
            <Route path="/maintenance" element={<MaintenancePage />} />
          </Route>
          <Route element={<ProtectedRoute permission="INCIDENTES_VER" />}>
            <Route path="/incidents" element={<IncidentsPage />} />
          </Route>
          <Route element={<ProtectedRoute permission="REPORTES_VER" />}>
            <Route path="/reports" element={<ReportsPage />} />
          </Route>
          <Route element={<ProtectedRoute permission="USUARIOS_GESTIONAR" />}>
            <Route path="/users" element={<UsersPage />} />
          </Route>
          <Route element={<ProtectedRoute permission="ROLES_GESTIONAR" />}>
            <Route path="/roles" element={<RolesPage />} />
          </Route>
          <Route element={<ProtectedRoute permission="AUDITORIA_VER" />}>
            <Route path="/audit" element={<AuditPage />} />
          </Route>
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  );
}
