# Matriz de trazabilidad

Relaciona cada requerimiento con su caso de uso, módulo, endpoint, entidad (tabla) y prueba.
Las pruebas CP-xxx están descritas en [testing/casos-de-prueba.md](testing/casos-de-prueba.md) y automatizadas en `backend/src/test`.

| Requerimiento | Caso de uso | Módulo | Endpoint | Entidad / tabla | Prueba |
|---|---|---|---|---|---|
| RF-001 Iniciar sesión | CU-001 | Autenticación | `POST /api/auth/login` | usuarios, auditoria | CP-001, CP-002 |
| RF-002 Cerrar sesión | CU-001 | Autenticación | `POST /api/auth/logout` | auditoria | CP-003 |
| RF-003 Recuperar contraseña (simulado) | CU-001 | Autenticación | `POST /api/auth/forgot-password, /reset-password` | usuarios | CP-003 |
| RF-004 Gestionar usuarios | CU-002 | Usuarios | `GET/POST/PUT/DELETE /api/users` | usuarios, roles | CP-017 |
| RF-005 Gestionar roles y permisos | CU-002 | Usuarios | `GET /api/roles, PUT /api/roles/{id}/permissions` | roles, permisos, rol_permisos | CP-017 |
| RF-006 Registrar y editar drones | CU-003 | Drones | `POST/PUT /api/drones` | drones | CP-004 |
| RF-007 Consultar, buscar y filtrar drones | CU-003 | Drones | `GET /api/drones, /{id}, /{id}/history` | drones, asignaciones_mision, mantenimientos | CP-004 |
| RF-008 Desactivar drones | CU-003 | Drones | `DELETE /api/drones/{id}` | drones | CP-004 |
| RF-009 Actualizar estado técnico y batería | CU-014 | Drones | `PATCH /api/drones/{id}/status, POST /{id}/charge` | drones | CP-015 |
| RF-010 Gestionar pilotos | CU-004 | Pilotos | `GET/POST/PUT/DELETE /api/pilots` | pilotos, certificaciones_piloto | CP-005 |
| RF-011 Controlar vigencia de licencias | CU-004 | Pilotos | `(tarea programada)` | pilotos | CP-005 |
| RF-012 Gestionar clientes | CU-005 | Clientes | `GET/POST/PUT/DELETE /api/clients` | clientes | CP-017 |
| RF-013 Crear misión | CU-006 | Misiones | `POST /api/missions` | misiones, puntos_ruta | CP-006, CP-007 |
| RF-014 Editar y eliminar misión | CU-006 | Misiones | `PUT/DELETE /api/missions/{id}` | misiones | CP-014 |
| RF-015 Programar misión | CU-007 | Misiones | `POST /api/missions/{id}/submit` | misiones | CP-010 |
| RF-016 Asignar drone y piloto | CU-008 | Misiones | `GET /assignment-options, POST /api/missions/{id}/assign` | asignaciones_mision | CP-008, CP-009 |
| RF-017 Preparar e iniciar misión | CU-009 | Misiones | `POST /prepare, POST /start` | misiones, drones, pilotos | CP-011 |
| RF-018 Pausar y reanudar misión | CU-010 | Misiones | `POST /pause, POST /resume` | misiones | CP-012 |
| RF-019 Finalizar misión | CU-011 | Misiones | `POST /api/missions/{id}/finish` | misiones, reportes | CP-013 |
| RF-020 Cancelar misión | CU-018 | Misiones | `POST /api/missions/{id}/cancel` | misiones | CP-014 |
| RF-021 Simular telemetría | CU-017 | Telemetría | `(tarea programada)` | telemetria | CP-013 |
| RF-022 Monitorear telemetría | CU-017 | Telemetría | `GET /api/missions/{id}/telemetry` | telemetria, puntos_ruta | CP-013 |
| RF-023 Registrar incidente | CU-012 | Incidentes | `POST /api/missions/{id}/incidents` | incidentes | CP-016 |
| RF-024 Gestionar incidentes | CU-012 | Incidentes | `GET /api/incidents, PATCH /{id}/status` | incidentes | CP-016 |
| RF-025 Registrar evidencia | CU-013 | Evidencias | `POST /api/missions/{id}/evidences` | evidencias | CP-013 |
| RF-026 Gestionar mantenimiento | CU-014 | Mantenimiento | `GET/POST/PUT /api/maintenance, /start, /complete, /cancel` | mantenimientos, drones | CP-015 |
| RF-027 Generar reportes | CU-015 | Reportes | `GET /api/reports/**` | reportes | CP-013 |
| RF-028 Consultar dashboard | CU-016 | Dashboard | `GET /api/dashboard` | todas | CP-017 |
| RF-029 Auditoría | CU-016 | Auditoría | `GET /api/audit, GET /api/missions/{id}/history` | auditoria | CP-013 |
| RF-031 Registrarse | CU-019 | Autenticación | `POST /api/auth/register` | usuarios, notificaciones | CP-019, CP-020 |
| RF-032 Aprobar o rechazar cuentas | CU-020 | Usuarios | `POST /api/users/{id}/approve, /reject` | usuarios, pilotos, clientes | CP-019 |
| RF-033 Administrador propietario | CU-020 | Configuración | `(arranque)` | usuarios | CP-022 |
| RF-034 Simulador 3D de vuelo | CU-021 | Simulador | `GET /api/simulator/plan` | — | CP-023, CP-024 |
| RF-035 Instalar como aplicación | CU-021 | PWA | `manifest.json, sw.js` | — | CP-025 |
| RF-030 Notificaciones | CU-011 | Notificaciones | `GET /api/notifications, PATCH /{id}/read` | notificaciones | CP-013 |
