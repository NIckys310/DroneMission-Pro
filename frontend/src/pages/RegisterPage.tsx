import { useState, type FormEvent } from 'react';
import { Link as RouterLink, Navigate } from 'react-router-dom';
import { Alert, Box, Button, Grid2 as Grid, IconButton, InputAdornment, Link, Stack, TextField, Typography } from '@mui/material';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { authApi } from '../api/endpoints';
import { errorMessage, fieldErrors } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import PasswordChecklist from '../components/PasswordChecklist';
import { EMAIL_REGEX } from '../utils/validation';
import { isStrongPassword, USERNAME_REGEX } from '../utils/password';
import AuthShell from './AuthShell';

type Form = { firstName: string; lastName: string; username: string; email: string; password: string; confirmPassword: string };
const empty: Form = { firstName: '', lastName: '', username: '', email: '', password: '', confirmPassword: '' };

/**
 * Registro público. La cuenta queda pendiente: un administrador debe aprobarla
 * y asignarle un rol antes de que pueda iniciar sesión.
 */
export default function RegisterPage() {
  const { user } = useAuth();
  const [form, setForm] = useState<Form>(empty);
  const [errors, setErrors] = useState<Partial<Record<keyof Form, string>>>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const [done, setDone] = useState<string | null>(null);
  const [show, setShow] = useState(false);
  const [loading, setLoading] = useState(false);

  if (user) return <Navigate to="/" replace />;
  const personal = [form.username, form.firstName, form.email.split('@')[0]];
  const set = (k: keyof Form) => (e: React.ChangeEvent<HTMLInputElement>) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const validate = () => {
    const found: typeof errors = {};
    if (!form.firstName.trim()) found.firstName = 'El nombre es obligatorio';
    if (!form.lastName.trim()) found.lastName = 'Los apellidos son obligatorios';
    if (!USERNAME_REGEX.test(form.username)) found.username = '3 a 30 caracteres: letras, números, punto, guion o guion bajo';
    if (!EMAIL_REGEX.test(form.email)) found.email = 'El correo no es válido';
    if (!isStrongPassword(form.password, personal)) found.password = 'La contraseña no cumple los requisitos';
    if (form.password !== form.confirmPassword) found.confirmPassword = 'Las contraseñas no coinciden';
    setErrors(found);
    return Object.keys(found).length === 0;
  };

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    setServerError(null);
    try {
      const r = await authApi.register(form);
      setDone(r.message);
      setForm(empty);
    } catch (err) {
      setServerError(errorMessage(err));
      setErrors(fieldErrors(err) as typeof errors);
    } finally {
      setLoading(false);
    }
  };

  const visibility = {
    input: {
      endAdornment: (
        <InputAdornment position="end">
          <IconButton aria-label={show ? 'Ocultar contraseña' : 'Mostrar contraseña'} onClick={() => setShow((s) => !s)} edge="end">
            {show ? <VisibilityOff /> : <Visibility />}
          </IconButton>
        </InputAdornment>
      ),
    },
  };

  return (
    <AuthShell>
      <Typography variant="h5" sx={{ mb: 0.5 }}>Crear cuenta</Typography>
      <Typography color="text.secondary" variant="body2" sx={{ mb: 3 }}>
        Tu cuenta quedará pendiente hasta que un administrador la apruebe y te asigne un rol.
      </Typography>
      {done ? (
        <Stack spacing={2}>
          <Alert severity="success">{done}</Alert>
          <Button component={RouterLink} to="/login" variant="contained">Volver al inicio de sesión</Button>
        </Stack>
      ) : (
        <Box component="form" onSubmit={submit} noValidate>
          {serverError && <Alert severity="error" sx={{ mb: 2 }}>{serverError}</Alert>}
          <Grid container spacing={2}>
            <Grid size={6}><TextField size="medium" label="Nombre" value={form.firstName} onChange={set('firstName')} error={!!errors.firstName} helperText={errors.firstName} /></Grid>
            <Grid size={6}><TextField size="medium" label="Apellidos" value={form.lastName} onChange={set('lastName')} error={!!errors.lastName} helperText={errors.lastName} /></Grid>
            <Grid size={12}><TextField size="medium" label="Nombre de usuario" autoComplete="username" value={form.username} onChange={set('username')} error={!!errors.username} helperText={errors.username ?? 'Con él podrás iniciar sesión'} /></Grid>
            <Grid size={12}><TextField size="medium" label="Correo electrónico" type="email" autoComplete="email" value={form.email} onChange={set('email')} error={!!errors.email} helperText={errors.email} /></Grid>
            <Grid size={12}><TextField size="medium" label="Contraseña" type={show ? 'text' : 'password'} autoComplete="new-password" value={form.password} onChange={set('password')} error={!!errors.password} helperText={errors.password} slotProps={visibility} /></Grid>
            <Grid size={12}><PasswordChecklist password={form.password} personal={personal} /></Grid>
            <Grid size={12}><TextField size="medium" label="Confirmar contraseña" type={show ? 'text' : 'password'} autoComplete="new-password" value={form.confirmPassword} onChange={set('confirmPassword')} error={!!errors.confirmPassword} helperText={errors.confirmPassword} /></Grid>
          </Grid>
          <Button type="submit" variant="contained" size="large" fullWidth sx={{ mt: 3 }} disabled={loading}>{loading ? 'Creando…' : 'Crear cuenta'}</Button>
          <Typography variant="body2" sx={{ mt: 2, textAlign: 'center' }}>
            ¿Ya tienes cuenta? <Link component={RouterLink} to="/login">Inicia sesión</Link>
          </Typography>
        </Box>
      )}
    </AuthShell>
  );
}
