package com.dronemission.missions;

import com.dronemission.auth.CurrentUserService;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.drones.Drone;
import com.dronemission.drones.DroneStatus;
import com.dronemission.missions.dto.MissionResponse;
import com.dronemission.missions.event.MissionEvent;
import com.dronemission.missions.event.MissionEventPublisher;
import com.dronemission.missions.event.MissionEventType;
import com.dronemission.pilots.Pilot;
import com.dronemission.pilots.PilotStatus;
import com.dronemission.reports.ReportService;
import com.dronemission.telemetry.TelemetryService;
import com.dronemission.telemetry.dto.TelemetrySummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ciclo de vida de la misión (módulo 10). Cada método es una transición de la
 * máquina de estados; Mission.changeStatus() rechaza las transiciones inválidas.
 *
 *  Operador: enviar (BORRADOR->PENDIENTE), asignar (->PROGRAMADA, en AssignmentService), cancelar.
 *  Piloto:   preparar, despegar, pausar, reanudar y finalizar.
 */
@Service
@RequiredArgsConstructor
public class MissionLifecycleService {

    private final MissionService missionService;
    private final AssignmentService assignmentService;
    private final MissionAccessService accessService;
    private final MissionEventPublisher eventPublisher;
    private final TelemetryService telemetryService;
    private final ReportService reportService;
    private final CurrentUserService currentUserService;

    /** BORRADOR -> PENDIENTE: la misión queda lista para que se le asignen recursos. */
    @Transactional
    public MissionResponse submit(Long id) {
        Mission mission = missionService.get(id);
        if (mission.getFlightPlan().isEmpty()) {
            throw new BusinessRuleException("La misión no tiene plan de vuelo");
        }
        mission.changeStatus(MissionStatus.PENDIENTE);
        eventPublisher.publish(new MissionEvent(MissionEventType.ENVIADA, mission, "Enviada a planificación"));
        return missionService.toResponse(mission);
    }

    /**
     * PROGRAMADA -> EN_PREPARACION: chequeo previo al vuelo. Se vuelven a validar
     * piloto, licencia, drone y batería; el plan de vuelo se "carga" en el drone virtual
     * y ambos recursos quedan EN_MISION.
     */
    @Transactional
    public MissionResponse prepare(Long id) {
        Mission mission = missionService.get(id);
        accessService.checkExecute(mission);
        mission.assertCanTransitionTo(MissionStatus.EN_PREPARACION);
        MissionAssignment assignment = assignmentService.validateReadyForFlight(mission);
        mission.changeStatus(MissionStatus.EN_PREPARACION);
        assignment.getDrone().setStatus(DroneStatus.EN_MISION);
        assignment.getPilot().setStatus(PilotStatus.EN_MISION);
        eventPublisher.publish(new MissionEvent(MissionEventType.PREPARADA, mission,
                "Chequeo prevuelo completado; plan de vuelo de " + mission.getFlightPlan().size() + " puntos cargado en "
                        + assignment.getDrone().getCode()));
        return missionService.toResponse(mission);
    }

    /** EN_PREPARACION -> EN_VUELO: el simulador empieza a generar telemetría. */
    @Transactional
    public MissionResponse start(Long id) {
        Mission mission = missionService.get(id);
        accessService.checkExecute(mission);
        if (mission.getActiveAssignment().isEmpty()) {
            throw new BusinessRuleException("La misión no puede iniciarse sin drone y piloto asignados");
        }
        mission.changeStatus(MissionStatus.EN_VUELO);
        mission.setActualStart(LocalDateTime.now());
        eventPublisher.publish(new MissionEvent(MissionEventType.INICIADA, mission, "Despegue"));
        return missionService.toResponse(mission);
    }

    /** EN_VUELO -> PAUSADA: el drone virtual deja de avanzar. */
    @Transactional
    public MissionResponse pause(Long id, String reason) {
        Mission mission = missionService.get(id);
        accessService.checkExecute(mission);
        mission.changeStatus(MissionStatus.PAUSADA);
        eventPublisher.publish(new MissionEvent(MissionEventType.PAUSADA, mission, reason));
        return missionService.toResponse(mission);
    }

    /** PAUSADA -> EN_VUELO. */
    @Transactional
    public MissionResponse resume(Long id) {
        Mission mission = missionService.get(id);
        accessService.checkExecute(mission);
        mission.changeStatus(MissionStatus.EN_VUELO);
        eventPublisher.publish(new MissionEvent(MissionEventType.REANUDADA, mission, null));
        return missionService.toResponse(mission);
    }

    /**
     * EN_VUELO | PAUSADA -> FINALIZADA. Suma horas de vuelo al drone y al piloto,
     * actualiza la batería del drone, libera ambos recursos y genera el reporte.
     */
    @Transactional
    public MissionResponse finish(Long id, String observations) {
        Mission mission = missionService.get(id);
        accessService.checkExecute(mission);
        mission.changeStatus(MissionStatus.FINALIZADA);
        LocalDateTime now = LocalDateTime.now();
        mission.setActualEnd(now);
        mission.appendObservation(observations);

        MissionAssignment assignment = mission.getActiveAssignment()
                .orElseThrow(() -> new BusinessRuleException("La misión no tiene recursos asignados"));
        TelemetrySummary summary = telemetryService.summarize(id);
        long flightSeconds = summary.readings() > 0 ? summary.flightTimeS()
                : Duration.between(mission.getActualStart() != null ? mission.getActualStart() : now, now).getSeconds();
        BigDecimal hours = BigDecimal.valueOf(flightSeconds).divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);

        Drone drone = assignment.getDrone();
        drone.addFlightHours(hours);
        if (summary.readings() > 0) {
            drone.setBatteryLevel(summary.endBattery());
        }
        drone.setStatus(DroneStatus.DISPONIBLE);

        Pilot pilot = assignment.getPilot();
        pilot.addFlightHours(hours);
        pilot.setStatus(PilotStatus.DISPONIBLE);
        pilot.refreshLicenseStatus(LocalDate.now());

        reportService.recordMissionReport(mission, currentUserService.getEntity());
        eventPublisher.publish(new MissionEvent(MissionEventType.FINALIZADA, mission,
                "Vuelo de " + hours + " h; distancia " + Math.round(summary.distanceM()) + " m"));
        return missionService.toResponse(mission);
    }

    /** -> CANCELADA (solo antes del vuelo). Si estaba en preparación, libera drone y piloto. */
    @Transactional
    public MissionResponse cancel(Long id, String reason) {
        Mission mission = missionService.get(id);
        boolean wasPreparing = mission.getStatus() == MissionStatus.EN_PREPARACION;
        mission.changeStatus(MissionStatus.CANCELADA);
        mission.appendObservation("Cancelada: " + reason);
        if (wasPreparing) {
            mission.getActiveAssignment().ifPresent(a -> {
                a.getDrone().setStatus(DroneStatus.DISPONIBLE);
                a.getPilot().setStatus(PilotStatus.DISPONIBLE);
            });
        }
        eventPublisher.publish(new MissionEvent(MissionEventType.CANCELADA, mission, reason));
        return missionService.toResponse(mission);
    }
}
