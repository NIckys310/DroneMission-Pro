import { incidentType, options, severity } from '../../utils/labels';
import type { FieldConfig } from '../../utils/validation';

export const incidentFields: FieldConfig[] = [
  { name: 'type', label: 'Tipo de incidente', type: 'select', required: true, options: options(incidentType) },
  { name: 'severity', label: 'Severidad', type: 'select', required: true, options: options(severity), helperText: 'Crítica: pausa automáticamente una misión en vuelo' },
  { name: 'description', label: 'Descripción', type: 'textarea', required: true, maxLength: 1000, cols: 12 },
];
