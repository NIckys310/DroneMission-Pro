import { describe, expect, it } from 'vitest';
import { isStrongPassword, passwordRules, USERNAME_REGEX } from './password';

describe('contraseñas seguras', () => {
  it('acepta una contraseña fuerte', () => {
    expect(isStrongPassword('Vuelo2026Seguro')).toBe(true);
  });

  it('rechaza contraseñas débiles y dice qué falta', () => {
    expect(isStrongPassword('12345678')).toBe(false);
    const failed = passwordRules('abc').filter((r) => !r.ok).map((r) => r.label);
    expect(failed).toContain('Al menos 8 caracteres');
    expect(failed).toContain('Una letra mayúscula');
  });

  it('no permite usar el nombre de usuario dentro de la contraseña', () => {
    expect(isStrongPassword('Nicoll2026x', ['nicoll'])).toBe(false);
  });

  it('valida el formato del nombre de usuario', () => {
    expect(USERNAME_REGEX.test('nicoll.lo')).toBe(true);
    expect(USERNAME_REGEX.test('ab')).toBe(false);
    expect(USERNAME_REGEX.test('con espacio')).toBe(false);
  });
});
