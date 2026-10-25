// Requisitos de contraseña segura (los mismos que valida el backend en PasswordPolicy.java).
export interface PasswordRule { label: string; ok: boolean }

export function passwordRules(password: string, personal: string[] = []): PasswordRule[] {
  const lower = password.toLowerCase();
  const containsPersonal = personal.some((p) => p && p.length >= 3 && lower.includes(p.toLowerCase()));
  return [
    { label: 'Al menos 8 caracteres', ok: password.length >= 8 },
    { label: 'Una letra mayúscula', ok: /[A-ZÁÉÍÓÚÑ]/.test(password) },
    { label: 'Una letra minúscula', ok: /[a-záéíóúñ]/.test(password) },
    { label: 'Un número', ok: /\d/.test(password) },
    { label: 'No contiene tu nombre, usuario o correo', ok: password.length > 0 && !containsPersonal },
  ];
}

export const isStrongPassword = (password: string, personal: string[] = []) =>
  passwordRules(password, personal).every((r) => r.ok);

export const USERNAME_REGEX = /^[a-zA-Z0-9._-]{3,30}$/;
