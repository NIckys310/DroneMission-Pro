import { describe, expect, it } from 'vitest';
import { normalize, validateAll, validateField, type FieldConfig } from './validation';

const fields: FieldConfig[] = [
  { name: 'name', label: 'El nombre', required: true },
  { name: 'email', label: 'El correo', type: 'email', required: true },
  { name: 'battery', label: 'La batería', type: 'number', min: 0, max: 100 },
];

describe('validación de formularios', () => {
  it('marca los campos obligatorios vacíos', () => {
    const errors = validateAll(fields, { name: '', email: '' });
    expect(errors.name).toBe('El nombre es obligatorio');
    expect(errors.email).toBe('El correo es obligatorio');
  });

  it('valida el formato del correo', () => {
    expect(validateField(fields[1], 'no-es-correo', {})).toBe('El correo no es válido');
    expect(validateField(fields[1], 'piloto@dronemission.local', {})).toBeNull();
  });

  it('valida rangos numéricos', () => {
    expect(validateField(fields[2], 120, {})).toBe('El valor máximo es 100');
    expect(validateField(fields[2], -1, {})).toBe('El valor mínimo es 0');
    expect(validateField(fields[2], 80, {})).toBeNull();
  });

  it('usa reglas personalizadas entre campos', () => {
    const end: FieldConfig = {
      name: 'end', label: 'La hora fin', validate: (v, all) => (String(v) <= String(all.start) ? 'Debe ser posterior al inicio' : null),
    };
    expect(validateField(end, '08:00', { start: '09:00' })).toBe('Debe ser posterior al inicio');
    expect(validateField(end, '10:00', { start: '09:00' })).toBeNull();
  });

  it('normaliza vacíos a null y números a number', () => {
    expect(normalize(fields, { name: 'X', email: '', battery: '50' })).toEqual({ name: 'X', email: null, battery: 50 });
  });
});
