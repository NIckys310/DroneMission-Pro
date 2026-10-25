# API REST

Documentación interactiva: **http://localhost:8080/swagger-ui.html** (botón *Authorize* con el token del login).

Formato de error común:

```json
{ "timestamp": "2026-10-25T10:15:00", "status": 400, "error": "Bad Request",
  "message": "El drone DRN-004 está en mantenimiento", "path": "/api/missions/15/assign" }
```

| Código | Significado |
|---|---|
| 400 | Datos inválidos (`fieldErrors`) o regla de negocio incumplida |
| 401 | Sin token / token vencido / credenciales incorrectas |
| 403 | El rol no tiene el permiso |
| 404 | Recurso inexistente |
| 409 | Transición de estado inválida o valor duplicado |
| 429 | Demasiados intentos de login/registro desde la misma IP |

## Endpoints

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | público | Iniciar sesión con correo **o** usuario (`identifier`) |
| POST | `/api/auth/register` | público | Crear cuenta (queda pendiente de aprobación) |
| GET | `/api/auth/config` | público | Registro habilitado, usuarios demo y política de contraseñas |
| GET | `/api/auth/me` | autenticado | Usuario actual |
| POST | `/api/auth/logout` | autenticado | Cerrar sesión (auditoría) |
| POST | `/api/auth/forgot-password` | público | Recuperación simulada |
| POST | `/api/auth/reset-password` | público | Restablecer contraseña |
| GET | `/api/dashboard` | DASHBOARD_VER | Indicadores y alertas |
| GET/POST | `/api/users` | USUARIOS_GESTIONAR | Listar / crear usuarios |
| GET/PUT/DELETE | `/api/users/{id}` | USUARIOS_GESTIONAR | Consultar / editar / desactivar |
| POST | `/api/users/{id}/approve` | USUARIOS_GESTIONAR | Aprobar cuenta y asignar rol |
| POST | `/api/users/{id}/reject` | USUARIOS_GESTIONAR | Rechazar cuenta pendiente |
| GET | `/api/roles` | USUARIOS_GESTIONAR | Roles con permisos |
| GET | `/api/permissions` | ROLES_GESTIONAR | Catálogo de permisos |
| PUT | `/api/roles/{id}/permissions` | ROLES_GESTIONAR | Cambiar permisos del rol |
| GET | `/api/drones?q=&status=&type=` | DRONES_VER | Listar / buscar / filtrar |
| POST | `/api/drones` | DRONES_GESTIONAR | Crear (Factory) |
| GET/PUT/DELETE | `/api/drones/{id}` | DRONES_VER / GESTIONAR | Detalle / editar / desactivar |
| GET | `/api/drones/{id}/history` | DRONES_VER | Misiones y mantenimientos |
| PATCH | `/api/drones/{id}/status` | DRONES_ESTADO_TECNICO | Estado técnico |
| POST | `/api/drones/{id}/charge` | DRONES_ESTADO_TECNICO | Cargar batería |
| GET/POST | `/api/pilots` | PILOTOS_VER / GESTIONAR | Listar / crear |
| GET | `/api/pilots/me` | rol PILOTO | Perfil del piloto |
| GET/PUT/DELETE | `/api/pilots/{id}` | PILOTOS_VER / GESTIONAR | Detalle / editar / desactivar |
| GET | `/api/pilots/{id}/missions` | PILOTOS_VER | Historial |
| GET/POST | `/api/clients` | CLIENTES_VER / GESTIONAR | Listar / crear |
| GET/PUT/DELETE | `/api/clients/{id}` | CLIENTES_VER / GESTIONAR | Detalle / editar / desactivar |
| GET | `/api/missions?status=&type=&clientId=&q=` | MISIONES_VER | Listar (según alcance del rol) |
| POST | `/api/missions` | MISIONES_GESTIONAR | Crear (Builder + plan de vuelo) |
| GET/PUT/DELETE | `/api/missions/{id}` | MISIONES_VER / GESTIONAR | Detalle / editar / eliminar borrador |
| GET | `/api/missions/{id}/flight-plan` | MISIONES_VER | Plan de vuelo y estimaciones |
| GET | `/api/missions/{id}/history` | MISIONES_VER | Auditoría de la misión |
| POST | `/api/missions/{id}/submit` | MISIONES_GESTIONAR | BORRADOR → PENDIENTE |
| GET | `/api/missions/{id}/assignment-options` | MISIONES_GESTIONAR | Recursos elegibles y motivos |
| POST | `/api/missions/{id}/assign` | MISIONES_GESTIONAR | Asignar drone y piloto |
| POST | `/api/missions/{id}/prepare` | MISIONES_EJECUTAR | Chequeo prevuelo |
| POST | `/api/missions/{id}/start` | MISIONES_EJECUTAR | Despegar |
| POST | `/api/missions/{id}/pause` | MISIONES_EJECUTAR | Pausar |
| POST | `/api/missions/{id}/resume` | MISIONES_EJECUTAR | Reanudar |
| POST | `/api/missions/{id}/finish` | MISIONES_EJECUTAR | Finalizar |
| POST | `/api/missions/{id}/cancel` | MISIONES_GESTIONAR | Cancelar |
| POST | `/api/missions/{id}/observations` | EJECUTAR o GESTIONAR | Registrar observación |
| GET | `/api/missions/{id}/telemetry` | MISIONES_VER | Telemetría simulada |
| GET/POST | `/api/missions/{id}/incidents` | MISIONES_VER / INCIDENTES_REGISTRAR | Incidentes de la misión |
| GET/POST | `/api/missions/{id}/evidences` | MISIONES_VER / EVIDENCIAS_REGISTRAR | Evidencias (JSON o multipart) |
| GET | `/api/evidences/{id}/file` | MISIONES_VER | Descargar archivo |
| DELETE | `/api/evidences/{id}` | MISIONES_GESTIONAR | Eliminar evidencia |
| GET | `/api/incidents?severity=&status=&type=` | INCIDENTES_VER | Listar incidentes |
| PATCH | `/api/incidents/{id}/status` | INCIDENTES_GESTIONAR | Cambiar estado |
| GET/POST | `/api/maintenance` | MANTENIMIENTO_VER / GESTIONAR | Listar / registrar |
| GET/PUT | `/api/maintenance/{id}` | MANTENIMIENTO_VER / GESTIONAR | Detalle / editar |
| POST | `/api/maintenance/{id}/start` | MANTENIMIENTO_GESTIONAR | Iniciar (drone EN_MANTENIMIENTO) |
| POST | `/api/maintenance/{id}/complete` | MANTENIMIENTO_GESTIONAR | Completar (drone DISPONIBLE) |
| POST | `/api/maintenance/{id}/cancel` | MANTENIMIENTO_GESTIONAR | Cancelar |
| GET | `/api/reports` | REPORTES_VER | Historial de reportes |
| GET | `/api/reports/missions/{id}` y `/pdf` | REPORTES_VER | Reporte de misión |
| GET | `/api/reports/drones` y `/pdf` | REPORTES_GENERAR | Reporte de drones |
| GET | `/api/reports/pilots` y `/pdf` | REPORTES_GENERAR | Reporte de pilotos |
| GET | `/api/reports/maintenance` y `/pdf` | REPORTES_GENERAR | Reporte de mantenimiento |
| GET | `/api/audit?limit=` | AUDITORIA_VER | Bitácora |
| GET | `/api/notifications` | autenticado | Mis notificaciones |
| PATCH | `/api/notifications/{id}/read` | autenticado | Marcar como leída |
| POST | `/api/notifications/read-all` | autenticado | Marcar todas |
