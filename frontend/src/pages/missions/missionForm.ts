import type { Client, Mission } from '../../types';
import { missionType, options, priority } from '../../utils/labels';
import { today } from '../../utils/format';
import type { FieldConfig } from '../../utils/validation';

/** Campos del formulario de misión (las opciones de cliente se cargan desde la API). */
export function missionFields(clients: Client[]): FieldConfig[] {
  return [
    { name: 'name', label: 'Nombre de la misión', required: true, maxLength: 150, cols: 8 },
    { name: 'type', label: 'Tipo', type: 'select', required: true, options: options(missionType), cols: 4, helperText: 'Define el patrón de vuelo (Strategy)' },
    { name: 'clientId', label: 'Cliente', type: 'select', required: true, cols: 8, options: clients.filter((c) => c.active).map((c) => ({ value: c.id, label: c.name })) },
    { name: 'priority', label: 'Prioridad', type: 'select', required: true, options: options(priority), cols: 4 },
    { name: 'location', label: 'Ubicación', required: true, maxLength: 200, cols: 12 },
    { name: 'latitude', label: 'Latitud', type: 'number', required: true, min: -90, max: 90, cols: 4 },
    { name: 'longitude', label: 'Longitud', type: 'number', required: true, min: -180, max: 180, cols: 4 },
    { name: 'targetAltitudeM', label: 'Altitud objetivo (m)', type: 'number', required: true, min: 5, max: 120, cols: 4, helperText: 'Máximo legal: 120 m' },
    {
      name: 'scheduledDate', label: 'Fecha programada', type: 'date', required: true, cols: 4,
      validate: (v, all) => (!all.id && String(v) < today() ? 'No puede ser una fecha pasada' : null),
    },
    { name: 'startTime', label: 'Hora de inicio', type: 'time', required: true, cols: 4 },
    {
      name: 'estimatedEndTime', label: 'Hora estimada de fin', type: 'time', required: true, cols: 4,
      validate: (v, all) => (all.startTime && String(v) <= String(all.startTime) ? 'Debe ser posterior a la hora de inicio' : null),
    },
    { name: 'objective', label: 'Objetivo', required: true, maxLength: 500, cols: 12 },
    { name: 'description', label: 'Descripción', type: 'textarea', maxLength: 1000, cols: 12 },
  ];
}

export const emptyMission = {
  type: 'INSPECCION', priority: 'MEDIA', latitude: 3.4516, longitude: -76.532, targetAltitudeM: 60,
  scheduledDate: today(), startTime: '09:00', estimatedEndTime: '10:00',
};

export const missionToForm = (m: Mission): Record<string, unknown> => ({
  ...m,
  clientId: m.client.id,
  startTime: m.startTime.slice(0, 5),
  estimatedEndTime: m.estimatedEndTime.slice(0, 5),
});
