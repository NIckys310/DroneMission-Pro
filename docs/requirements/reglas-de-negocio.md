# Reglas de negocio

| # | Regla | Dónde se implementa | Prueba |
|---|---|---|---|
| RN-01 | Un drone en mantenimiento no puede asignarse. | `AssignmentService.droneIneligibility` | `AssignmentRulesTest.droneInMaintenance`, `MissionFlowIntegrationTest` |
| RN-02 | Un drone fuera de servicio no puede asignarse. | `AssignmentService.droneIneligibility` | `AssignmentRulesTest.droneOutOfService` |
| RN-03 | Un piloto con licencia vencida no puede ser asignado ni iniciar una misión. | `AssignmentService.pilotIneligibility`, `validateReadyForFlight` | `AssignmentRulesTest.expiredLicense` |
| RN-04 | Un piloto no puede tener dos misiones simultáneas (cruce de fecha y horario). | `MissionAssignmentRepository.pilotHasOverlap` | `AssignmentRulesTest.pilotOverlap` |
| RN-05 | Un drone no puede tener dos misiones simultáneas. | `MissionAssignmentRepository.droneHasOverlap` | `MissionFlowIntegrationTest.overlapIsRejected` |
| RN-06 | Una misión no puede iniciarse sin piloto. | `AssignmentService.validateReadyForFlight` | `MissionFlowIntegrationTest` |
| RN-07 | Una misión no puede iniciarse sin drone. | `AssignmentService.validateReadyForFlight` | `MissionFlowIntegrationTest` |
| RN-08 | Una misión finalizada no puede reabrirse. | `MissionStatus` (máquina de estados) | `MissionStatusTest`, `MissionFlowIntegrationTest` |
| RN-09 | Una misión cancelada no puede iniciarse. | `Mission.assertCanTransitionTo` | `MissionFlowIntegrationTest.cancelledCannotStart` |
| RN-10 | Una misión pausada puede reanudarse. | `MissionStatus` (PAUSADA → EN_VUELO) | `MissionStatusTest` |
| RN-11 | Las operaciones importantes se auditan. | `AuditService`, `AuditMissionListener` | `MissionFlowIntegrationTest` (historial) |
| RN-12 | El cliente solo consulta sus propias misiones. | `MissionAccessService.canView` | `MissionFlowIntegrationTest.scopedVisibility` |
| RN-13 | El técnico solo administra mantenimiento (y estado técnico). | Permisos del rol TECNICO | `PermissionIntegrationTest` |
| RN-14 | El administrador tiene acceso completo. | Rol ADMINISTRADOR con todos los permisos | `PermissionIntegrationTest` |
| RN-15 | El drone debe tener la batería mínima de la estrategia (Inspección 50 %, Agricultura 70 %, Vigilancia/Monitoreo 60 %, Topografía 60 %). | `MissionStrategy.minimumBatteryPercent` | `AssignmentRulesTest.lowBattery` |
| RN-16 | El tipo de drone debe ser compatible con el tipo de misión. | `MissionStrategy.compatibleDroneTypes` | `AssignmentRulesTest.incompatibleType` |
| RN-17 | Al iniciar un mantenimiento el drone pasa a EN_MANTENIMIENTO; al completarlo, a DISPONIBLE. | `MaintenanceService.start / complete` | `MaintenanceAndIncidentIntegrationTest` |
| RN-18 | No se inicia mantenimiento a un drone que está en misión. | `MaintenanceService.start` | Prueba manual (smoke) |
| RN-19 | Un incidente CRÍTICO durante el vuelo pausa la misión y notifica a operaciones. | `IncidentService.handleCritical` | Documentado en CP-016 |
| RN-20 | Si la batería simulada baja del 20 %, se registra un incidente automático BATERIA_BAJA (una sola vez). | `TelemetrySimulator`, `IncidentService.registerSystemIncident` | Prueba manual |
| RN-21 | La altitud de vuelo está entre 5 y 120 m (límite legal RAC 100). | `MissionBuilder`, CHECK en BD | `MissionBuilderTest.rejectsAltitude` |
| RN-22 | Solo el piloto asignado (o el administrador) prepara, despega, pausa y finaliza la misión. | `MissionAccessService.checkExecute` | `MissionFlowIntegrationTest` (403) |
| RN-23 | Solo se eliminan misiones en BORRADOR; el resto se cancela. | `MissionService.delete` | `MissionFlowIntegrationTest.deleteOnlyDrafts` |
| RN-24 | Los registros con historial (usuarios, drones, pilotos, clientes) se desactivan en lugar de borrarse. | Servicios `deactivate` | `DroneCrudIntegrationTest.crud` |
| RN-25 | Una cuenta creada por registro queda inactiva hasta que un administrador la aprueba y le asigna un rol. | `AuthService.register`, `UserService.approve` | `RegistrationSecurityIntegrationTest.registerApproveLogin` |
| RN-26 | El nombre de usuario y el correo son únicos. | Restricciones UNIQUE + `AuthService.register` | `RegistrationSecurityIntegrationTest.registrationValidations` |
| RN-27 | Tras 5 intentos fallidos la cuenta se bloquea 15 minutos (aunque luego se use la contraseña correcta). | `AuthService.login` | `RegistrationSecurityIntegrationTest.lockout` |
| RN-28 | El administrador desbloquea una cuenta asignándole una nueva contraseña. | `UserService.update` | Prueba manual |
| RN-29 | Al aprobar, se puede vincular la ficha de piloto o cliente de una cuenta desactivada, pero no la de una activa. | `UserService.linkProfiles` | `approveLinkingPilotFromInactiveAccount` |
