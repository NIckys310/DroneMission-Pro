# Casos de prueba documentados

| ID | Caso | Datos / pasos | Resultado esperado | Automatizada en |
|---|---|---|---|---|
| CP-001 | Login correcto (correo o usuario) | admin@dronemission.local / Demo2026! · operador / Demo2026! | 200, token y rol | `AuthIntegrationTest.loginOk`, `loginWithUsername` |
| CP-002 | Login incorrecto | contraseña errónea; identificador vacío | 401 mensaje genérico; 400 `fieldErrors.identifier` | `AuthIntegrationTest.wrongPassword`, `blankIdentifier` |
| CP-003 | Acceso sin token | GET /api/auth/me sin cabecera | 401; con token 200 | `AuthIntegrationTest.meRequiresToken` |
| CP-004 | CRUD de drones | crear sin código → DRN-009; editar; desactivar; serie duplicada | 201 / 200 / 204 / 409 | `DroneCrudIntegrationTest` |
| CP-005 | CRUD de pilotos | crear con certificación; licencia vencida; licencia duplicada | DISPONIBLE / LICENCIA_VENCIDA / 409 | `PilotCrudIntegrationTest` |
| CP-006 | Crear misión | datos válidos | 201, BORRADOR, plan de 18 puntos (inspección) | `MissionFlowIntegrationTest.fullLifecycle` |
| CP-007 | Validaciones del Builder | fin < inicio, fecha pasada, altitud 150 m | BusinessRuleException | `MissionBuilderTest` |
| CP-008 | Asignar recurso inválido | drone DRN-004 (mantenimiento), piloto Diego (licencia vencida), DRN-007 (45 % batería) | 400 con el motivo | `MissionFlowIntegrationTest`, `AssignmentRulesTest` |
| CP-009 | Cruce de horario | misión 10:00–10:30 hoy con DRN-008 (ocupado en MSN-0003) | 400 "ya tiene otra misión en ese horario" | `MissionFlowIntegrationTest.overlapIsRejected` |
| CP-010 | Transiciones válidas | BORRADOR→PENDIENTE→PROGRAMADA→EN_PREPARACION→EN_VUELO↔PAUSADA→FINALIZADA | 200 en cada paso | `MissionStatusTest`, `fullLifecycle` |
| CP-011 | Transiciones inválidas | despegar sin chequeo; preparar una finalizada | 409 | `fullLifecycle` |
| CP-012 | Pausar y reanudar | piloto asignado | PAUSADA → EN_VUELO | `fullLifecycle` |
| CP-013 | Telemetría, incidente, evidencia, fin y reporte | 2 ticks del simulador; incidente; evidencia URL; finalizar | 2 lecturas simuladas; reporte con 1 incidente y 1 evidencia; 8 registros de historial | `fullLifecycle` |
| CP-014 | Cancelar / eliminar | cancelar MSN-0004 e intentar prepararla; borrar PENDIENTE y BORRADOR | 409 / 409 / 204 | `cancelledCannotStart`, `deleteOnlyDrafts` |
| CP-015 | Mantenimiento | registrar y luego iniciar y completar sobre DRN-005 | EN_MANTENIMIENTO → DISPONIBLE, batería 100 % | `MaintenanceAndIncidentIntegrationTest` |
| CP-016 | Incidentes | piloto registra en su misión; en una ajena; operador resuelve | 201 / 403 / RESUELTO | `MaintenanceAndIncidentIntegrationTest.incidents` |
| CP-017 | Control de permisos | 13 combinaciones rol × endpoint | 200 o 403 según la matriz | `PermissionIntegrationTest` |
| CP-019 | Registro y aprobación | registrar `lmejia`; intentar entrar; aprobar como OPERADOR (o PILOTO vinculando ficha); entrar | 201 / 401 pendiente / 200 / 200 | `RegistrationSecurityIntegrationTest.registerApproveLogin`, `approveLinkingPilotFromInactiveAccount` |
| CP-020 | Validaciones del registro | contraseña `12345678`; usuario repetido; confirmación distinta | 400 / 409 / 400 | `registrationValidations` |
| CP-021 | Bloqueo por fuerza bruta | 5 contraseñas erróneas y luego la correcta | 401 "Cuenta bloqueada…" | `lockout` |
| CP-022 | Configuración pública y límite por IP | GET /api/auth/config; 25 logins seguidos | sin datos sensibles; 429 al superar 20/min | `publicConfig` + prueba manual |
| CP-018 | Alcance de datos | cliente y piloto listan misiones | cliente 3 (sus misiones), piloto 3; misión ajena 403 | `scopedVisibility` |

## Guion de prueba manual (demo)

1. **Administrador:** ver el dashboard y las alertas → crear un drone → crear un piloto.
2. **Operador:** crear una misión de inspección para hoy → enviar a planificación → asignar DRN-001 y Andrés Torres (ver los motivos de los no elegibles).
3. **Piloto (piloto@):** abrir la misión → chequeo prevuelo → despegar → pestaña *Vuelo y telemetría* (mapa en vivo) → registrar incidente y evidencia → finalizar.
4. **Cliente:** ver la misión finalizada y descargar el PDF.
5. **Técnico:** registrar mantenimiento de DRN-005 con "Iniciar ahora" → completar.
6. **Administrador:** revisar auditoría y reportes.
