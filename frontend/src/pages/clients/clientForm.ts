import { clientType, options } from '../../utils/labels';
import { PHONE_REGEX, type FieldConfig } from '../../utils/validation';

export const clientFields: FieldConfig[] = [
  { name: 'type', label: 'Tipo de cliente', type: 'select', required: true, options: options(clientType), cols: 4 },
  { name: 'name', label: 'Nombre o razón social', required: true, maxLength: 150, cols: 8 },
  { name: 'document', label: 'Documento / NIT', required: true, maxLength: 30, cols: 4 },
  { name: 'email', label: 'Correo', type: 'email', required: true, cols: 4 },
  { name: 'phone', label: 'Teléfono', required: true, cols: 4, pattern: { regex: PHONE_REGEX, message: 'El teléfono no es válido' } },
  { name: 'address', label: 'Dirección', required: true, maxLength: 200, cols: 8 },
  { name: 'city', label: 'Ciudad', required: true, maxLength: 80, cols: 4 },
];
