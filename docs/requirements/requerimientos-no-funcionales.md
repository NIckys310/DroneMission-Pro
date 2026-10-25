# Requerimientos no funcionales

| Código | Categoría | Requerimiento | Cómo se cumple |
|---|---|---|---|
| RNF-001 | Seguridad | Las contraseñas nunca se almacenan en texto plano. | Hash BCrypt con factor de costo 12 (`PasswordEncoder`); columna `contrasena_hash`. |
| RNF-002 | Seguridad | Toda la API (excepto login y recuperación) exige un token JWT válido. | `JwtAuthenticationFilter` + `SecurityConfig` (stateless). |
| RNF-003 | Seguridad | Cada endpoint verifica el permiso del rol. | `@PreAuthorize("hasAuthority('...')")` + tabla `rol_permisos`. |
| RNF-004 | Seguridad | El cliente solo accede a sus misiones y el piloto a las asignadas. | `MissionAccessService`. |
| RNF-005 | Seguridad | Los secretos se configuran por variables de entorno; no hay claves reales en el código. | `application.yml` con `${JWT_SECRET}`, `.env.example`. |
| RNF-006 | Seguridad | Los errores no exponen detalles internos. | `GlobalExceptionHandler` devuelve `ApiError` genérico ante errores 500. |
| RNF-007 | Seguridad | CORS solo para el origen del frontend. | `CORS_ALLOWED_ORIGINS`. |
| RNF-008 | Rendimiento | Las consultas habituales responden en menos de 1 s con los datos demo. | Índices en estados, fechas y llaves foráneas. |
| RNF-009 | Rendimiento | La telemetría se actualiza en pantalla cada 2 s sin recargar la página. | Polling del frontend + simulador programado. |
| RNF-010 | Usabilidad | Interfaz en español, responsive y con mensajes de error claros. | MUI, mensajes del backend en español, validación por campo. |
| RNF-011 | Usabilidad | El usuario solo ve las opciones que su rol permite. | Menú y botones según permisos y `availableActions`. |
| RNF-012 | Mantenibilidad | Arquitectura en capas y paquetes por módulo. | Controller → Service → Repository; ver vista de desarrollo. |
| RNF-013 | Mantenibilidad | El esquema de BD se versiona con migraciones. | Flyway `V1`, `V2`; Hibernate `ddl-auto=validate`. |
| RNF-014 | Mantenibilidad | Pruebas automatizadas de las reglas críticas. | 80 pruebas JUnit (unitarias + integración) y Vitest. |
| RNF-015 | Disponibilidad | El sistema se ejecuta sin instalar una base de datos. | Perfil H2 embebida por defecto; PostgreSQL opcional. |
| RNF-016 | Disponibilidad | La simulación continúa tras reiniciar el servidor. | El simulador reconstruye el estado desde la última lectura guardada. |
| RNF-017 | Escalabilidad | La API es sin estado (stateless) y puede replicarse. | JWT sin sesión en servidor. |
| RNF-018 | Escalabilidad | Agregar un tipo de misión no modifica los servicios existentes. | Patrón Strategy: nueva clase `@Component`. |
| RNF-019 | Trazabilidad | Las operaciones importantes quedan auditadas. | `AuditService` + observador `AuditMissionListener`. |
| RNF-020 | Portabilidad | Funciona en Windows, Linux y macOS. | Java 17+, Node 18+, Maven Wrapper, scripts `.bat` y `.sh`. |
| RNF-021 | Seguridad | Las contraseñas deben ser seguras: 8+ caracteres, mayúscula, minúscula, número y sin datos personales. | `PasswordPolicy` (backend) y `PasswordChecklist` (frontend). |
| RNF-022 | Seguridad | Tras 5 intentos fallidos la cuenta se bloquea 15 minutos. | `AuthService.registerFailedAttempt`; columnas `intentos_fallidos`, `bloqueado_hasta`. |
| RNF-023 | Seguridad | Máximo 20 intentos por minuto y por IP en login, registro y recuperación. | `AuthRateLimitFilter` (HTTP 429). |
| RNF-024 | Seguridad | Ninguna cuenta registrada obtiene permisos sin aprobación de un administrador. | Cuenta inactiva con `pendiente_aprobacion`. |
| RNF-025 | Seguridad | En un servidor público no existen credenciales conocidas: usuarios demo desactivados, consola H2 deshabilitada y token de recuperación oculto. | Variables `APP_DEMO_USERS`, `H2_CONSOLE_ENABLED`, `APP_SHOW_RESET_TOKEN` en `render.yaml`. |
| RNF-026 | Seguridad | La contraseña del administrador dueño nunca está en el código ni en el repositorio. | Variables secretas `ADMIN_*` (`sync: false` en Render) y `SecurityBootstrap`. |
| RNF-027 | Disponibilidad | Las cuentas y datos se conservan aunque el servidor se reinicie. | PostgreSQL administrado en Render. |
