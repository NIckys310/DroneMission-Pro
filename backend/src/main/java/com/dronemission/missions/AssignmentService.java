package com.dronemission.missions;

import com.dronemission.auth.CurrentUserService;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.common.exception.InvalidStateTransitionException;
import com.dronemission.drones.Drone;
import com.dronemission.drones.DroneRepository;
import com.dronemission.drones.DroneStatus;
import com.dronemission.drones.DroneService;
import com.dronemission.missions.dto.AssignRequest;
import com.dronemission.missions.dto.AssignmentOptionsResponse;
import com.dronemission.missions.dto.MissionResponse;
import com.dronemission.missions.event.MissionEvent;
import com.dronemission.missions.event.MissionEventPublisher;
import com.dronemission.missions.event.MissionEventType;
import com.dronemission.missions.strategy.MissionStrategy;
import com.dronemission.missions.strategy.MissionStrategyResolver;
import com.dronemission.pilots.Pilot;
import com.dronemission.pilots.PilotRepository;
import com.dronemission.pilots.PilotService;
import com.dronemission.pilots.PilotStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;

/**
 * Asignación de drones y pilotos a misiones (módulo 11).
 * Todas las validaciones de disponibilidad viven aquí y se reutilizan para:
 *  1) mostrar en el frontend qué recursos son elegibles y por qué,
 *  2) validar la asignación,
 *  3) volver a validar justo antes del vuelo (preparación).
 */
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final MissionService missionService;
    private final MissionAssignmentRepository assignmentRepository;
    private final DroneRepository droneRepository;
    private final PilotRepository pilotRepository;
    private final DroneService droneService;
    private final PilotService pilotService;
    private final MissionStrategyResolver strategyResolver;
    private final MissionEventPublisher eventPublisher;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public AssignmentOptionsResponse options(Long missionId) {
        Mission mission = missionService.get(missionId);
        MissionStrategy strategy = strategyResolver.forType(mission.getType());
        var drones = droneRepository.findByActiveTrue().stream()
                .sorted(Comparator.comparing(Drone::getCode))
                .map(d -> {
                    Optional<String> reason = droneIneligibility(mission, d, strategy);
                    return new AssignmentOptionsResponse.DroneOption(d.getId(), d.getCode(), d.getName(),
                            d.getType().name(), d.getStatus().name(), d.getBatteryLevel(), reason.isEmpty(), reason.orElse(null));
                })
                .sorted(Comparator.comparing(o -> !o.eligible()))
                .toList();
        var pilots = pilotRepository.findByActiveTrue().stream()
                .sorted(Comparator.comparing(Pilot::getFullName))
                .map(p -> {
                    Optional<String> reason = pilotIneligibility(mission, p);
                    return new AssignmentOptionsResponse.PilotOption(p.getId(), p.getFullName(), p.getStatus().name(),
                            p.getLicenseExpiresOn().toString(), reason.isEmpty(), reason.orElse(null));
                })
                .sorted(Comparator.comparing(o -> !o.eligible()))
                .toList();
        return new AssignmentOptionsResponse(strategy.minimumBatteryPercent(), drones, pilots);
    }

    @Transactional
    public MissionResponse assign(Long missionId, AssignRequest request) {
        Mission mission = missionService.get(missionId);
        if (mission.getStatus() != MissionStatus.PENDIENTE && mission.getStatus() != MissionStatus.PROGRAMADA) {
            throw new InvalidStateTransitionException("Solo se pueden asignar recursos a misiones PENDIENTES o PROGRAMADAS (estado actual: "
                    + mission.getStatus() + ")");
        }
        Drone drone = droneService.get(request.droneId());
        Pilot pilot = pilotService.get(request.pilotId());
        MissionStrategy strategy = strategyResolver.forType(mission.getType());

        droneIneligibility(mission, drone, strategy).ifPresent(reason -> {
            throw new BusinessRuleException(reason);
        });
        pilotIneligibility(mission, pilot).ifPresent(reason -> {
            throw new BusinessRuleException(reason);
        });

        // Se conserva el historial: la asignación anterior queda inactiva
        mission.getActiveAssignment().ifPresent(previous -> previous.setActive(false));
        MissionAssignment assignment = new MissionAssignment();
        assignment.setMission(mission);
        assignment.setDrone(drone);
        assignment.setPilot(pilot);
        assignment.setAssignedBy(currentUserService.getEntity());
        assignmentRepository.save(assignment);
        mission.getAssignments().add(assignment);

        if (mission.getStatus() == MissionStatus.PENDIENTE) {
            mission.changeStatus(MissionStatus.PROGRAMADA);
        }
        eventPublisher.publish(new MissionEvent(MissionEventType.ASIGNADA, mission,
                "Drone " + drone.getCode() + " y piloto " + pilot.getFullName() + " asignados"));
        return missionService.toResponse(mission);
    }

    /** Devuelve el motivo por el que el drone NO se puede asignar, o vacío si es elegible. */
    public Optional<String> droneIneligibility(Mission mission, Drone drone, MissionStrategy strategy) {
        if (!drone.isActive()) {
            return Optional.of("El drone " + drone.getCode() + " está dado de baja");
        }
        if (drone.getStatus() == DroneStatus.EN_MANTENIMIENTO) {
            return Optional.of("El drone " + drone.getCode() + " está en mantenimiento");
        }
        if (drone.getStatus() == DroneStatus.FUERA_DE_SERVICIO) {
            return Optional.of("El drone " + drone.getCode() + " está fuera de servicio");
        }
        if (!strategy.compatibleDroneTypes().contains(drone.getType())) {
            return Optional.of("Un drone de tipo " + drone.getType() + " no es compatible con una misión de "
                    + mission.getType() + " (compatibles: " + strategy.compatibleDroneTypes() + ")");
        }
        if (!drone.hasBatteryAtLeast(strategy.minimumBatteryPercent())) {
            return Optional.of("El drone " + drone.getCode() + " tiene " + drone.getBatteryLevel()
                    + " % de batería; se requiere al menos " + strategy.minimumBatteryPercent() + " %");
        }
        if (assignmentRepository.droneHasOverlap(drone.getId(), mission.getId(), MissionStatus.occupyingResources(),
                mission.getScheduledDate(), mission.getStartTime(), mission.getEstimatedEndTime())) {
            return Optional.of("El drone " + drone.getCode() + " ya tiene otra misión en ese horario");
        }
        return Optional.empty();
    }

    /** Devuelve el motivo por el que el piloto NO se puede asignar, o vacío si es elegible. */
    public Optional<String> pilotIneligibility(Mission mission, Pilot pilot) {
        if (!pilot.isActive()) {
            return Optional.of("El piloto " + pilot.getFullName() + " está inactivo");
        }
        if (!pilot.hasValidLicenseOn(LocalDate.now()) || pilot.getStatus() == PilotStatus.LICENCIA_VENCIDA) {
            return Optional.of("La licencia del piloto " + pilot.getFullName() + " está vencida");
        }
        if (!pilot.hasValidLicenseOn(mission.getScheduledDate())) {
            return Optional.of("La licencia del piloto " + pilot.getFullName() + " vence antes de la fecha de la misión");
        }
        if (pilot.getStatus() == PilotStatus.NO_DISPONIBLE) {
            return Optional.of("El piloto " + pilot.getFullName() + " no está disponible");
        }
        if (assignmentRepository.pilotHasOverlap(pilot.getId(), mission.getId(), MissionStatus.occupyingResources(),
                mission.getScheduledDate(), mission.getStartTime(), mission.getEstimatedEndTime())) {
            return Optional.of("El piloto " + pilot.getFullName() + " ya tiene otra misión en ese horario");
        }
        return Optional.empty();
    }

    /**
     * Validación final antes del vuelo (reglas 1-7): la misión debe tener drone y piloto,
     * y ambos deben estar disponibles en este momento.
     */
    public MissionAssignment validateReadyForFlight(Mission mission) {
        MissionAssignment assignment = mission.getActiveAssignment()
                .orElseThrow(() -> new BusinessRuleException("La misión no puede iniciarse sin drone y piloto asignados"));
        Drone drone = assignment.getDrone();
        Pilot pilot = assignment.getPilot();
        MissionStrategy strategy = strategyResolver.forType(mission.getType());

        if (!pilot.hasValidLicenseOn(LocalDate.now())) {
            throw new BusinessRuleException("El piloto " + pilot.getFullName() + " tiene la licencia vencida y no puede iniciar la misión");
        }
        if (pilot.getStatus() != PilotStatus.DISPONIBLE) {
            throw new BusinessRuleException("El piloto " + pilot.getFullName() + " no está disponible (estado: " + pilot.getStatus() + ")");
        }
        if (drone.getStatus() != DroneStatus.DISPONIBLE) {
            throw new BusinessRuleException("El drone " + drone.getCode() + " no está disponible (estado: " + drone.getStatus() + ")");
        }
        if (!drone.hasBatteryAtLeast(strategy.minimumBatteryPercent())) {
            throw new BusinessRuleException("El drone " + drone.getCode() + " tiene " + drone.getBatteryLevel()
                    + " % de batería; se requiere al menos " + strategy.minimumBatteryPercent() + " %");
        }
        return assignment;
    }
}
