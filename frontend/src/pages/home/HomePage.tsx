import { useAuth } from '../../auth/AuthContext';
import OpsDashboard from './OpsDashboard';
import PilotHome from './PilotHome';
import ClientHome from './ClientHome';

/** La pantalla de inicio depende del rol: dashboard de operaciones, piloto o cliente. */
export default function HomePage() {
  const { hasRole } = useAuth();
  if (hasRole('PILOTO')) return <PilotHome />;
  if (hasRole('CLIENTE')) return <ClientHome />;
  return <OpsDashboard />;
}
