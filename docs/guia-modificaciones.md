# Guía para modificar el sistema por tu cuenta

Esta guía explica, paso a paso y sin ayuda de IA, cómo **agregar, cambiar o eliminar** cosas en DroneMission Pro. Úsala para la exposición: cada receta indica qué archivos tocar y en qué orden.

> **Regla de oro:** un dato atraviesa siempre las mismas capas, de abajo hacia arriba:
> **Tabla (SQL) → Entidad (Java) → DTO → Mapper → Service → Controller → `types/index.ts` → formulario/tabla (React)**.

---

## 0. Antes de empezar

| Quiero… | Comando |
|---|---|
| Arrancar el backend | `iniciar-backend.bat` (o `cd backend && mvnw spring-boot:run`) |
| Arrancar el frontend | `iniciar-frontend.bat` (o `cd frontend && npm run dev`) |
| Reiniciar la base H2 desde cero | Detén el backend y borra la carpeta `backend/data/` |
| Probar que no rompí nada | `cd backend && mvnw test` y `cd frontend && npm run build` |
| Ver los endpoints | http://localhost:8080/swagger-ui.html |
| Ver la base H2 | http://localhost:8080/h2-console (URL `jdbc:h2:file:./data/dronemission`, usuario `sa`, sin contraseña) |

---

## 1. Agregar un campo nuevo a una entidad existente

**Ejemplo:** agregar el **peso en kg** al drone.

### 1.1 Base de datos: nueva migración
Crea `backend/src/main/resources/db/migration/V3__agregar_peso_drone.sql`:
```sql
ALTER TABLE drones ADD COLUMN peso_kg NUMERIC(6,2);
```
> Nunca edites `V1` ni `V2`: Flyway detecta el cambio y no arranca. Si es tu base de pruebas, puedes borrar `backend/data/` y volver a arrancar.

### 1.2 Entidad — `drones/Drone.java`
```java
@Column(name = "peso_kg", precision = 6, scale = 2)
private BigDecimal weightKg;
```
(Lombok genera `getWeightKg()` y `setWeightKg()` automáticamente.)

### 1.3 DTO de entrada — `drones/dto/DroneRequest.java`
Agrega el parámetro al record, con su validación:
```java
@PositiveOrZero(message = "El peso no puede ser negativo") BigDecimal weightKg,
```

### 1.4 DTO de salida — `drones/dto/DroneResponse.java`
```java
BigDecimal weightKg,
```

### 1.5 Mapper — `drones/DroneMapper.java`
- En `apply(...)`: `drone.setWeightKg(req.weightKg());`
- En `toResponse(...)`: pasa `d.getWeightKg()` en la misma posición que en el record.

> Si cambias el orden de los parámetros de un `record`, el compilador te avisa en todos los lugares donde se usa: **sigue los errores**.

### 1.6 Frontend
1. `frontend/src/types/index.ts` → en `interface Drone`: `weightKg: number | null;`
2. `frontend/src/pages/drones/droneForm.ts` → agrega una línea al arreglo `droneFields`:
   ```ts
   { name: 'weightKg', label: 'Peso (kg)', type: 'number', min: 0, cols: 4 },
   ```
   ¡Listo! El formulario genérico (`EntityFormDialog`) dibuja el campo y lo valida solo.
3. Para mostrarlo en la tabla, edita `DronesPage.tsx`: agrega `<TableCell>Peso</TableCell>` en el encabezado y `<TableCell>{d.weightKg}</TableCell>` en la fila.

### 1.7 Documentación
Actualiza `docs/diagrams/erd/erd.puml` y `docs/diagrams/class/vista-logica-clases.puml`.

---

## 2. Eliminar un campo

Haz el camino **inverso**: quita el campo del formulario y de `types` → del Mapper → de los DTO → de la entidad → crea una migración `V4__quitar_xxx.sql` con `ALTER TABLE ... DROP COLUMN ...;`. Compila (`mvnw test`): el compilador señala cualquier uso olvidado.

---

## 3. Agregar un valor a un enum (estado, tipo…)

**Ejemplo:** nuevo tipo de incidente `INTERFERENCIA`.

1. `incidents/IncidentType.java` → agrega `INTERFERENCIA`.
2. Migración `V3__tipo_interferencia.sql` actualizando la restricción CHECK:
   ```sql
   ALTER TABLE incidentes DROP CONSTRAINT ck_incidentes_tipo;
   ALTER TABLE incidentes ADD CONSTRAINT ck_incidentes_tipo CHECK (tipo IN
     ('BATERIA_BAJA','PERDIDA_SENAL','OBSTACULO','ERROR_TECNICO','CLIMA','ATERRIZAJE_EMERGENCIA','OTRO','INTERFERENCIA'));
   ```
3. Frontend: `types/index.ts` (tipo `IncidentType`) y `utils/labels.ts` (`INTERFERENCIA: 'Interferencia'`). El select del formulario se llena solo desde `labels.ts`.

---

## 4. Cambiar una regla de negocio

| Regla | Dónde cambiarla |
|---|---|
| Batería mínima por tipo de misión | `minimumBatteryPercent()` en `missions/strategy/*Strategy.java` |
| Drones compatibles con un tipo de misión | `compatibleDroneTypes()` en la estrategia |
| Patrón de vuelo (radio de la órbita, tamaño del lote…) | constantes al inicio de cada `*Strategy.java` |
| Transiciones de estado permitidas | mapa `TRANSITIONS` en `missions/MissionStatus.java` |
| Altitud máxima | `MAX_ALTITUDE_M` en `MissionBuilder.java` **y** el CHECK `ck_misiones_altitud` (migración) |
| Umbral de batería baja del simulador | `LOW_BATTERY_THRESHOLD` en `telemetry/TelemetrySimulator.java` |
| Velocidad de la simulación | `SIMULATION_TIME_FACTOR` en `.env` / `application.yml` |
| Duración del token | `JWT_EXPIRATION_MINUTES` |
| Validaciones de asignación | `droneIneligibility` / `pilotIneligibility` en `missions/AssignmentService.java` |

Después de cambiar una regla, ejecuta `mvnw test`: si alguna prueba falla, actualízala para reflejar la nueva regla.

---

## 5. Agregar un endpoint nuevo

**Ejemplo:** `GET /api/drones/low-battery` (drones con menos de 30 %).

1. **Repository** (`DroneRepository.java`) — Spring genera la consulta a partir del nombre:
   ```java
   List<Drone> findByActiveTrueAndBatteryLevelLessThan(int level);
   ```
2. **Service** (`DroneService.java`):
   ```java
   @Transactional(readOnly = true)
   public List<DroneResponse> lowBattery() {
       return droneRepository.findByActiveTrueAndBatteryLevelLessThan(30).stream()
               .map(DroneMapper::toResponse).toList();
   }
   ```
3. **Controller** (`DroneController.java`) — sin lógica, solo delega:
   ```java
   @GetMapping("/low-battery")
   @PreAuthorize("hasAuthority('DRONES_VER')")
   public List<DroneResponse> lowBattery() {
       return droneService.lowBattery();
   }
   ```
   > Ponlo **antes** de `@GetMapping("/{id}")` en el archivo para que se lea con claridad (Spring prioriza la ruta literal de todos modos).
4. **Frontend** (`api/endpoints.ts`, dentro de `droneApi`):
   ```ts
   lowBattery: () => data<Drone[]>(http.get('/drones/low-battery')),
   ```
5. Pruébalo en Swagger y agrégalo a `docs/architecture/api-rest.md`.

---

## 6. Agregar un permiso o cambiar qué puede hacer un rol

- **Sin programar:** como administrador, entra a **Roles y permisos**, marca/desmarca y guarda.
- **Permiso nuevo:** migración que inserte el permiso y lo asigne:
  ```sql
  INSERT INTO permisos (codigo, descripcion) VALUES ('DRONES_EXPORTAR', 'Exportar la flota');
  INSERT INTO rol_permisos (rol_id, permiso_id) SELECT 1, id FROM permisos WHERE codigo = 'DRONES_EXPORTAR';
  ```
  Luego úsalo en el controller: `@PreAuthorize("hasAuthority('DRONES_EXPORTAR')")` y en el frontend con `can('DRONES_EXPORTAR')`.

---

## 7. Agregar una entidad (módulo) completa

**Ejemplo:** `Battery` (baterías de repuesto).

1. Migración `V3__baterias.sql` con `CREATE TABLE baterias (...)` y su FK a `drones`.
2. Crea el paquete `com.dronemission.batteries` copiando la estructura de `clients` (es el módulo más simple):
   `Battery.java` (entidad), `BatteryRepository`, `dto/BatteryRequest`, `dto/BatteryResponse`, `BatteryMapper`, `BatteryService`, `BatteryController`.
3. Agrega permisos (sección 6).
4. Frontend: tipo en `types/index.ts`, funciones en `api/endpoints.ts`, carpeta `pages/batteries/` copiando `pages/clients/`, ruta en `App.tsx` y opción del menú en `components/layout/AppLayout.tsx`.
5. Actualiza los diagramas de clases, ERD y paquetes.

---

## 8. Agregar un nuevo tipo de misión (patrón Strategy)

1. `MissionType.java` → nuevo valor, p. ej. `RESCATE`.
2. Crea `missions/strategy/RescueStrategy.java`:
   ```java
   @Component
   public class RescueStrategy extends AbstractMissionStrategy {
       @Override public String name() { return "Búsqueda en espiral"; }
       @Override public Set<MissionType> supportedTypes() { return Set.of(MissionType.RESCATE); }
       @Override public int minimumBatteryPercent() { return 80; }
       @Override public Set<DroneType> compatibleDroneTypes() { return Set.of(DroneType.VIGILANCIA); }
       @Override public double cruiseSpeedKmh() { return 25; }
       @Override public double batteryDrainPerMinute() { return 2.5; }
       @Override protected List<PlannedWaypoint> generatePattern(GeoPoint c, int alt) {
           List<PlannedWaypoint> points = new ArrayList<>();
           for (int i = 1; i <= 16; i++) {           // espiral que se abre
               double r = 20 * i, a = i * Math.PI / 4;
               points.add(new PlannedWaypoint(c.offset(r * Math.cos(a), r * Math.sin(a)), alt, WaypointAction.VIDEO));
           }
           return points;
       }
   }
   ```
3. Migración para el CHECK de `misiones.tipo`, y en el frontend `labels.ts` + `types`.
**No se toca ningún servicio**: esa es la ventaja del patrón Strategy.

---

## 8b. Seguridad y cuentas

| Quiero… | Cómo |
|---|---|
| Aprobar a alguien que se registró | Entra como administrador → **Usuarios** → *Cuentas pendientes* → **Aprobar** y elige el rol |
| Desbloquear una cuenta (5 intentos fallidos) | **Usuarios** → editar → escribir una **nueva contraseña** → Guardar |
| Recuperar MI contraseña de administrador en la nube | Render → servicio → *Environment*: cambia `ADMIN_PASSWORD` y pon `ADMIN_RESET_PASSWORD=true` → *Save, rebuild and deploy*. Luego vuelve a ponerlo en `false` |
| Cambiar los requisitos de contraseña | `backend/.../common/PasswordPolicy.java` **y** `frontend/src/utils/password.ts` (deben coincidir) |
| Cambiar intentos permitidos o minutos de bloqueo | Variables `APP_MAX_FAILED_ATTEMPTS` y `APP_LOCK_MINUTES` |
| Cerrar el registro público | `APP_REGISTRATION_ENABLED=false` (el enlace "Crear cuenta" desaparece) |
| Volver a ver los usuarios demo | `APP_DEMO_USERS=true` (¡nunca en un servidor público!) |

## 8c. Simulador 3D

| Quiero… | Dónde |
|---|---|
| Cambiar la velocidad o el consumo de batería del drone | `frontend/src/pages/simulator/engine.ts` (`createState` y `step`) |
| Cambiar los umbrales de batería (aviso 25 %, regreso 18 %, emergencia 6 %) | constantes al inicio de `engine.ts` |
| Agregar una falla simulada | `injectFailure` en `engine.ts` y un botón en `SimulatorPage.tsx` |
| Cambiar el escenario (torre, cultivo, casas, árboles) o el modelo del drone | `frontend/src/pages/simulator/scene.ts` |
| Cambiar la ruta de práctica de un tipo de misión | la estrategia del tipo en `backend/.../missions/strategy/` (es la misma que usan las misiones reales) |
| Cambiar la ubicación de la base de práctica | `SimulatorService.java` (coordenadas de Cali) |

Después de cambiar el motor ejecuta `cd frontend && npm test` para revisar que las pruebas del simulador sigan pasando.

## 9. Cambios visuales rápidos

| Quiero cambiar… | Archivo |
|---|---|
| Colores, tipografía, bordes | `frontend/src/theme.ts` |
| Textos de estados y tipos | `frontend/src/utils/labels.ts` |
| Opciones del menú lateral | `frontend/src/components/layout/AppLayout.tsx` (`useNavItems`) |
| Tarjetas y gráficas del dashboard | `frontend/src/pages/home/OpsDashboard.tsx` |
| Logo | `frontend/public/drone.svg` |
| Ícono y nombre de la app instalada | `frontend/public/manifest.json` y `frontend/public/icons/` (si cambias archivos, sube la versión `dronemission-v1` en `public/sw.js`) |
| Datos demo | `backend/src/main/resources/db/migration/V2__datos_iniciales.sql` (y borrar `backend/data/`) |

---

## 10. Si algo falla

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Validate failed: Migration checksum mismatch` | Editaste V1/V2 | Revierte el cambio o borra `backend/data/` |
| `Schema-validation: missing column` | Entidad con campo que no está en la tabla | Falta la migración o el nombre de `@Column` |
| 403 en un endpoint nuevo | El rol no tiene el permiso del `@PreAuthorize` | Asigna el permiso (sección 6) |
| El frontend dice "No hay conexión con el servidor" | Backend apagado | Arranca el backend primero |
| `Port 8080 already in use` | Otro backend abierto | Cierra la otra ventana o usa `SERVER_PORT=8081` |
