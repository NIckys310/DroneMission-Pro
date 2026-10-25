import { http } from './client';
import type {
  AssignmentOptions, AuditEntry, Client, DashboardData, Drone, DroneHistory, DroneReport, Evidence, FlightPlan,
  Incident, LoginResponse, Maintenance, MaintenanceReport, Mission, MissionReport, Notification, Permission, Pilot,
  PilotMissionEntry, PilotReport, ReportEntry, Role, TelemetryFeed, User, UserInfo,
} from '../types';

/*
 * Todos los endpoints del backend que usa el frontend, agrupados por módulo.
 * Cada función corresponde a un método de un controller de Spring Boot.
 */

type Params = Record<string, string | number | undefined>;
const clean = (params?: Params) =>
  Object.fromEntries(Object.entries(params ?? {}).filter(([, v]) => v !== undefined && v !== ''));
const data = <T,>(promise: Promise<{ data: T }>) => promise.then((r) => r.data);

export const authApi = {
  login: (email: string, password: string) => data<LoginResponse>(http.post('/auth/login', { email, password })),
  me: () => data<UserInfo>(http.get('/auth/me')),
  logout: () => http.post('/auth/logout'),
  forgotPassword: (email: string) =>
    data<{ message: string; simulatedToken: string | null }>(http.post('/auth/forgot-password', { email })),
  resetPassword: (token: string, newPassword: string) =>
    data<{ message: string }>(http.post('/auth/reset-password', { token, newPassword })),
};

export const dashboardApi = {
  get: () => data<DashboardData>(http.get('/dashboard')),
};

export const droneApi = {
  list: (params?: Params) => data<Drone[]>(http.get('/drones', { params: clean(params) })),
  get: (id: number) => data<Drone>(http.get(`/drones/${id}`)),
  history: (id: number) => data<DroneHistory>(http.get(`/drones/${id}/history`)),
  create: (body: unknown) => data<Drone>(http.post('/drones', body)),
  update: (id: number, body: unknown) => data<Drone>(http.put(`/drones/${id}`, body)),
  changeStatus: (id: number, status: string, reason?: string) => data<Drone>(http.patch(`/drones/${id}/status`, { status, reason })),
  charge: (id: number) => data<Drone>(http.post(`/drones/${id}/charge`)),
  deactivate: (id: number) => http.delete(`/drones/${id}`),
};

export const pilotApi = {
  list: (params?: Params) => data<Pilot[]>(http.get('/pilots', { params: clean(params) })),
  get: (id: number) => data<Pilot>(http.get(`/pilots/${id}`)),
  me: () => data<Pilot>(http.get('/pilots/me')),
  missions: (id: number) => data<PilotMissionEntry[]>(http.get(`/pilots/${id}/missions`)),
  create: (body: unknown) => data<Pilot>(http.post('/pilots', body)),
  update: (id: number, body: unknown) => data<Pilot>(http.put(`/pilots/${id}`, body)),
  deactivate: (id: number) => http.delete(`/pilots/${id}`),
};

export const clientApi = {
  list: (params?: Params) => data<Client[]>(http.get('/clients', { params: clean(params) })),
  get: (id: number) => data<Client>(http.get(`/clients/${id}`)),
  create: (body: unknown) => data<Client>(http.post('/clients', body)),
  update: (id: number, body: unknown) => data<Client>(http.put(`/clients/${id}`, body)),
  deactivate: (id: number) => http.delete(`/clients/${id}`),
};

export const missionApi = {
  list: (params?: Params) => data<Mission[]>(http.get('/missions', { params: clean(params) })),
  get: (id: number) => data<Mission>(http.get(`/missions/${id}`)),
  create: (body: unknown) => data<Mission>(http.post('/missions', body)),
  update: (id: number, body: unknown) => data<Mission>(http.put(`/missions/${id}`, body)),
  remove: (id: number) => http.delete(`/missions/${id}`),
  flightPlan: (id: number) => data<FlightPlan>(http.get(`/missions/${id}/flight-plan`)),
  history: (id: number) => data<AuditEntry[]>(http.get(`/missions/${id}/history`)),
  telemetry: (id: number) => data<TelemetryFeed>(http.get(`/missions/${id}/telemetry`)),
  assignmentOptions: (id: number) => data<AssignmentOptions>(http.get(`/missions/${id}/assignment-options`)),
  submit: (id: number) => data<Mission>(http.post(`/missions/${id}/submit`)),
  assign: (id: number, droneId: number, pilotId: number) => data<Mission>(http.post(`/missions/${id}/assign`, { droneId, pilotId })),
  cancel: (id: number, reason: string) => data<Mission>(http.post(`/missions/${id}/cancel`, { reason })),
  prepare: (id: number) => data<Mission>(http.post(`/missions/${id}/prepare`)),
  start: (id: number) => data<Mission>(http.post(`/missions/${id}/start`)),
  pause: (id: number, text?: string) => data<Mission>(http.post(`/missions/${id}/pause`, { text })),
  resume: (id: number) => data<Mission>(http.post(`/missions/${id}/resume`)),
  finish: (id: number, text?: string) => data<Mission>(http.post(`/missions/${id}/finish`, { text })),
  observe: (id: number, text: string) => data<Mission>(http.post(`/missions/${id}/observations`, { text })),
  incidents: (id: number) => data<Incident[]>(http.get(`/missions/${id}/incidents`)),
  addIncident: (id: number, body: unknown) => data<Incident>(http.post(`/missions/${id}/incidents`, body)),
  evidences: (id: number) => data<Evidence[]>(http.get(`/missions/${id}/evidences`)),
  addEvidence: (id: number, form: FormData) => data<Evidence>(http.post(`/missions/${id}/evidences`, form)),
};

export const evidenceApi = {
  remove: (id: number) => http.delete(`/evidences/${id}`),
};

export const incidentApi = {
  list: (params?: Params) => data<Incident[]>(http.get('/incidents', { params: clean(params) })),
  changeStatus: (id: number, status: string, resolution?: string) =>
    data<Incident>(http.patch(`/incidents/${id}/status`, { status, resolution })),
};

export const maintenanceApi = {
  list: (params?: Params) => data<Maintenance[]>(http.get('/maintenance', { params: clean(params) })),
  create: (body: unknown) => data<Maintenance>(http.post('/maintenance', body)),
  update: (id: number, body: unknown) => data<Maintenance>(http.put(`/maintenance/${id}`, body)),
  start: (id: number) => data<Maintenance>(http.post(`/maintenance/${id}/start`)),
  complete: (id: number, body: unknown) => data<Maintenance>(http.post(`/maintenance/${id}/complete`, body)),
  cancel: (id: number) => data<Maintenance>(http.post(`/maintenance/${id}/cancel`)),
};

export const reportApi = {
  history: () => data<ReportEntry[]>(http.get('/reports')),
  mission: (id: number) => data<MissionReport>(http.get(`/reports/missions/${id}`)),
  drones: () => data<DroneReport>(http.get('/reports/drones')),
  pilots: () => data<PilotReport>(http.get('/reports/pilots')),
  maintenance: () => data<MaintenanceReport>(http.get('/reports/maintenance')),
};

export const userApi = {
  list: () => data<User[]>(http.get('/users')),
  create: (body: unknown) => data<User>(http.post('/users', body)),
  update: (id: number, body: unknown) => data<User>(http.put(`/users/${id}`, body)),
  deactivate: (id: number) => http.delete(`/users/${id}`),
  roles: () => data<Role[]>(http.get('/roles')),
  permissions: () => data<Permission[]>(http.get('/permissions')),
  updateRolePermissions: (id: number, permissions: string[]) => data<Role>(http.put(`/roles/${id}/permissions`, { permissions })),
};

export const auditApi = {
  list: (limit = 300) => data<AuditEntry[]>(http.get('/audit', { params: { limit } })),
};

export const notificationApi = {
  list: () => data<Notification[]>(http.get('/notifications')),
  unreadCount: () => data<{ count: number }>(http.get('/notifications/unread-count')),
  markRead: (id: number) => http.patch(`/notifications/${id}/read`),
  markAllRead: () => http.post('/notifications/read-all'),
};
