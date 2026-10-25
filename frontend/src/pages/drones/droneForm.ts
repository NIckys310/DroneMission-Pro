import type { Drone } from '../../types';
import { droneType, options } from '../../utils/labels';
import { today } from '../../utils/format';
import type { FieldConfig } from '../../utils/validation';

/** Campos del formulario de drones. Para agregar un campo: añádelo aquí y en DroneRequest (backend). */
export const droneFields: FieldConfig[] = [
  { name: 'code', label: 'Código interno', maxLength: 20, helperText: 'Vacío = se genera automáticamente (DRN-XXX)', cols: 4 },
  { name: 'name', label: 'Nombre', required: true, maxLength: 100, cols: 4 },
  { name: 'type', label: 'Tipo', type: 'select', required: true, options: options(droneType), cols: 4 },
  { name: 'manufacturer', label: 'Fabricante', required: true, maxLength: 80, cols: 4 },
  { name: 'model', label: 'Modelo', required: true, maxLength: 80, cols: 4 },
  { name: 'serialNumber', label: 'Número de serie', required: true, maxLength: 60, cols: 4 },
  { name: 'batteryCapacityMah', label: 'Capacidad batería (mAh)', type: 'number', min: 1, cols: 4, helperText: 'Vacío = valor típico del tipo' },
  { name: 'batteryLevel', label: 'Batería actual (%)', type: 'number', min: 0, max: 100, cols: 4 },
  { name: 'flightHours', label: 'Horas de vuelo', type: 'number', min: 0, cols: 4 },
  { name: 'maxSpeedKmh', label: 'Velocidad máx. (km/h)', type: 'number', min: 1, cols: 4 },
  { name: 'autonomyMinutes', label: 'Autonomía (min)', type: 'number', min: 1, cols: 4 },
  {
    name: 'acquiredOn', label: 'Fecha de adquisición', type: 'date', required: true, cols: 4,
    validate: (v) => (String(v) > today() ? 'No puede ser una fecha futura' : null),
  },
  { name: 'lastMaintenanceOn', label: 'Último mantenimiento', type: 'date', cols: 6 },
  {
    name: 'nextMaintenanceOn', label: 'Próximo mantenimiento', type: 'date', cols: 6,
    validate: (v, all) => (v && all.lastMaintenanceOn && String(v) <= String(all.lastMaintenanceOn) ? 'Debe ser posterior al último mantenimiento' : null),
  },
  { name: 'description', label: 'Descripción', type: 'textarea', maxLength: 1000, cols: 12 },
];

export const emptyDrone = { batteryLevel: 100, flightHours: 0, acquiredOn: today() };

export const droneToForm = (d: Drone): Record<string, unknown> => ({ ...d });
