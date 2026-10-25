// Tipos que reflejan exactamente los DTOs del backend (paquetes */dto).

export type RoleName = 'ADMINISTRADOR' | 'OPERADOR' | 'PILOTO' | 'TECNICO' | 'CLIENTE';

export interface UserInfo {
  id: number;
  fullName: string;
  email: string;
  role: RoleName;
  permissions: string[];
  pilotId: number | null;
  clientId: number | null;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresInMinutes: number;
  user: UserInfo;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: Record<string, string>;
}

export type DroneType = 'INSPECCION' | 'AGRICULTURA' | 'VIGILANCIA' | 'TOPOGRAFIA' | 'FOTOGRAFIA';
export type DroneStatus = 'DISPONIBLE' | 'EN_MISION' | 'EN_MANTENIMIENTO' | 'FUERA_DE_SERVICIO';

export interface Drone {
  id: number;
  code: string;
  name: string;
  manufacturer: string;
  model: string;
  serialNumber: string;
  type: DroneType;
  status: DroneStatus;
  batteryCapacityMah: number;
  batteryLevel: number;
  flightHours: number;
  maxSpeedKmh: number;
  autonomyMinutes: number;
  acquiredOn: string;
  lastMaintenanceOn: string | null;
  nextMaintenanceOn: string | null;
  maintenanceDue: boolean;
  description: string | null;
  active: boolean;
}

export interface DroneHistory {
  missions: { missionId: number; code: string; name: string; status: MissionStatus; scheduledDate: string; pilotName: string; assignedAt: string }[];
  maintenance: { id: number; type: string; status: string; date: string; description: string; cost: number; technicianName: string }[];
}

export type PilotStatus = 'DISPONIBLE' | 'EN_MISION' | 'NO_DISPONIBLE' | 'LICENCIA_VENCIDA';

export interface Certification {
  id?: number | null;
  name: string;
  issuer: string;
  issuedOn: string;
  expiresOn: string | null;
}

export interface Pilot {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
  document: string;
  email: string;
  phone: string;
  licenseNumber: string;
  licenseExpiresOn: string;
  licenseValid: boolean;
  flightHours: number;
  status: PilotStatus;
  active: boolean;
  userEmail: string | null;
  certifications: Certification[];
  missionCount: number;
}

export interface PilotMissionEntry {
  missionId: number;
  code: string;
  name: string;
  status: MissionStatus;
  scheduledDate: string;
  droneCode: string;
}

export interface Client {
  id: number;
  type: 'PERSONA' | 'EMPRESA';
  name: string;
  document: string;
  email: string;
  phone: string;
  address: string;
  city: string;
  active: boolean;
  missionCount: number;
  userEmail: string | null;
}

export type MissionType = 'INSPECCION' | 'AGRICULTURA' | 'VIGILANCIA' | 'TOPOGRAFIA' | 'FOTOGRAFIA' | 'MONITOREO';
export type MissionPriority = 'BAJA' | 'MEDIA' | 'ALTA' | 'CRITICA';
export type MissionStatus =
  | 'BORRADOR' | 'PENDIENTE' | 'PROGRAMADA' | 'EN_PREPARACION' | 'EN_VUELO' | 'PAUSADA' | 'FINALIZADA' | 'CANCELADA';
export type MissionAction =
  | 'EDIT' | 'DELETE' | 'SUBMIT' | 'ASSIGN' | 'CANCEL' | 'PREPARE' | 'START' | 'PAUSE' | 'RESUME' | 'FINISH'
  | 'OBSERVE' | 'INCIDENT' | 'EVIDENCE' | 'REPORT';

export interface Mission {
  id: number;
  code: string;
  name: string;
  description: string | null;
  type: MissionType;
  client: { id: number; name: string };
  location: string;
  latitude: number;
  longitude: number;
  targetAltitudeM: number;
  scheduledDate: string;
  startTime: string;
  estimatedEndTime: string;
  priority: MissionPriority;
  status: MissionStatus;
  objective: string;
  observations: string | null;
  drone: { id: number; code: string; name: string; type: DroneType; status: DroneStatus; batteryLevel: number } | null;
  pilot: { id: number; fullName: string; status: PilotStatus; licenseExpiresOn: string } | null;
  createdBy: string;
  actualStart: string | null;
  actualEnd: string | null;
  createdAt: string;
  availableActions: MissionAction[];
}

export interface Waypoint {
  sequence: number;
  latitude: number;
  longitude: number;
  altitudeM: number;
  action: string;
}

export interface FlightPlan {
  strategy: string;
  minimumBatteryPercent: number;
  compatibleDroneTypes: DroneType[];
  cruiseSpeedKmh: number;
  estimatedDistanceM: number;
  estimatedDurationMin: number;
  estimatedBatteryUsePercent: number;
  waypoints: Waypoint[];
}

export interface AssignmentOptions {
  minimumBatteryPercent: number;
  drones: { id: number; code: string; name: string; type: DroneType; status: DroneStatus; batteryLevel: number; eligible: boolean; reason: string | null }[];
  pilots: { id: number; fullName: string; status: PilotStatus; licenseExpiresOn: string; eligible: boolean; reason: string | null }[];
}

export interface TelemetryPoint {
  id: number;
  recordedAt: string;
  latitude: number;
  longitude: number;
  altitudeM: number;
  speedKmh: number;
  battery: number;
  distanceM: number;
  temperatureC: number;
  flightTimeS: number;
  currentWaypoint: number;
  simulated: boolean;
}

export interface TelemetrySummary {
  readings: number;
  maxAltitudeM: number;
  avgSpeedKmh: number;
  maxSpeedKmh: number;
  distanceM: number;
  flightTimeS: number;
  startBattery: number;
  endBattery: number;
  batteryUsed: number;
  maxTemperatureC: number;
}

export interface TelemetryFeed {
  simulated: boolean;
  notice: string;
  missionStatus: MissionStatus;
  live: boolean;
  routeCompleted: boolean;
  totalWaypoints: number;
  summary: TelemetrySummary;
  points: TelemetryPoint[];
}

export type IncidentType =
  | 'BATERIA_BAJA' | 'PERDIDA_SENAL' | 'OBSTACULO' | 'ERROR_TECNICO' | 'CLIMA' | 'ATERRIZAJE_EMERGENCIA' | 'OTRO';
export type Severity = 'BAJA' | 'MEDIA' | 'ALTA' | 'CRITICA';
export type IncidentStatus = 'ABIERTO' | 'EN_REVISION' | 'RESUELTO';

export interface Incident {
  id: number;
  missionId: number;
  missionCode: string;
  missionName: string;
  date: string;
  type: IncidentType;
  severity: Severity;
  description: string;
  reportedBy: string;
  status: IncidentStatus;
  resolution: string | null;
}

export type EvidenceType = 'FOTOGRAFIA' | 'VIDEO' | 'DOCUMENTO' | 'ARCHIVO' | 'OBSERVACION';

export interface Evidence {
  id: number;
  missionId: number;
  type: EvidenceType;
  name: string;
  url: string;
  storedFile: boolean;
  description: string | null;
  visibleToClient: boolean;
  uploadedBy: string;
  date: string;
}

export type MaintenanceType = 'PREVENTIVO' | 'CORRECTIVO' | 'CALIBRACION' | 'REPARACION' | 'INSPECCION';
export type MaintenanceStatus = 'PROGRAMADO' | 'EN_PROCESO' | 'COMPLETADO' | 'CANCELADO';

export interface Maintenance {
  id: number;
  droneId: number;
  droneCode: string;
  droneName: string;
  droneStatus: DroneStatus;
  technicianId: number;
  technicianName: string;
  type: MaintenanceType;
  date: string;
  description: string;
  cost: number;
  status: MaintenanceStatus;
  nextDate: string | null;
  observations: string | null;
  startedAt: string | null;
  completedAt: string | null;
}

export interface ReportEntry {
  id: number;
  type: 'MISION' | 'DRONES' | 'PILOTOS' | 'MANTENIMIENTO';
  title: string;
  missionId: number | null;
  missionCode: string | null;
  generatedBy: string;
  generatedAt: string;
  summary: string;
}

export interface MissionReport {
  missionId: number;
  code: string;
  name: string;
  type: MissionType;
  status: MissionStatus;
  durationMinutes: number | null;
  client: { name: string; type: string; document: string; email: string; phone: string; city: string };
  drone: { code: string; name: string; manufacturer: string; model: string; type: string } | null;
  pilot: { fullName: string; licenseNumber: string; licenseExpiresOn: string } | null;
  telemetry: TelemetrySummary;
  incidents: Incident[];
  evidences: Evidence[];
}

export interface DroneReport {
  total: number; available: number; inMission: number; inMaintenance: number; outOfService: number; totalFlightHours: number;
  drones: { code: string; name: string; model: string; type: DroneType; status: DroneStatus; batteryLevel: number; flightHours: number; nextMaintenanceOn: string | null }[];
}

export interface PilotReport {
  total: number; available: number; expiredLicenses: number; totalFlightHours: number;
  pilots: { fullName: string; licenseNumber: string; licenseExpiresOn: string; licenseValid: boolean; status: PilotStatus; flightHours: number; missions: number; finishedMissions: number }[];
}

export interface MaintenanceReport {
  total: number; scheduled: number; inProgress: number; completed: number; cancelled: number; totalCost: number;
  costByDrone: { droneCode: string; droneName: string; count: number; cost: number }[];
  history: { date: string; droneCode: string; type: MaintenanceType; status: MaintenanceStatus; technician: string; cost: number; description: string }[];
}

export interface DashboardData {
  drones: { total: number; available: number; inMission: number; inMaintenance: number; outOfService: number; totalFlightHours: number };
  pilots: { total: number; available: number; inMission: number; expiredLicenses: number };
  missionsByStatus: Record<MissionStatus, number>;
  missionsByType: { label: MissionType; count: number }[];
  flightHoursByDrone: { drone: string; hours: number }[];
  missionsPerDay: { date: string; scheduled: number; finished: number }[];
  activeMissions: DashboardMission[];
  upcomingMissions: DashboardMission[];
  recentIncidents: Incident[];
  pendingMaintenance: Maintenance[];
  alerts: { level: 'info' | 'warning' | 'error'; message: string; link: string }[];
  openIncidents: number;
}

export interface DashboardMission {
  id: number; code: string; name: string; status: MissionStatus; type: MissionType; date: string; startTime: string;
  clientName: string; droneCode: string | null; pilotName: string | null;
}

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
  username: string;
  email: string;
  role: RoleName;
  active: boolean;
  pendingApproval: boolean;
  lockedUntil: string | null;
  pilotId: number | null;
  clientId: number | null;
  lastLoginAt: string | null;
  createdAt: string;
}

export interface Role {
  id: number;
  name: RoleName;
  description: string;
  permissions: string[];
}

export interface Permission {
  id: number;
  code: string;
  description: string;
}

export interface AuditEntry {
  id: number;
  userEmail: string;
  action: string;
  entity: string;
  entityId: number | null;
  description: string;
  date: string;
}

export interface Notification {
  id: number;
  title: string;
  message: string;
  missionId: number | null;
  read: boolean;
  date: string;
}

export interface AuthConfig {
  demoUsersEnabled: boolean;
  registrationEnabled: boolean;
  showResetToken: boolean;
  passwordPolicy: string;
}
