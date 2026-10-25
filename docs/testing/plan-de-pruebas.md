# Plan de pruebas

## Estrategia

| Nivel | Qué prueba | Herramienta | Dónde |
|---|---|---|---|
| Unitarias | Reglas puras sin base de datos: máquina de estados, Builder, Strategy, Factory, licencias, reglas de asignación (con Mockito) | JUnit 5, Mockito, AssertJ | `backend/src/test/java/com/dronemission/unit` |
| Integración | La aplicación completa con H2 en memoria, migraciones y datos demo, mediante peticiones HTTP reales | Spring Boot Test, MockMvc | `backend/src/test/java/com/dronemission/integration` |
| Frontend | Validaciones de formularios | Vitest | `frontend/src/utils/validation.test.ts` |
| Manual / aceptación | Recorrido de la demo por cada rol en el navegador | Guion de [casos-de-prueba.md](casos-de-prueba.md) | — |

Cada prueba de integración es `@Transactional`: los cambios se deshacen al terminar y las pruebas no dependen unas de otras.

## Cómo ejecutarlas

```bash
cd backend && ./mvnw test        # Windows: mvnw.cmd test
cd frontend && npm test
```

## Resultado actual

- Backend: **80 pruebas, 0 fallos** (45 unitarias y 35 de integración).
- Frontend: **5 pruebas, 0 fallos**.

| Clase de prueba | Pruebas |
|---|---|
| MissionStatusTest | 21 |
| MissionStrategyTest | 9 |
| AssignmentRulesTest | 8 |
| MissionBuilderTest | 6 |
| DroneFactoryTest | 2 |
| PilotLicenseTest | 2 |
| AuthIntegrationTest | 4 |
| DroneCrudIntegrationTest | 4 |
| PilotCrudIntegrationTest | 3 |
| MissionFlowIntegrationTest | 5 |
| MaintenanceAndIncidentIntegrationTest | 3 |
| PermissionIntegrationTest | 13 |

## Criterios de aceptación

- Todas las pruebas automatizadas pasan.
- El backend arranca con H2 y con PostgreSQL (Hibernate `validate` confirma que las entidades coinciden con las tablas).
- El frontend compila sin errores de TypeScript (`npm run build`).
- Los 5 usuarios demo completan su recorrido sin errores.
