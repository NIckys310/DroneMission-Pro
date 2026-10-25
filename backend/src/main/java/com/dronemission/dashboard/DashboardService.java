package com.dronemission.dashboard;

import com.dronemission.dashboard.dto.DashboardResponse;
import com.dronemission.dashboard.dto.DashboardResponse.*;
import com.dronemission.drones.Drone;
import com.dronemission.drones.DroneRepository;
import com.dronemission.drones.DroneStatus;
import com.dronemission.incidents.IncidentRepository;
import com.dronemission.incidents.IncidentService;
import com.dronemission.incidents.IncidentSeverity;
import com.dronemission.incidents.IncidentStatus;
import com.dronemission.incidents.dto.IncidentResponse;
import com.dronemission.maintenance.MaintenanceRepository;
import com.dronemission.maintenance.MaintenanceService;
import com.dronemission.maintenance.MaintenanceStatus;
import com.dronemission.missions.*;
import com.dronemission.pilots.Pilot;
import com.dronemission.pilots.PilotRepository;
import com.dronemission.pilots.PilotStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Calcula los indicadores del dashboard a partir de los datos reales. */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int LOW_BATTERY_ALERT = 30;

    private final DroneRepository droneRepository;
    private final PilotRepository pilotRepository;
    private final MissionRepository missionRepository;
    private final IncidentRepository incidentRepository;
    private final MaintenanceRepository maintenanceRepository;

    @Transactional(readOnly = true)
    public DashboardResponse build() {
        LocalDate today = LocalDate.now();
        List<Drone> drones = droneRepository.findByActiveTrue();
        List<Pilot> pilots = pilotRepository.findByActiveTrue();
        List<Mission> missions = missionRepository.findAll();

        Map<DroneStatus, Long> droneCount = drones.stream().collect(Collectors.groupingBy(Drone::getStatus, Collectors.counting()));
        BigDecimal totalHours = drones.stream().map(Drone::getFlightHours).reduce(BigDecimal.ZERO, BigDecimal::add);
        DroneStats droneStats = new DroneStats(drones.size(), droneCount.getOrDefault(DroneStatus.DISPONIBLE, 0L),
                droneCount.getOrDefault(DroneStatus.EN_MISION, 0L), droneCount.getOrDefault(DroneStatus.EN_MANTENIMIENTO, 0L),
                droneCount.getOrDefault(DroneStatus.FUERA_DE_SERVICIO, 0L), totalHours);

        Map<PilotStatus, Long> pilotCount = pilots.stream().collect(Collectors.groupingBy(Pilot::getStatus, Collectors.counting()));
        PilotStats pilotStats = new PilotStats(pilots.size(), pilotCount.getOrDefault(PilotStatus.DISPONIBLE, 0L),
                pilotCount.getOrDefault(PilotStatus.EN_MISION, 0L), pilotCount.getOrDefault(PilotStatus.LICENCIA_VENCIDA, 0L));

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (MissionStatus s : MissionStatus.values()) {
            byStatus.put(s.name(), missions.stream().filter(m -> m.getStatus() == s).count());
        }
        List<CountItem> byType = Arrays.stream(MissionType.values())
                .map(t -> new CountItem(t.name(), missions.stream().filter(m -> m.getType() == t).count()))
                .toList();
        List<HoursItem> hoursByDrone = drones.stream()
                .sorted(Comparator.comparing(Drone::getFlightHours).reversed())
                .map(d -> new HoursItem(d.getCode(), d.getFlightHours()))
                .toList();

        // Misiones por día: 7 días atrás y 7 hacia adelante
        List<DayItem> perDay = new ArrayList<>();
        for (int offset = -7; offset <= 7; offset++) {
            LocalDate day = today.plusDays(offset);
            long scheduled = missions.stream().filter(m -> m.getScheduledDate().equals(day) && m.getStatus() != MissionStatus.CANCELADA).count();
            long finished = missions.stream().filter(m -> m.getStatus() == MissionStatus.FINALIZADA
                    && m.getActualEnd() != null && m.getActualEnd().toLocalDate().equals(day)).count();
            perDay.add(new DayItem(day, scheduled, finished));
        }

        List<MissionItem> active = missions.stream().filter(m -> m.getStatus().isInOperation())
                .map(DashboardService::toItem).toList();
        List<MissionItem> upcoming = missions.stream()
                .filter(m -> (m.getStatus() == MissionStatus.PROGRAMADA || m.getStatus() == MissionStatus.PENDIENTE)
                        && !m.getScheduledDate().isBefore(today))
                .sorted(Comparator.comparing(Mission::getScheduledDate).thenComparing(Mission::getStartTime))
                .limit(6).map(DashboardService::toItem).toList();

        List<IncidentResponse> incidents = incidentRepository.findAllByOrderByDateDesc(PageRequest.of(0, 6)).stream()
                .map(IncidentService::toResponse).toList();
        var pendingMaintenance = maintenanceRepository
                .findByStatusInOrderByDateAsc(EnumSet.of(MaintenanceStatus.PROGRAMADO, MaintenanceStatus.EN_PROCESO)).stream()
                .map(MaintenanceService::toResponse).toList();

        return new DashboardResponse(droneStats, pilotStats, byStatus, byType, hoursByDrone, perDay, active, upcoming,
                incidents, pendingMaintenance, buildAlerts(today, drones, pilots), incidentRepository.countByStatusNot(IncidentStatus.RESUELTO));
    }

    private List<Alert> buildAlerts(LocalDate today, List<Drone> drones, List<Pilot> pilots) {
        List<Alert> alerts = new ArrayList<>();
        long critical = incidentRepository.countBySeverityAndStatusNot(IncidentSeverity.CRITICA, IncidentStatus.RESUELTO);
        if (critical > 0) {
            alerts.add(new Alert("error", critical + " incidente(s) crítico(s) sin resolver", "/incidents"));
        }
        drones.stream().filter(d -> d.getNextMaintenanceOn() != null && !d.getNextMaintenanceOn().isAfter(today.plusDays(7))
                        && d.getStatus() != DroneStatus.EN_MANTENIMIENTO)
                .forEach(d -> alerts.add(new Alert(d.getNextMaintenanceOn().isBefore(today) ? "error" : "warning",
                        "Drone " + d.getCode() + ": mantenimiento " + (d.getNextMaintenanceOn().isBefore(today) ? "vencido desde " : "previsto para ")
                                + d.getNextMaintenanceOn(), "/drones/" + d.getId())));
        drones.stream().filter(d -> d.getBatteryLevel() < LOW_BATTERY_ALERT && d.getStatus() == DroneStatus.DISPONIBLE)
                .forEach(d -> alerts.add(new Alert("warning", "Drone " + d.getCode() + " con batería baja (" + d.getBatteryLevel() + " %)",
                        "/drones/" + d.getId())));
        pilots.stream().filter(p -> !p.hasValidLicenseOn(today))
                .forEach(p -> alerts.add(new Alert("error", "Licencia vencida: " + p.getFullName(), "/pilots/" + p.getId())));
        pilots.stream().filter(p -> p.hasValidLicenseOn(today) && !p.hasValidLicenseOn(today.plusDays(30)))
                .forEach(p -> alerts.add(new Alert("warning", "La licencia de " + p.getFullName() + " vence el " + p.getLicenseExpiresOn(),
                        "/pilots/" + p.getId())));
        return alerts;
    }

    private static MissionItem toItem(Mission m) {
        var a = m.getActiveAssignment();
        return new MissionItem(m.getId(), m.getCode(), m.getName(), m.getStatus().name(), m.getType().name(),
                m.getScheduledDate(), m.getStartTime(), m.getClient().getName(),
                a.map(x -> x.getDrone().getCode()).orElse(null), a.map(x -> x.getPilot().getFullName()).orElse(null));
    }
}
