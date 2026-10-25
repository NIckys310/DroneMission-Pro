import { useState, type FormEvent } from 'react';
import { Link as RouterLink, Navigate, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import {
  Alert, Box, Button, Divider, IconButton, InputAdornment, Link, Stack, TextField, Typography,
} from '@mui/material';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { useAuth } from '../auth/AuthContext';
import { errorMessage } from '../api/client';
import { EMAIL_REGEX } from '../utils/validation';
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
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<{ email?: string; password?: string }>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const found: typeof errors = {};
    if (!email) found.email = 'El correo es obligatorio';
    else if (!EMAIL_REGEX.test(email)) found.email = 'El correo no es válido';
    if (!password) found.password = 'La contraseña es obligatoria';
    setErrors(found);
    if (Object.keys(found).length) return;

    setLoading(true);
    setServerError(null);
    try {
      await login(email.trim(), password);
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
      <Typography color="text.secondary" variant="body2" sx={{ mb: 3 }}>Ingresa con tu correo corporativo.</Typography>
      {params.get('expired') && <Alert severity="info" sx={{ mb: 2 }}>Tu sesión expiró. Inicia sesión de nuevo.</Alert>}
      {serverError && <Alert severity="error" sx={{ mb: 2 }}>{serverError}</Alert>}

      <Box component="form" onSubmit={handleSubmit} noValidate>
        <Stack spacing={2}>
          <TextField label="Correo electrónico" type="email" autoComplete="username" value={email}
            onChange={(e) => setEmail(e.target.value)} error={!!errors.email} helperText={errors.email} size="medium" />
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

      <Divider sx={{ my: 3 }}><Typography variant="caption" color="text.secondary">USUARIOS DEMO · contraseña {DEMO_PASSWORD}</Typography></Divider>
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
        {DEMO_USERS.map((u) => (
          <Button key={u.email} size="small" variant="outlined" onClick={() => { setEmail(u.email); setPassword(DEMO_PASSWORD); }}>
            {u.role}
          </Button>
        ))}
      </Box>
    </AuthShell>
  );
}
