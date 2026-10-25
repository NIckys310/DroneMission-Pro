# DroneMission Pro — Backend

API REST con **Spring Boot 3.3 / Java 17**.

```bash
./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run  → http://localhost:8080
./mvnw test                 # 80 pruebas
./mvnw package              # genera target/dronemission-backend-1.0.0.jar
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Consola H2: http://localhost:8080/h2-console (`jdbc:h2:file:./data/dronemission`, usuario `sa`)
- Perfil PostgreSQL: `SPRING_PROFILES_ACTIVE=postgres`

## Estructura por módulo

| Paquete | Contenido |
|---|---|
| `config` | Seguridad (CORS, JWT, rutas públicas) y Swagger |
| `common.exception` | Excepciones de negocio y `GlobalExceptionHandler` (formato `ApiError`) |
| `auth` | Login, JWT, filtro de autenticación, usuario actual |
| `users` | Usuarios, roles y permisos |
| `drones`, `pilots`, `clients` | Catálogos (con `DroneFactory`) |
| `missions` | Misiones, asignaciones, plan de vuelo, máquina de estados, Builder; `strategy/` (Strategy) y `event/` (Observer) |
| `telemetry` | Simulador del drone virtual y consultas |
| `incidents`, `evidence`, `maintenance` | Operación |
| `reports`, `dashboard` | Reportes PDF e indicadores |
| `audit`, `notifications` | Observadores de los eventos de misión |

Cada módulo: `Entidad → Repository → Service → Controller`, con `dto/` y `Mapper`.
