// Validaciones del lado del cliente. Mejoran la experiencia, pero el backend
// SIEMPRE vuelve a validar (nunca se confía solo en el frontend).

export type FieldType = 'text' | 'email' | 'number' | 'date' | 'time' | 'select' | 'textarea' | 'password' | 'checkbox';

export interface FieldConfig {
  name: string;
  label: string;
  type?: FieldType;
  required?: boolean;
  options?: { value: string | number; label: string }[];
  min?: number;
  max?: number;
  minLength?: number;
  maxLength?: number;
  pattern?: { regex: RegExp; message: string };
  helperText?: string;
  /** Ancho en la grilla de 12 columnas (por defecto 6). */
  cols?: number;
  disabled?: boolean;
  /** Regla adicional: devuelve el mensaje de error o null. */
  validate?: (value: unknown, values: Record<string, unknown>) => string | null;
}

export const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
export const PHONE_REGEX = /^[0-9+()\s-]{7,30}$/;

export function validateField(field: FieldConfig, value: unknown, values: Record<string, unknown>): string | null {
  const empty = value === undefined || value === null || value === '';
  if (field.required && empty) return `${field.label} es obligatorio`;
  if (empty) return null;

  const text = String(value);
  if (field.type === 'email' && !EMAIL_REGEX.test(text)) return 'El correo no es válido';
  if (field.type === 'number') {
    const n = Number(value);
    if (Number.isNaN(n)) return 'Debe ser un número';
    if (field.min !== undefined && n < field.min) return `El valor mínimo es ${field.min}`;
    if (field.max !== undefined && n > field.max) return `El valor máximo es ${field.max}`;
  }
  if (field.minLength && text.length < field.minLength) return `Mínimo ${field.minLength} caracteres`;
  if (field.maxLength && text.length > field.maxLength) return `Máximo ${field.maxLength} caracteres`;
  if (field.pattern && !field.pattern.regex.test(text)) return field.pattern.message;
  return field.validate ? field.validate(value, values) : null;
}

export function validateAll(fields: FieldConfig[], values: Record<string, unknown>): Record<string, string> {
  const errors: Record<string, string> = {};
  for (const field of fields) {
    const error = validateField(field, values[field.name], values);
    if (error) errors[field.name] = error;
  }
  return errors;
}

/** Convierte strings vacíos en null y los campos numéricos en number antes de enviar. */
export function normalize(fields: FieldConfig[], values: Record<string, unknown>): Record<string, unknown> {
  const result: Record<string, unknown> = { ...values };
  for (const field of fields) {
    const v = values[field.name];
    if (v === '' || v === undefined) result[field.name] = null;
    else if (field.type === 'number') result[field.name] = Number(v);
  }
  return result;
}
