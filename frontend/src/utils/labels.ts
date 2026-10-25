// Textos en español y colores para cada valor de los enums del backend.

export type ChipColor = 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'info';

interface Meta { label: string; color: ChipColor }

const meta = (label: string, color: ChipColor = 'default'): Meta => ({ label, color });

export const missionStatus: Record<string, Meta> = {
  BORRADOR: meta('Borrador'),
  PENDIENTE: meta('Pendiente', 'secondary'),
  PROGRAMADA: meta('Programada', 'info'),
  EN_PREPARACION: meta('En preparación', 'warning'),
  EN_VUELO: meta('En vuelo', 'primary'),
  PAUSADA: meta('Pausada', 'warning'),
  FINALIZADA: meta('Finalizada', 'success'),
  CANCELADA: meta('Cancelada', 'error'),
};

export const droneStatus: Record<string, Meta> = {
  DISPONIBLE: meta('Disponible', 'success'),
  EN_MISION: meta('En misión', 'primary'),
  EN_MANTENIMIENTO: meta('En mantenimiento', 'warning'),
  FUERA_DE_SERVICIO: meta('Fuera de servicio', 'error'),
};

export const pilotStatus: Record<string, Meta> = {
  DISPONIBLE: meta('Disponible', 'success'),
  EN_MISION: meta('En misión', 'primary'),
  NO_DISPONIBLE: meta('No disponible'),
  LICENCIA_VENCIDA: meta('Licencia vencida', 'error'),
};

export const maintenanceStatus: Record<string, Meta> = {
  PROGRAMADO: meta('Programado', 'info'),
  EN_PROCESO: meta('En proceso', 'warning'),
  COMPLETADO: meta('Completado', 'success'),
  CANCELADO: meta('Cancelado', 'error'),
};

export const incidentStatus: Record<string, Meta> = {
  ABIERTO: meta('Abierto', 'error'),
  EN_REVISION: meta('En revisión', 'warning'),
  RESUELTO: meta('Resuelto', 'success'),
};

export const severity: Record<string, Meta> = {
  BAJA: meta('Baja', 'default'),
  MEDIA: meta('Media', 'info'),
  ALTA: meta('Alta', 'warning'),
  CRITICA: meta('Crítica', 'error'),
};

export const priority = severity;

export const droneType: Record<string, string> = {
  INSPECCION: 'Inspección', AGRICULTURA: 'Agricultura', VIGILANCIA: 'Vigilancia', TOPOGRAFIA: 'Topografía', FOTOGRAFIA: 'Fotografía',
};

export const missionType: Record<string, string> = { ...droneType, MONITOREO: 'Monitoreo' };

export const incidentType: Record<string, string> = {
  BATERIA_BAJA: 'Batería baja', PERDIDA_SENAL: 'Pérdida de señal', OBSTACULO: 'Obstáculo detectado',
  ERROR_TECNICO: 'Error técnico', CLIMA: 'Condiciones climáticas', ATERRIZAJE_EMERGENCIA: 'Aterrizaje de emergencia', OTRO: 'Otro',
};

export const evidenceType: Record<string, string> = {
  FOTOGRAFIA: 'Fotografía', VIDEO: 'Video', DOCUMENTO: 'Documento', ARCHIVO: 'Archivo', OBSERVACION: 'Observación',
};

export const maintenanceType: Record<string, string> = {
  PREVENTIVO: 'Preventivo', CORRECTIVO: 'Correctivo', CALIBRACION: 'Calibración', REPARACION: 'Reparación', INSPECCION: 'Inspección',
};

export const roleName: Record<string, string> = {
  ADMINISTRADOR: 'Administrador', OPERADOR: 'Operador', PILOTO: 'Piloto', TECNICO: 'Técnico', CLIENTE: 'Cliente',
};

export const clientType: Record<string, string> = { PERSONA: 'Persona natural', EMPRESA: 'Empresa' };

export const waypointAction: Record<string, string> = {
  DESPEGUE: 'Despegue', PASO: 'Paso', FOTO: 'Foto', VIDEO: 'Video', ESCANEO: 'Escaneo', FUMIGACION: 'Fumigación', ATERRIZAJE: 'Aterrizaje',
};

/** Convierte un diccionario de etiquetas en opciones para un <select>. */
export function options(dict: Record<string, string | Meta>): { value: string; label: string }[] {
  return Object.entries(dict).map(([value, v]) => ({ value, label: typeof v === 'string' ? v : v.label }));
}
