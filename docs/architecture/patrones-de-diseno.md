# Patrones de diseño y su justificación

Cada patrón se usó porque resuelve un problema concreto del sistema, no para aumentar código.

## Builder — `missions/MissionBuilder.java`

**Problema:** una misión tiene 15 atributos obligatorios y reglas entre ellos (hora fin > hora inicio, fecha no pasada, altitud 5–120 m, cliente activo). Con un constructor gigante o con *setters* sueltos podría existir una misión a medio construir.

**Solución:** API fluida (`new MissionBuilder().code(..).name(..)...build()`) que valida todo en `build()` y devuelve la misión en `BORRADOR`.

**Uso:** `MissionService.create`. **Prueba:** `MissionBuilderTest`.

## Strategy — `missions/strategy/*`

**Problema:** cada tipo de misión vuela distinto: inspección hace órbitas, agricultura un zigzag a baja altura, vigilancia recorre el perímetro, topografía una cuadrícula. También cambian la batería mínima, los drones compatibles, la velocidad y el consumo. Con `if/else` por tipo el código se repetiría en asignación, plan de vuelo y simulador.

**Solución:** interfaz `MissionStrategy` con cuatro implementaciones (`InspectionStrategy`, `AgricultureStrategy`, `SurveillanceStrategy`, `MappingStrategy`) y un `MissionStrategyResolver` que elige la estrategia según el tipo. `AbstractMissionStrategy` (método plantilla) agrega despegue y aterrizaje comunes.

**Uso:** `FlightPlanService` (genera el plan), `AssignmentService` (batería y compatibilidad), `TelemetrySimulator` (velocidad y consumo). **Agregar un tipo nuevo** = crear una clase `@Component`. **Prueba:** `MissionStrategyTest`.

## Observer — `missions/event/*`

**Problema:** cuando una misión cambia hay que auditar y notificar a varias personas. Si `MissionService` llamara directamente a cada servicio, quedaría acoplado y cada nueva reacción (por ejemplo, enviar correos) exigiría modificarlo.

**Solución:** `MissionEventPublisher` (sujeto) publica `MissionEvent`; los observadores implementan `MissionEventListener`: `AuditMissionListener` y `NotificationMissionListener`. Spring inyecta automáticamente todos los observadores (principio abierto/cerrado).

## Factory (método fábrica) — `drones/DroneFactory.java`

**Problema:** al registrar un drone, la capacidad de batería, la velocidad y la autonomía dependen del tipo y el usuario puede no conocerlas; además hay que generar el código `DRN-XXX`.

**Solución:** `DroneFactory.createDrone(request, secuencia)` centraliza la creación y aplica la especificación típica de cada tipo. Es la variante *Simple Factory*; se documenta así honestamente.

## State (máquina de estados) — `missions/MissionStatus.java`

**Problema:** hay 8 estados y solo ciertas transiciones son válidas (una misión finalizada no se reabre, una cancelada no se inicia).

**Solución:** el enum conoce sus transiciones permitidas (`canTransitionTo`) y `Mission.changeStatus` rechaza las inválidas con `InvalidStateTransitionException` (HTTP 409). **Prueba:** `MissionStatusTest` (21 casos).

## Singleton — beans de Spring (`TelemetrySimulator`)

**Justificación:** debe existir **un solo motor de simulación** que procese las misiones en vuelo; dos instancias duplicarían la telemetría. Se usa el singleton gestionado por Spring (alcance por defecto), no un singleton manual con `static`, lo que mantiene la clase probable e inyectable. El simulador no guarda estado en memoria: lo reconstruye desde la BD.

## MVC / arquitectura en capas

- **Modelo:** entidades JPA y DTOs.
- **Vista:** frontend React.
- **Controlador:** `*Controller` (solo recibe, delega y responde).
- Capas Controller → Service → Repository; ninguna regla de negocio en los controllers.

## Otros patrones presentes

- **DTO:** los controllers nunca exponen entidades (`dto/*Request`, `*Response`).
- **Repository:** Spring Data JPA.
- **Mapper:** `DroneMapper`, `PilotMapper`, `ClientMapper`, `MissionMapper`.
- **Template Method:** `AbstractMissionStrategy.generateFlightPlan`.
