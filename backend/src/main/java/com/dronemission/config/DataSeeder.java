package com.dronemission.config;

import com.dronemission.audit.AuditLog;
import com.dronemission.audit.AuditRepository;
import com.dronemission.clients.Client;
import com.dronemission.clients.ClientRepository;
import com.dronemission.clients.ClientType;
import com.dronemission.drones.Drone;
import com.dronemission.drones.DroneRepository;
import com.dronemission.drones.DroneStatus;
import com.dronemission.drones.DroneType;
import com.dronemission.evidence.Evidence;
import com.dronemission.evidence.EvidenceRepository;
import com.dronemission.evidence.EvidenceType;
import com.dronemission.incidents.Incident;
import com.dronemission.incidents.IncidentRepository;
import com.dronemission.incidents.IncidentSeverity;
import com.dronemission.incidents.IncidentStatus;
import com.dronemission.incidents.IncidentType;
import com.dronemission.maintenance.Maintenance;
import com.dronemission.maintenance.MaintenanceRepository;
import com.dronemission.maintenance.MaintenanceStatus;
import com.dronemission.maintenance.MaintenanceType;
import com.dronemission.missions.Mission;
import com.dronemission.missions.MissionAssignment;
import com.dronemission.missions.MissionAssignmentRepository;
import com.dronemission.missions.MissionPriority;
import com.dronemission.missions.MissionRepository;
import com.dronemission.missions.MissionStatus;
import com.dronemission.missions.MissionType;
import com.dronemission.notifications.Notification;
import com.dronemission.notifications.NotificationRepository;
import com.dronemission.pilots.Pilot;
import com.dronemission.pilots.PilotCertification;
import com.dronemission.pilots.PilotRepository;
import com.dronemission.pilots.PilotStatus;
import com.dronemission.reports.Report;
import com.dronemission.reports.ReportRepository;
import com.dronemission.reports.ReportType;
import com.dronemission.telemetry.Telemetry;
import com.dronemission.telemetry.TelemetryRepository;
import com.dronemission.users.Permission;
import com.dronemission.users.PermissionRepository;
import com.dronemission.users.Role;
import com.dronemission.users.RoleName;
import com.dronemission.users.RoleRepository;
import com.dronemission.users.User;
import com.dronemission.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Datos iniciales de demostración. Reemplaza a las migraciones SQL de Flyway
 * (V1 esquema, V2 datos, V3 seguridad): en MongoDB no hay tablas que crear, las colecciones
 * aparecen solas al guardar el primer documento.
 *
 * Se ejecuta al arrancar, SOLO si la base está vacía (no hay roles). Las fechas son relativas
 * a hoy para que la demo siempre tenga misiones "de hoy", "de mañana" y "de la semana pasada".
 * Contraseña de TODOS los usuarios demo: Demo2026!
 *
 * Los IDs salen en el mismo orden que antes (1, 2, 3...) porque los asigna la secuencia de cada colección.
 */
@Slf4j
@Component
@Order(0) // antes que los demás procesos de arranque (planes de vuelo, licencias, administrador)
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    /** Hash BCrypt de "Demo2026!" (el mismo que tenía la migración V2). */
    private static final String DEMO_HASH = "$2a$10$P/jlsuL8yNAuzGh4ODDJt.LozX5.jtgqY5CAWeAtA2n8hPu823pYS";

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PilotRepository pilotRepository;
    private final DroneRepository droneRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final MissionRepository missionRepository;
    private final MissionAssignmentRepository assignmentRepository;
    private final TelemetryRepository telemetryRepository;
    private final EvidenceRepository evidenceRepository;
    private final IncidentRepository incidentRepository;
    private final ReportRepository reportRepository;
    private final AuditRepository auditRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public void run(ApplicationArguments args) {
        seed();
    }

    /** Llena la base con los datos demo si está vacía. Las pruebas lo llaman después de limpiar la base. */
    public void seed() {
        if (roleRepository.count() > 0) {
            return; // ya hay datos: no se toca nada
        }
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // ------------------------------------------------------------------ Permisos y roles
        String[][] permissionData = {
                {"USUARIOS_GESTIONAR", "Crear, editar y desactivar usuarios"},
                {"ROLES_GESTIONAR", "Modificar los permisos de los roles"},
                {"DRONES_VER", "Consultar la flota de drones"},
                {"DRONES_GESTIONAR", "Crear, editar y desactivar drones"},
                {"DRONES_ESTADO_TECNICO", "Cambiar el estado técnico de un drone"},
                {"PILOTOS_VER", "Consultar pilotos"},
                {"PILOTOS_GESTIONAR", "Crear, editar y desactivar pilotos"},
                {"CLIENTES_VER", "Consultar clientes"},
                {"CLIENTES_GESTIONAR", "Crear, editar y desactivar clientes"},
                {"MISIONES_VER", "Consultar misiones (según alcance del rol)"},
                {"MISIONES_GESTIONAR", "Crear, editar, programar, asignar y cancelar misiones"},
                {"MISIONES_EJECUTAR", "Preparar, iniciar, pausar, reanudar y finalizar misiones"},
                {"INCIDENTES_VER", "Consultar todos los incidentes"},
                {"INCIDENTES_REGISTRAR", "Registrar incidentes en una misión"},
                {"INCIDENTES_GESTIONAR", "Cambiar el estado de los incidentes"},
                {"EVIDENCIAS_REGISTRAR", "Registrar evidencias en una misión"},
                {"MANTENIMIENTO_VER", "Consultar mantenimientos"},
                {"MANTENIMIENTO_GESTIONAR", "Registrar y gestionar mantenimientos"},
                {"REPORTES_VER", "Consultar y descargar reportes"},
                {"REPORTES_GENERAR", "Generar reportes"},
                {"DASHBOARD_VER", "Ver el dashboard de operaciones"},
                {"AUDITORIA_VER", "Consultar la bitácora de auditoría"}};
        List<Permission> permissions = new ArrayList<>();
        for (String[] p : permissionData) {
            Permission permission = new Permission();
            permission.setCode(p[0]);
            permission.setDescription(p[1]);
            permissions.add(permissionRepository.save(permission));
        }
        Map<String, Permission> byCode = permissions.stream()
                .collect(Collectors.toMap(Permission::getCode, Function.identity()));

        Role admin = role(RoleName.ADMINISTRADOR, "Acceso completo al sistema", new HashSet<>(permissions));
        Role operator = role(RoleName.OPERADOR, "Planifica misiones y asigna drones y pilotos", pick(byCode,
                "DRONES_VER", "PILOTOS_VER", "CLIENTES_VER", "MISIONES_VER", "MISIONES_GESTIONAR", "INCIDENTES_VER",
                "INCIDENTES_REGISTRAR", "INCIDENTES_GESTIONAR", "EVIDENCIAS_REGISTRAR", "MANTENIMIENTO_VER",
                "REPORTES_VER", "REPORTES_GENERAR", "DASHBOARD_VER"));
        Role pilotRole = role(RoleName.PILOTO, "Ejecuta las misiones asignadas", pick(byCode,
                "MISIONES_VER", "MISIONES_EJECUTAR", "INCIDENTES_REGISTRAR", "EVIDENCIAS_REGISTRAR"));
        Role technician = role(RoleName.TECNICO, "Gestiona el mantenimiento de la flota", pick(byCode,
                "DRONES_VER", "DRONES_ESTADO_TECNICO", "MANTENIMIENTO_VER", "MANTENIMIENTO_GESTIONAR", "DASHBOARD_VER"));
        Role clientRole = role(RoleName.CLIENTE, "Consulta sus misiones, evidencias y reportes", pick(byCode,
                "MISIONES_VER", "REPORTES_VER"));

        // ------------------------------------------------------------------ Usuarios demo
        User uAdmin = user("Laura", "Gómez Salazar", "admin", admin);
        User uOperator = user("Carlos", "Ramírez Peña", "operador", operator);
        User uPilot = user("Andrés", "Torres Molina", "piloto", pilotRole);
        User uTech = user("Mariana", "López Cárdenas", "tecnico", technician);
        User uClient = user("Sofía", "Herrera Vélez", "cliente", clientRole);
        User uPilot2 = user("Valentina", "Ruiz Arango", "piloto2", pilotRole);

        // ------------------------------------------------------------------ Clientes
        Client agro = client(ClientType.EMPRESA, "AgroValle S.A.S.", "900123456-7", "contacto@agrovalle.example", "602 555 0101", "Km 5 vía Palmira", "Palmira", uClient);
        Client energy = client(ClientType.EMPRESA, "Energía del Pacífico E.S.P.", "890456789-1", "operaciones@epacifico.example", "602 555 0202", "Calle 15 # 26-40", "Yumbo", null);
        Client builder = client(ClientType.EMPRESA, "Constructora Andina Ltda.", "901987654-3", "proyectos@candina.example", "602 555 0303", "Av. 6N # 23-50", "Cali", null);
        Client jorge = client(ClientType.PERSONA, "Jorge Medina Rojas", "16789456", "jorge.medina@correo.example", "315 555 0404", "Hacienda El Paraíso", "El Cerrito", null);

        // ------------------------------------------------------------------ Pilotos (con certificaciones embebidas)
        Pilot andres = pilot("Andrés", "Torres Molina", "1144123456", "piloto@dronemission.local", "310 555 1001", "UAEAC-RPA-2023-0145", today.plusDays(400), "152.50", PilotStatus.DISPONIBLE, uPilot,
                cert("Operación RPAS categoría específica", "Aerocivil", today.minusDays(700), today.plusDays(400)),
                cert("Inspección de líneas eléctricas", "SENA", today.minusDays(500), null));
        Pilot valentina = pilot("Valentina", "Ruiz Arango", "1130987654", "piloto2@dronemission.local", "311 555 1002", "UAEAC-RPA-2024-0288", today.plusDays(200), "98.00", PilotStatus.DISPONIBLE, uPilot2,
                cert("Aplicación aérea de agroquímicos", "ICA", today.minusDays(300), today.plusDays(430)),
                cert("Operación RPAS categoría específica", "Aerocivil", today.minusDays(400), today.plusDays(200)));
        pilot("Diego", "Castaño Ríos", "94456123", "diego.castano@dronemission.local", "312 555 1003", "UAEAC-RPA-2021-0032", today.minusDays(15), "210.75", PilotStatus.LICENCIA_VENCIDA, null,
                cert("Fotogrametría y topografía con drones", "Universidad del Valle", today.minusDays(900), null));
        pilot("Camila", "Ortiz Becerra", "1107456789", "camila.ortiz@dronemission.local", "313 555 1004", "UAEAC-RPA-2025-0410", today.plusDays(300), "45.25", PilotStatus.NO_DISPONIBLE, null,
                cert("Vuelo nocturno con cámara térmica", "Aerocivil", today.minusDays(120), today.plusDays(600)));

        // ------------------------------------------------------------------ Drones
        Drone d1 = drone("DRN-001", "Halcón I", "DJI", "Matrice 300 RTK", "M300-8841-A", DroneType.INSPECCION, DroneStatus.DISPONIBLE, 5935, 95, "120.50", 82, 55, today.minusDays(800), today.minusDays(30), today.plusDays(60), "Drone de inspección con cámara zoom y térmica.");
        Drone d2 = drone("DRN-002", "Agro Titán", "DJI", "Agras T40", "T40-2210-B", DroneType.AGRICULTURA, DroneStatus.DISPONIBLE, 30000, 88, "76.25", 36, 18, today.minusDays(500), today.minusDays(60), today.plusDays(30), "Tanque de 40 L para fumigación de precisión.");
        Drone d3 = drone("DRN-003", "Centinela", "Autel", "EVO Max 4T", "EVO4T-5521", DroneType.VIGILANCIA, DroneStatus.DISPONIBLE, 8070, 100, "64.00", 72, 42, today.minusDays(300), today.minusDays(20), today.plusDays(70), "Vigilancia diurna y nocturna con sensor térmico.");
        Drone d4 = drone("DRN-004", "Cartógrafo", "DJI", "Phantom 4 RTK", "P4R-7781-C", DroneType.TOPOGRAFIA, DroneStatus.EN_MANTENIMIENTO, 5870, 60, "188.75", 58, 30, today.minusDays(1200), today.minusDays(95), today.minusDays(5), "Levantamientos fotogramétricos con RTK.");
        Drone d5 = drone("DRN-005", "Lente Aéreo", "DJI", "Mavic 3 Pro", "M3P-4410-D", DroneType.FOTOGRAFIA, DroneStatus.DISPONIBLE, 5000, 76, "33.50", 75, 43, today.minusDays(200), today.minusDays(40), today.plusDays(50), "Fotografía y video 5.1K para eventos y bienes raíces.");
        Drone d6 = drone("DRN-006", "Vigía Nocturno", "Parrot", "Anafi USA", "ANF-9932-E", DroneType.VIGILANCIA, DroneStatus.FUERA_DE_SERVICIO, 3400, 20, "240.00", 55, 32, today.minusDays(1500), today.minusDays(200), today.plusDays(2), "Motor trasero dañado. Pendiente de reparación.");
        Drone d7 = drone("DRN-007", "Halcón II", "Skydio", "X10", "SKX10-1188", DroneType.INSPECCION, DroneStatus.DISPONIBLE, 4500, 45, "12.00", 64, 40, today.minusDays(90), null, today.plusDays(10), "Batería baja: útil para demostrar la validación de batería.");
        Drone d8 = drone("DRN-008", "Mapper Pro", "senseFly", "eBee X", "EBX-6620-F", DroneType.TOPOGRAFIA, DroneStatus.DISPONIBLE, 4900, 92, "58.25", 90, 90, today.minusDays(400), today.minusDays(15), today.plusDays(75), "Ala fija para mapeo de grandes extensiones.");

        // ------------------------------------------------------------------ Mantenimientos
        maintenance(d4, uTech, MaintenanceType.CALIBRACION, today.minusDays(1), "Calibración de IMU, brújula y módulo RTK.", "350000", MaintenanceStatus.EN_PROCESO, today.plusDays(90), "Desviación de 2 m detectada en el último vuelo.", now.minusHours(20), null);
        maintenance(d1, uTech, MaintenanceType.PREVENTIVO, today.minusDays(30), "Revisión general de hélices, motores y firmware.", "280000", MaintenanceStatus.COMPLETADO, today.plusDays(60), "Se actualizó el firmware a la última versión.", now.minusDays(30), now.minusDays(29));
        maintenance(d6, uTech, MaintenanceType.REPARACION, today.plusDays(2), "Cambio del motor trasero izquierdo.", "1200000", MaintenanceStatus.PROGRAMADO, today.plusDays(120), "Repuesto en camino.", null, null);
        maintenance(d2, uTech, MaintenanceType.INSPECCION, today.minusDays(60), "Inspección del sistema de aspersión y boquillas.", "150000", MaintenanceStatus.COMPLETADO, today.plusDays(30), null, now.minusDays(60), now.minusDays(60));
        maintenance(d7, uTech, MaintenanceType.PREVENTIVO, today.plusDays(10), "Primer mantenimiento preventivo (90 días de uso).", "220000", MaintenanceStatus.PROGRAMADO, null, null, null, null);

        // ------------------------------------------------------------------ Misiones
        LocalDateTime m1Start = today.minusDays(7).atStartOfDay().plusMinutes(485);
        LocalDateTime m2Start = today.minusDays(3).atStartOfDay().plusMinutes(395);
        Mission m1 = mission("MSN-0001", "Inspección torre de transmisión T-45", "Revisión de aisladores y herrajes de la torre T-45.", MissionType.INSPECCION, energy, "Línea Yumbo - Cali, torre T-45", 3.585200, -76.495100, 60, today.minusDays(7), "08:00", "09:30", MissionPriority.ALTA, MissionStatus.FINALIZADA, "Detectar puntos calientes y daños en aisladores.", "Vuelo sin novedades mayores.", uOperator, m1Start, m1Start.plusMinutes(25));
        Mission m2 = mission("MSN-0002", "Fumigación lote 12 de caña", "Aplicación de fertilizante foliar en el lote 12.", MissionType.AGRICULTURA, agro, "Hacienda La Esperanza, lote 12", 3.539400, -76.303200, 15, today.minusDays(3), "06:30", "08:00", MissionPriority.MEDIA, MissionStatus.FINALIZADA, "Cubrir 8 hectáreas con aplicación uniforme.", null, uOperator, m2Start, m2Start.plusMinutes(18));
        Mission m3 = mission("MSN-0003", "Levantamiento topográfico Los Pinos", "Ortofoto y modelo digital del terreno para la urbanización Los Pinos.", MissionType.TOPOGRAFIA, builder, "Urbanización Los Pinos, Jamundí", 3.262100, -76.539800, 80, today, "09:00", "11:00", MissionPriority.ALTA, MissionStatus.PROGRAMADA, "Generar ortofoto con precisión de 3 cm/píxel.", null, uOperator, null, null);
        Mission m4 = mission("MSN-0004", "Vigilancia perimetral planta Acopi", "Recorrido perimetral de seguridad de la subestación.", MissionType.VIGILANCIA, energy, "Subestación Acopi, Yumbo", 3.551300, -76.487600, 50, today.plusDays(2), "19:00", "20:30", MissionPriority.MEDIA, MissionStatus.PROGRAMADA, "Detectar intrusiones en el perímetro.", null, uOperator, null, null);
        mission("MSN-0005", "Monitoreo cultivo de café", "Seguimiento del estado vegetativo del cultivo.", MissionType.MONITOREO, agro, "Finca El Mirador, El Cerrito", 3.685400, -76.312500, 40, today.plusDays(4), "07:00", "08:30", MissionPriority.BAJA, MissionStatus.PENDIENTE, "Identificar zonas con estrés hídrico.", null, uOperator, null, null);
        mission("MSN-0006", "Sesión fotográfica Hacienda El Paraíso", "Fotografías aéreas para material turístico.", MissionType.FOTOGRAFIA, jorge, "Hacienda El Paraíso, El Cerrito", 3.632900, -76.258400, 70, today.plusDays(6), "16:00", "17:00", MissionPriority.BAJA, MissionStatus.BORRADOR, "Capturar 40 fotografías en hora dorada.", null, uOperator, null, null);
        Mission m7 = mission("MSN-0007", "Inspección puente Juanchito", "Inspección visual de la estructura del puente.", MissionType.INSPECCION, builder, "Puente Juanchito, Cali", 3.452600, -76.475300, 45, today.minusDays(2), "10:00", "11:00", MissionPriority.ALTA, MissionStatus.CANCELADA, "Detectar fisuras en las vigas principales.", "Cancelada por falla técnica del drone asignado.", uOperator, null, null);
        mission("MSN-0008", "Fumigación lote 7 de caña", "Control de plagas en el lote 7.", MissionType.AGRICULTURA, agro, "Hacienda La Esperanza, lote 7", 3.408700, -76.350900, 15, today.plusDays(5), "06:00", "07:30", MissionPriority.ALTA, MissionStatus.PENDIENTE, "Aplicar control biológico en 6 hectáreas.", null, uOperator, null, null);

        // Asignaciones (drone + piloto) de cada misión
        assign(m1, d1, andres, uOperator, now.minusDays(9));
        assign(m2, d2, valentina, uOperator, now.minusDays(5));
        assign(m3, d8, andres, uOperator, now.minusDays(1));
        assign(m4, d3, valentina, uOperator, now.minusDays(1));
        assign(m7, d5, andres, uOperator, now.minusDays(4));

        // Telemetría resumida de las misiones finalizadas (datos simulados)
        telemetry(m1, m1Start.plusMinutes(1), 3.585200, -76.495100, 20.00, 8.00, 94, 0.00, 24.10, 60, 1);
        telemetry(m1, m1Start.plusMinutes(5), 3.585900, -76.494600, 60.00, 22.50, 86, 310.40, 24.60, 300, 3);
        telemetry(m1, m1Start.plusMinutes(10), 3.585600, -76.493800, 60.00, 18.20, 76, 690.80, 25.20, 600, 5);
        telemetry(m1, m1Start.plusMinutes(15), 3.584600, -76.494300, 60.00, 21.00, 66, 1080.10, 25.80, 900, 7);
        telemetry(m1, m1Start.plusMinutes(20), 3.584800, -76.495600, 55.00, 19.60, 57, 1460.30, 26.10, 1200, 9);
        telemetry(m1, m1Start.plusMinutes(25), 3.585200, -76.495100, 0.00, 0.00, 49, 1620.00, 25.40, 1500, 10);
        telemetry(m2, m2Start.plusMinutes(1), 3.539400, -76.303200, 8.00, 6.00, 87, 0.00, 21.30, 60, 1);
        telemetry(m2, m2Start.plusMinutes(6), 3.539900, -76.302400, 15.00, 18.00, 64, 520.00, 22.00, 360, 4);
        telemetry(m2, m2Start.plusMinutes(12), 3.540500, -76.301700, 15.00, 18.00, 38, 1240.00, 22.90, 720, 8);
        telemetry(m2, m2Start.plusMinutes(18), 3.539400, -76.303200, 0.00, 0.00, 19, 1810.00, 23.10, 1080, 12);

        // ------------------------------------------------------------------ Evidencias
        evidence(m1, EvidenceType.FOTOGRAFIA, "Aislador fase B", "/demo/evidencias/torre-t45.svg", "Punto caliente leve en el aislador de la fase B.", true, uPilot, m1Start.plusMinutes(12));
        evidence(m1, EvidenceType.DOCUMENTO, "Informe termográfico T-45", "/demo/evidencias/informe-t45.svg", "Resumen de temperaturas registradas.", true, uPilot, m1Start.plusMinutes(40));
        evidence(m2, EvidenceType.FOTOGRAFIA, "Cobertura lote 12", "/demo/evidencias/lote-12.svg", "Vista general del lote tras la aplicación.", true, uPilot2, m2Start.plusMinutes(15));
        evidence(m2, EvidenceType.OBSERVACION, "Viento lateral", "-", "Ráfagas de 18 km/h en el costado norte; se redujo la velocidad.", false, uPilot2, m2Start.plusMinutes(7));

        // ------------------------------------------------------------------ Incidentes
        incident(m1, m1Start.plusMinutes(9), IncidentType.OBSTACULO, IncidentSeverity.MEDIA, "Ave rapaz cerca de la torre; se mantuvo distancia de seguridad.", uPilot, IncidentStatus.RESUELTO, "Se esperó 2 minutos y se continuó la inspección.");
        incident(m2, m2Start.plusMinutes(7), IncidentType.CLIMA, IncidentSeverity.BAJA, "Ráfagas de viento lateral de 18 km/h.", uPilot2, IncidentStatus.RESUELTO, "Se redujo la velocidad de aplicación.");
        incident(m2, m2Start.plusMinutes(17), IncidentType.BATERIA_BAJA, IncidentSeverity.ALTA, "Batería del drone por debajo del 20 % (alerta automática del sistema).", null, IncidentStatus.RESUELTO, "El piloto finalizó la misión y aterrizó en la base.");
        incident(m7, now.minusDays(2), IncidentType.ERROR_TECNICO, IncidentSeverity.ALTA, "Falla en el gimbal de la cámara durante la prueba previa al vuelo.", uPilot, IncidentStatus.ABIERTO, null);

        // ------------------------------------------------------------------ Reportes históricos
        report(m1, uOperator, m1Start.plusMinutes(30), "Misión finalizada en 25 min. Distancia 1620 m. Batería consumida 45 %. 1 incidente(s), 2 evidencia(s).");
        report(m2, uOperator, m2Start.plusMinutes(20), "Misión finalizada en 18 min. Distancia 1810 m. Batería consumida 68 %. 2 incidente(s), 2 evidencia(s).");

        // ------------------------------------------------------------------ Auditoría y notificaciones de ejemplo
        audit(uOperator, "CREAR", "MISION", 3L, "Misión MSN-0003 creada", now.minusDays(2));
        audit(uOperator, "ASIGNAR", "MISION", 3L, "Drone DRN-008 y piloto Andrés Torres Molina asignados a MSN-0003", now.minusDays(1));
        audit(uOperator, "CANCELAR", "MISION", 7L, "Misión MSN-0007 cancelada: falla técnica del drone", now.minusDays(2));
        audit(uTech, "INICIAR", "MANTENIMIENTO", 1L, "Mantenimiento de DRN-004 iniciado; drone EN_MANTENIMIENTO", now.minusHours(20));
        notification(uPilot, "Nueva misión asignada", "Se te asignó la misión MSN-0003 (Levantamiento topográfico Los Pinos) para hoy.", m3, now.minusDays(1));
        notification(uPilot2, "Nueva misión asignada", "Se te asignó la misión MSN-0004 (Vigilancia perimetral planta Acopi).", m4, now.minusDays(1));
        notification(uClient, "Misión finalizada", "La misión MSN-0002 finalizó. Ya puedes consultar el reporte.", m2, m2Start);

        log.info("Base de datos MongoDB vacía: se cargaron los datos de demostración (usuario admin: {})", uAdmin.getEmail());
    }

    // ====================================================================== Ayudantes
    // Cada método crea un objeto, le pone los valores y lo guarda con su repositorio.

    private static Set<Permission> pick(Map<String, Permission> byCode, String... codes) {
        Set<Permission> set = new HashSet<>();
        for (String code : codes) {
            set.add(byCode.get(code));
        }
        return set;
    }

    private Role role(RoleName name, String description, Set<Permission> permissions) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    private User user(String firstName, String lastName, String username, Role role) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(username + "@dronemission.local");
        user.setPasswordHash(DEMO_HASH);
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }

    private Client client(ClientType type, String name, String document, String email, String phone,
                          String address, String city, User user) {
        Client c = new Client();
        c.setType(type);
        c.setName(name);
        c.setDocument(document);
        c.setEmail(email);
        c.setPhone(phone);
        c.setAddress(address);
        c.setCity(city);
        c.setActive(true);
        c.setUser(user);
        return clientRepository.save(c);
    }

    private static PilotCertification cert(String name, String issuer, LocalDate issuedOn, LocalDate expiresOn) {
        PilotCertification c = new PilotCertification();
        c.setName(name);
        c.setIssuer(issuer);
        c.setIssuedOn(issuedOn);
        c.setExpiresOn(expiresOn);
        return c;
    }

    private Pilot pilot(String firstName, String lastName, String document, String email, String phone,
                        String license, LocalDate licenseExpiresOn, String hours, PilotStatus status, User user,
                        PilotCertification... certifications) {
        Pilot p = new Pilot();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setDocument(document);
        p.setEmail(email);
        p.setPhone(phone);
        p.setLicenseNumber(license);
        p.setLicenseExpiresOn(licenseExpiresOn);
        p.setFlightHours(new BigDecimal(hours));
        p.setStatus(status);
        p.setActive(true);
        p.setUser(user);
        p.replaceCertifications(List.of(certifications));
        return pilotRepository.save(p);
    }

    private Drone drone(String code, String name, String manufacturer, String model, String serial, DroneType type,
                        DroneStatus status, int capacityMah, int battery, String hours, int maxSpeed, int autonomy,
                        LocalDate acquiredOn, LocalDate lastMaintenance, LocalDate nextMaintenance, String description) {
        Drone d = new Drone();
        d.setCode(code);
        d.setName(name);
        d.setManufacturer(manufacturer);
        d.setModel(model);
        d.setSerialNumber(serial);
        d.setType(type);
        d.setStatus(status);
        d.setBatteryCapacityMah(capacityMah);
        d.setBatteryLevel(battery);
        d.setFlightHours(new BigDecimal(hours));
        d.setMaxSpeedKmh(maxSpeed);
        d.setAutonomyMinutes(autonomy);
        d.setAcquiredOn(acquiredOn);
        d.setLastMaintenanceOn(lastMaintenance);
        d.setNextMaintenanceOn(nextMaintenance);
        d.setDescription(description);
        d.setActive(true);
        return droneRepository.save(d);
    }

    private void maintenance(Drone drone, User technician, MaintenanceType type, LocalDate date, String description,
                             String cost, MaintenanceStatus status, LocalDate nextDate, String observations,
                             LocalDateTime startedAt, LocalDateTime completedAt) {
        Maintenance m = new Maintenance();
        m.setDrone(drone);
        m.setTechnician(technician);
        m.setType(type);
        m.setDate(date);
        m.setDescription(description);
        m.setCost(new BigDecimal(cost));
        m.setStatus(status);
        m.setNextDate(nextDate);
        m.setObservations(observations);
        m.setStartedAt(startedAt);
        m.setCompletedAt(completedAt);
        maintenanceRepository.save(m);
    }

    private Mission mission(String code, String name, String description, MissionType type, Client client,
                            String location, double lat, double lon, int altitude, LocalDate date, String start,
                            String end, MissionPriority priority, MissionStatus status, String objective,
                            String observations, User createdBy, LocalDateTime actualStart, LocalDateTime actualEnd) {
        Mission m = new Mission();
        m.setCode(code);
        m.setName(name);
        m.setDescription(description);
        m.setType(type);
        m.setClient(client);
        m.setLocation(location);
        m.setLatitude(lat);
        m.setLongitude(lon);
        m.setTargetAltitudeM(altitude);
        m.setScheduledDate(date);
        m.setStartTime(LocalTime.parse(start));
        m.setEstimatedEndTime(LocalTime.parse(end));
        m.setPriority(priority);
        m.setStatus(status);
        m.setObjective(objective);
        m.setObservations(observations);
        m.setCreatedBy(createdBy);
        m.setActualStart(actualStart);
        m.setActualEnd(actualEnd);
        // El plan de vuelo lo genera MissionService.ensureFlightPlans() al terminar de arrancar
        return missionRepository.save(m);
    }

    private void assign(Mission mission, Drone drone, Pilot pilot, User assignedBy, LocalDateTime at) {
        MissionAssignment a = new MissionAssignment();
        a.setMission(mission);
        a.setDrone(drone);
        a.setPilot(pilot);
        a.setAssignedBy(assignedBy);
        a.setAssignedAt(at);
        a.setActive(true);
        assignmentRepository.save(a);
        // La misión guarda la lista de sus asignaciones (referencias)
        mission.getAssignments().add(a);
        missionRepository.save(mission);
    }

    private void telemetry(Mission mission, LocalDateTime at, double lat, double lon, double altitude, double speed,
                           int battery, double distance, double temperature, int flightTimeS, int waypoint) {
        Telemetry t = new Telemetry();
        t.setMission(mission);
        t.setRecordedAt(at);
        t.setLatitude(lat);
        t.setLongitude(lon);
        t.setAltitudeM(altitude);
        t.setSpeedKmh(speed);
        t.setBattery(battery);
        t.setDistanceM(distance);
        t.setTemperatureC(temperature);
        t.setFlightTimeS(flightTimeS);
        t.setCurrentWaypoint(waypoint);
        t.setSimulated(true);
        telemetryRepository.save(t);
    }

    private void evidence(Mission mission, EvidenceType type, String name, String url, String description,
                          boolean visibleToClient, User uploadedBy, LocalDateTime date) {
        Evidence e = new Evidence();
        e.setMission(mission);
        e.setType(type);
        e.setName(name);
        e.setUrl(url);
        e.setDescription(description);
        e.setVisibleToClient(visibleToClient);
        e.setUploadedBy(uploadedBy);
        e.setDate(date);
        evidenceRepository.save(e);
    }

    private void incident(Mission mission, LocalDateTime date, IncidentType type, IncidentSeverity severity,
                          String description, User reportedBy, IncidentStatus status, String resolution) {
        Incident i = new Incident();
        i.setMission(mission);
        i.setDate(date);
        i.setType(type);
        i.setSeverity(severity);
        i.setDescription(description);
        i.setReportedBy(reportedBy);
        i.setStatus(status);
        i.setResolution(resolution);
        incidentRepository.save(i);
    }

    private void report(Mission mission, User generatedBy, LocalDateTime at, String summary) {
        Report r = new Report();
        r.setType(ReportType.MISION);
        r.setTitle("Reporte de misión " + mission.getCode());
        r.setMission(mission);
        r.setGeneratedBy(generatedBy);
        r.setGeneratedAt(at);
        r.setSummary(summary);
        reportRepository.save(r);
    }

    private void audit(User user, String action, String entity, Long entityId, String description, LocalDateTime date) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setUserEmail(user.getEmail());
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setDescription(description);
        log.setDate(date);
        auditRepository.save(log);
    }

    private void notification(User user, String title, String message, Mission mission, LocalDateTime date) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setMission(mission);
        n.setRead(false);
        n.setDate(date);
        notificationRepository.save(n);
    }
}
