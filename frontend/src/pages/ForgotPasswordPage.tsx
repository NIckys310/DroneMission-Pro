import { useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { Alert, Button, Link, Stack, TextField, Typography } from '@mui/material';
import { authApi } from '../api/endpoints';
import { errorMessage } from '../api/client';
import { EMAIL_REGEX } from '../utils/validation';
import PasswordChecklist from '../components/PasswordChecklist';
import { isStrongPassword } from '../utils/password';
import AuthShell from './AuthShell';

/** Recuperación de contraseña SIMULADA: el token se muestra en pantalla en lugar de enviarse por correo. */
export default function ForgotPasswordPage() {
  const [step, setStep] = useState<'email' | 'reset' | 'done'>('email');
  const [email, setEmail] = useState('');
  const [token, setToken] = useState('');
  const [password, setPassword] = useState('');
  const [info, setInfo] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const request = async () => {
    if (!EMAIL_REGEX.test(email)) { setError('El correo no es válido'); return; }
    setError(null);
    try {
      const r = await authApi.forgotPassword(email);
      setInfo(r.message);
      if (r.simulatedToken) {
        setToken(r.simulatedToken);
        setStep('reset');
      }
    } catch (e) { setError(errorMessage(e)); }
  };

  const reset = async () => {
    if (!isStrongPassword(password)) { setError('La contraseña no cumple los requisitos de seguridad'); return; }
    setError(null);
    try {
      const r = await authApi.resetPassword(token, password);
      setInfo(r.message);
      setStep('done');
    } catch (e) { setError(errorMessage(e)); }
  };

  return (
    <AuthShell>
      <Typography variant="h5" sx={{ mb: 0.5 }}>Recuperar contraseña</Typography>
      <Typography color="text.secondary" variant="body2" sx={{ mb: 3 }}>Te ayudaremos a recuperar el acceso a tu cuenta.</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {info && <Alert severity="info" sx={{ mb: 2 }}>{info}</Alert>}
      <Stack spacing={2}>
        {step === 'email' && (
          <>
            <TextField label="Correo electrónico" value={email} onChange={(e) => setEmail(e.target.value)} size="medium" />
            <Button variant="contained" size="large" onClick={request}>Enviar instrucciones</Button>
          </>
        )}
        {step === 'reset' && (
          <>
            <TextField label="Token recibido (simulado)" value={token} onChange={(e) => setToken(e.target.value)} size="medium" />
            <TextField label="Nueva contraseña" type="password" value={password} onChange={(e) => setPassword(e.target.value)} size="medium" />
            <PasswordChecklist password={password} />
            <Button variant="contained" size="large" onClick={reset}>Cambiar contraseña</Button>
          </>
        )}
        <Link component={RouterLink} to="/login" variant="body2">Volver al inicio de sesión</Link>
      </Stack>
    </AuthShell>
  );
}
