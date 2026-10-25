package com.dronemission.reports;

import com.dronemission.auth.AuthenticatedUser;
import com.dronemission.auth.CurrentUserService;
import com.dronemission.clients.Client;
import com.dronemission.clients.ClientRepository;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.drones.Drone;
import com.dronemission.drones.DroneRepository;
import com.dronemission.drones.DroneStatus;
import com.dronemission.evidence.EvidenceService;
import com.dronemission.incidents.IncidentService;
import com.dronemission.maintenance.Maintenance;
import com.dronemission.maintenance.MaintenanceRepository;
import com.dronemission.maintenance.MaintenanceStatus;
import com.dronemission.missions.*;
import com.dronemission.missions.strategy.MissionStrategyResolver;
import com.dronemission.pilots.Pilot;
import com.dronemission.pilots.PilotRepository;
import com.dronemission.pilots.PilotStatus;
import com.dronemission.reports.dto.*;
import com.dronemission.telemetry.TelemetryService;
import com.dronemission.telemetry.dto.TelemetrySummary;
import com.dronemission.users.RoleName;
import com.dronemission.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Arma los datos de cada reporte y lleva el historial de reportes generados. */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final MissionRepository missionRepository;
    private final MissionAssignmentRepository assignmentRepository;
    private final DroneRepository droneRepository;
    private final PilotRepository pilotRepository;
    private final ClientRepository clientRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final MissionAccessService accessService;
    private final MissionStrategyResolver strategyResolver;
    private final TelemetryService telemetryService;
    private final IncidentService incidentService;
    private final EvidenceService evidenceService;
    private final CurrentUserService currentUserService;
    private final PdfReportGenerator pdfGenerator;

    /** Archivo PDF listo para descargar. */
    public record PdfFile(String fileName, byte[] content) {
    }

    /** Historial: operaciones ve todo; el cliente solo los reportes de sus misiones. */
    @Transactional(readOnly = true)
    public List<ReportResponse> history() {
        AuthenticatedUser user = currentUserService.get();
        List<Report> reports;
        if (user.hasRole(RoleName.CLIENTE)) {
            reports = clientRepository.findByUserId(user.id())
                    .map(c -> reportRepository.findByMissionClientIdOrderByGeneratedAtDesc(c.getId()))
                    .orElse(List.of());
        } else {
            reports = reportRepository.findAllByOrderByGeneratedAtDesc();
        }
        return reports.stream().map(ReportService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MissionReport missionReport(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new com.dronemission.common.exception.ResourceNotFoundException("Misión", missionId));
        accessService.checkView(mission);
        if (currentUserService.get().hasRole(RoleName.CLIENTE) && mission.getStatus() != MissionStatus.FINALIZADA) {
            throw new AccessDeniedException("El reporte estará disponible cuando la misión finalice");
        }
        return buildMissionReport(mission);
    }

    /** Guarda en el historial un reporte de misión (se llama al finalizar y al descargar el PDF). */
    @Transactional
    public Report recordMissionReport(Mission mission, User generatedBy) {
        MissionReport data = buildMissionReport(mission);
        TelemetrySummary t = data.telemetry();
        String summary = "Misión " + data.status().toLowerCase() + (data.durationMinutes() != null ? " en " + data.durationMinutes() + " min" : "")
                + ". Distancia " + Math.round(t.distanceM()) + " m. Batería consumida " + t.batteryUsed() + " %. "
                + data.incidents().size() + " incidente(s), " + data.evidences().size() + " evidencia(s).";
        return record(ReportType.MISION, "Reporte de misión " + mission.getCode(), mission, generatedBy, summary);
    }

    @Transactional(readOnly = true)
    public DroneReport droneReport() {
        List<Drone> drones = droneRepository.findByActiveTrue().stream().sorted(Comparator.comparing(Drone::getCode)).toList();
        Map<DroneStatus, Long> byStatus = drones.stream().collect(Collectors.groupingBy(Drone::getStatus, Collectors.counting()));
        BigDecimal hours = drones.stream().map(Drone::getFlightHours).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<DroneReport.Row> rows = drones.stream()
                .map(d -> new DroneReport.Row(d.getCode(), d.getName(), d.getManufacturer() + " " + d.getModel(),
                        d.getType().name(), d.getStatus().name(), d.getBatteryLevel(), d.getFlightHours(), d.getNextMaintenanceOn()))
                .toList();
        return new DroneReport(drones.size(), byStatus.getOrDefault(DroneStatus.DISPONIBLE, 0L),
                byStatus.getOrDefault(DroneStatus.EN_MISION, 0L), byStatus.getOrDefault(DroneStatus.EN_MANTENIMIENTO, 0L),
                byStatus.getOrDefault(DroneStatus.FUERA_DE_SERVICIO, 0L), hours, rows);
    }

    @Transactional(readOnly = true)
    public PilotReport pilotReport() {
        LocalDate today = LocalDate.now();
        List<Pilot> pilots = pilotRepository.findByActiveTrue().stream().sorted(Comparator.comparing(Pilot::getFullName)).toList();
        List<PilotReport.Row> rows = pilots.stream()
                .map(p -> new PilotReport.Row(p.getFullName(), p.getLicenseNumber(), p.getLicenseExpiresOn(),
                        p.hasValidLicenseOn(today), p.getStatus().name(), p.getFlightHours(),
                        assignmentRepository.countByPilotIdAndActiveTrue(p.getId()),
                        assignmentRepository.countByPilotIdAndActiveTrueAndMissionStatus(p.getId(), MissionStatus.FINALIZADA)))
                .toList();
        long available = pilots.stream().filter(p -> p.getStatus() == PilotStatus.DISPONIBLE).count();
        long expired = pilots.stream().filter(p -> !p.hasValidLicenseOn(today)).count();
        BigDecimal hours = pilots.stream().map(Pilot::getFlightHours).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PilotReport(pilots.size(), available, expired, hours, rows);
    }

    @Transactional(readOnly = true)
    public MaintenanceReport maintenanceReport() {
        List<Maintenance> all = maintenanceRepository.findAllByOrderByDateDesc();
        Map<MaintenanceStatus, Long> byStatus = all.stream().collect(Collectors.groupingBy(Maintenance::getStatus, Collectors.counting()));
        List<Maintenance> counted = all.stream().filter(m -> m.getStatus() != MaintenanceStatus.CANCELADO).toList();
        BigDecimal total = counted.stream().map(Maintenance::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<Drone, List<Maintenance>> byDrone = counted.stream().collect(Collectors.groupingBy(Maintenance::getDrone));
        List<MaintenanceReport.CostByDrone> costByDrone = byDrone.entrySet().stream()
                .map(e -> new MaintenanceReport.CostByDrone(e.getKey().getCode(), e.getKey().getName(), e.getValue().size(),
                        e.getValue().stream().map(Maintenance::getCost).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .sorted(Comparator.comparing(MaintenanceReport.CostByDrone::cost).reversed())
                .toList();
        List<MaintenanceReport.Row> rows = all.stream()
                .map(m -> new MaintenanceReport.Row(m.getDate(), m.getDrone().getCode(), m.getType().name(), m.getStatus().name(),
                        m.getTechnician().getFullName(), m.getCost(), m.getDescription()))
                .toList();
        return new MaintenanceReport(all.size(), byStatus.getOrDefault(MaintenanceStatus.PROGRAMADO, 0L),
                byStatus.getOrDefault(MaintenanceStatus.EN_PROCESO, 0L), byStatus.getOrDefault(MaintenanceStatus.COMPLETADO, 0L),
                byStatus.getOrDefault(MaintenanceStatus.CANCELADO, 0L), total, costByDrone, rows);
    }

    @Transactional
    public PdfFile missionPdf(Long missionId) {
        MissionReport report = missionReport(missionId);
        recordMissionReport(missionRepository.getReferenceById(missionId), currentUserService.getEntity());
        return new PdfFile("reporte-" + report.code() + ".pdf", pdfGenerator.missionReport(report));
    }

    @Transactional
    public PdfFile dronePdf() {
        DroneReport r = droneReport();
        recordGlobalReport(ReportType.DRONES, r.total() + " drones: " + r.available() + " disponibles, "
                + r.inMission() + " en misión, " + r.inMaintenance() + " en mantenimiento. " + r.totalFlightHours() + " h de vuelo.");
        return new PdfFile("reporte-drones-" + LocalDate.now() + ".pdf", pdfGenerator.droneReport(r));
    }

    @Transactional
    public PdfFile pilotPdf() {
        PilotReport r = pilotReport();
        recordGlobalReport(ReportType.PILOTOS, r.total() + " pilotos, " + r.available() + " disponibles, "
                + r.expiredLicenses() + " con licencia vencida. " + r.totalFlightHours() + " h de vuelo.");
        return new PdfFile("reporte-pilotos-" + LocalDate.now() + ".pdf", pdfGenerator.pilotReport(r));
    }

    @Transactional
    public PdfFile maintenancePdf() {
        MaintenanceReport r = maintenanceReport();
        recordGlobalReport(ReportType.MANTENIMIENTO, r.total() + " mantenimientos (" + r.completed()
                + " completados). Costo total: $" + r.totalCost());
        return new PdfFile("reporte-mantenimiento-" + LocalDate.now() + ".pdf", pdfGenerator.maintenanceReport(r));
    }

    private void recordGlobalReport(ReportType type, String summary) {
        String title = switch (type) {
            case DRONES -> "Reporte de drones";
            case PILOTOS -> "Reporte de pilotos";
            case MANTENIMIENTO -> "Reporte de mantenimiento";
            case MISION -> throw new BusinessRuleException("Usa el reporte de misión");
        };
        record(type, title, null, currentUserService.getEntity(), summary);
    }


    private MissionReport buildMissionReport(Mission mission) {
        Client c = mission.getClient();
        Optional<MissionAssignment> assignment = mission.getActiveAssignment();
        MissionReport.DroneInfo drone = assignment.map(MissionAssignment::getDrone)
                .map(d -> new MissionReport.DroneInfo(d.getCode(), d.getName(), d.getManufacturer(), d.getModel(), d.getType().name()))
                .orElse(null);
        MissionReport.PilotInfo pilot = assignment.map(MissionAssignment::getPilot)
                .map(p -> new MissionReport.PilotInfo(p.getFullName(), p.getLicenseNumber(), p.getLicenseExpiresOn()))
                .orElse(null);
        Long duration = mission.getActualStart() != null && mission.getActualEnd() != null
                ? Duration.between(mission.getActualStart(), mission.getActualEnd()).toMinutes() : null;
        return new MissionReport(mission.getId(), mission.getCode(), mission.getName(), mission.getType().name(),
                mission.getStatus().name(), mission.getPriority().name(), mission.getObjective(), mission.getLocation(),
                mission.getLatitude(), mission.getLongitude(), mission.getScheduledDate(), mission.getStartTime(),
                mission.getEstimatedEndTime(), mission.getActualStart(), mission.getActualEnd(), duration,
                mission.getObservations(),
                new MissionReport.ClientInfo(c.getName(), c.getType().name(), c.getDocument(), c.getEmail(), c.getPhone(), c.getCity()),
                drone, pilot, strategyResolver.forType(mission.getType()).name(), mission.getFlightPlan().size(),
                telemetryService.summarize(mission.getId()),
                incidentService.findByMission(mission.getId()),
                evidenceService.findByMission(mission.getId()),
                TelemetryService.SIMULATION_NOTICE);
    }

    private Report record(ReportType type, String title, Mission mission, User user, String summary) {
        Report report = new Report();
        report.setType(type);
        report.setTitle(title);
        report.setMission(mission);
        report.setGeneratedBy(user);
        report.setSummary(summary.length() > 2000 ? summary.substring(0, 2000) : summary);
        return reportRepository.save(report);
    }

    private static ReportResponse toResponse(Report r) {
        return new ReportResponse(r.getId(), r.getType().name(), r.getTitle(),
                r.getMission() != null ? r.getMission().getId() : null,
                r.getMission() != null ? r.getMission().getCode() : null,
                r.getGeneratedBy() != null ? r.getGeneratedBy().getFullName() : "Sistema",
                r.getGeneratedAt(), r.getSummary());
    }
}
