import { useEffect, useState, type FormEvent } from 'react';
import { Link as RouterLink, Navigate, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import {
  Alert, Box, Button, Divider, IconButton, InputAdornment, Link, Stack, TextField, Typography,
} from '@mui/material';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { useAuth } from '../auth/AuthContext';
import { errorMessage } from '../api/client';
import { authApi } from '../api/endpoints';
import type { AuthConfig } from '../types';
import AuthShell from './AuthShell';

const DEMO_USERS = [
  { role: 'Administrador', email: 'admin@dronemission.local' },
  { role: 'Operador', email: 'operador@dronemission.local' },
  { role: 'Piloto', email: 'piloto@dronemission.local' },
  { role: 'Técnico', email: 'tecnico@dronemission.local' },
  { role: 'Cliente', email: 'cliente@dronemission.local' },
];
const DEMO_PASSWORD = 'Demo2026!';

export default function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [params] = useSearchParams();
  const [identifier, setIdentifier] = useState('');
  const [config, setConfig] = useState<AuthConfig | null>(null);
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<{ identifier?: string; password?: string }>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // Opciones públicas: si hay usuarios demo y si el registro está habilitado
  useEffect(() => { authApi.config().then(setConfig).catch(() => setConfig(null)); }, []);

  if (user) return <Navigate to="/" replace />;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const found: typeof errors = {};
    if (!identifier.trim()) found.identifier = 'Escribe tu correo o tu nombre de usuario';
    if (!password) found.password = 'La contraseña es obligatoria';
    setErrors(found);
    if (Object.keys(found).length) return;

    setLoading(true);
    setServerError(null);
    try {
      await login(identifier.trim(), password);
      const from = (location.state as { from?: string } | null)?.from ?? '/';
      navigate(from, { replace: true });
    } catch (err) {
      setServerError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell>
      <Typography variant="h5" sx={{ mb: 0.5 }}>Iniciar sesión</Typography>
      <Typography color="text.secondary" variant="body2" sx={{ mb: 3 }}>Ingresa con tu correo o tu nombre de usuario.</Typography>
      {params.get('expired') && <Alert severity="info" sx={{ mb: 2 }}>Tu sesión expiró. Inicia sesión de nuevo.</Alert>}
      {serverError && <Alert severity="error" sx={{ mb: 2 }}>{serverError}</Alert>}

      <Box component="form" onSubmit={handleSubmit} noValidate>
        <Stack spacing={2}>
          <TextField label="Correo o usuario" autoComplete="username" value={identifier}
            onChange={(e) => setIdentifier(e.target.value)} error={!!errors.identifier} helperText={errors.identifier} size="medium" />
          <TextField label="Contraseña" type={showPassword ? 'text' : 'password'} autoComplete="current-password" value={password}
            onChange={(e) => setPassword(e.target.value)} error={!!errors.password} helperText={errors.password} size="medium"
            slotProps={{
              input: {
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'} onClick={() => setShowPassword((s) => !s)} edge="end">
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              },
            }} />
          <Box sx={{ textAlign: 'right' }}>
            <Link component={RouterLink} to="/forgot-password" variant="body2">¿Olvidaste tu contraseña?</Link>
          </Box>
          <Button type="submit" variant="contained" size="large" disabled={loading}>{loading ? 'Ingresando…' : 'Ingresar'}</Button>
        </Stack>
      </Box>

      {config?.registrationEnabled && (
        <Typography variant="body2" sx={{ mt: 3, textAlign: 'center' }}>
          ¿No tienes cuenta? <Link component={RouterLink} to="/register" fontWeight={600}>Crear cuenta</Link>
        </Typography>
      )}

      {config?.demoUsersEnabled && (
        <>
          <Divider sx={{ my: 3 }}><Typography variant="caption" color="text.secondary">USUARIOS DEMO · contraseña {DEMO_PASSWORD}</Typography></Divider>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
            {DEMO_USERS.map((u) => (
              <Button key={u.email} size="small" variant="outlined" onClick={() => { setIdentifier(u.email); setPassword(DEMO_PASSWORD); }}>
                {u.role}
              </Button>
            ))}
          </Box>
        </>
      )}
    </AuthShell>
  );
}
