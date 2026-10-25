package com.dronemission.missions;

import com.dronemission.audit.AuditService;
import com.dronemission.audit.dto.AuditResponse;
import com.dronemission.auth.AuthenticatedUser;
import com.dronemission.auth.CurrentUserService;
import com.dronemission.clients.Client;
import com.dronemission.clients.ClientService;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.common.exception.InvalidStateTransitionException;
import com.dronemission.common.exception.ResourceNotFoundException;
import com.dronemission.missions.dto.FlightPlanResponse;
import com.dronemission.missions.dto.MissionRequest;
import com.dronemission.missions.dto.MissionResponse;
import com.dronemission.missions.event.MissionEvent;
import com.dronemission.missions.event.MissionEventPublisher;
import com.dronemission.missions.event.MissionEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/** CRUD y consultas de misiones. Los cambios de estado viven en MissionLifecycleService. */
@Slf4j
@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final ClientService clientService;
    private final FlightPlanService flightPlanService;
    private final MissionAccessService accessService;
    private final MissionEventPublisher eventPublisher;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    /** Lista las misiones visibles para el usuario, con filtros opcionales. */
    @Transactional(readOnly = true)
    public List<MissionResponse> findAll(MissionStatus status, MissionType type, Long clientId, String query) {
        AuthenticatedUser user = currentUserService.get();
        String q = query == null ? "" : query.trim().toLowerCase();
        return missionRepository.findAllByOrderByScheduledDateDescStartTimeDesc().stream()
                .filter(m -> accessService.canView(m, user))
                .filter(m -> status == null || m.getStatus() == status)
                .filter(m -> type == null || m.getType() == type)
                .filter(m -> clientId == null || m.getClient().getId().equals(clientId))
                .filter(m -> q.isEmpty() || m.getCode().toLowerCase().contains(q) || m.getName().toLowerCase().contains(q)
                        || m.getLocation().toLowerCase().contains(q) || m.getClient().getName().toLowerCase().contains(q))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MissionResponse findById(Long id) {
        Mission mission = get(id);
        accessService.checkView(mission);
        return toResponse(mission);
    }

    @Transactional
    public MissionResponse create(MissionRequest req) {
        Client client = clientService.get(req.clientId());
        Mission mission = new MissionBuilder()
                .code(nextCode())
                .name(req.name())
                .description(req.description())
                .type(req.type())
                .client(client)
                .location(req.location(), req.latitude(), req.longitude())
                .altitude(req.targetAltitudeM())
                .schedule(req.scheduledDate(), req.startTime(), req.estimatedEndTime())
                .priority(req.priority())
                .objective(req.objective())
                .observations(req.observations())
                .createdBy(currentUserService.getEntity())
                .build();
        flightPlanService.generate(mission);
        missionRepository.save(mission);
        eventPublisher.publish(new MissionEvent(MissionEventType.CREADA, mission, "Misión creada en estado BORRADOR"));
        return toResponse(mission);
    }

    @Transactional
    public MissionResponse update(Long id, MissionRequest req) {
        Mission mission = get(id);
        if (!mission.getStatus().isEditable()) {
            throw new InvalidStateTransitionException("La misión " + mission.getCode() + " está " + mission.getStatus()
                    + " y ya no se puede editar");
        }
        boolean scheduleChanged = !req.scheduledDate().equals(mission.getScheduledDate())
                || !req.startTime().equals(mission.getStartTime()) || !req.estimatedEndTime().equals(mission.getEstimatedEndTime());
        boolean planChanged = req.type() != mission.getType() || !Objects.equals(req.latitude(), mission.getLatitude())
                || !Objects.equals(req.longitude(), mission.getLongitude()) || !req.targetAltitudeM().equals(mission.getTargetAltitudeM());

        MissionBuilder.validateSchedule(req.scheduledDate(), req.startTime(), req.estimatedEndTime(), !scheduleChanged);
        MissionBuilder.validateAltitude(req.targetAltitudeM());
        if (mission.getStatus() == MissionStatus.PROGRAMADA && (scheduleChanged || req.type() != mission.getType())) {
            throw new BusinessRuleException("La misión ya tiene drone y piloto asignados para ese horario: no se puede cambiar "
                    + "la fecha, el horario ni el tipo. Cancélala y crea una nueva misión.");
        }

        mission.setName(req.name().trim());
        mission.setDescription(req.description());
        mission.setType(req.type());
        mission.setClient(clientService.get(req.clientId()));
        mission.setLocation(req.location().trim());
        mission.setLatitude(req.latitude());
        mission.setLongitude(req.longitude());
        mission.setTargetAltitudeM(req.targetAltitudeM());
        mission.setScheduledDate(req.scheduledDate());
        mission.setStartTime(req.startTime());
        mission.setEstimatedEndTime(req.estimatedEndTime());
        mission.setPriority(req.priority());
        mission.setObjective(req.objective().trim());
        mission.setObservations(req.observations());
        if (planChanged) {
            flightPlanService.generate(mission);
        }
        eventPublisher.publish(new MissionEvent(MissionEventType.ACTUALIZADA, mission,
                planChanged ? "Datos actualizados y plan de vuelo regenerado" : "Datos actualizados"));
        return toResponse(mission);
    }

    /** Solo se eliminan borradores; una misión ya enviada debe cancelarse. */
    @Transactional
    public void delete(Long id) {
        Mission mission = get(id);
        if (mission.getStatus() != MissionStatus.BORRADOR) {
            throw new InvalidStateTransitionException("Solo se pueden eliminar misiones en BORRADOR; cancela la misión "
                    + mission.getCode() + " en su lugar");
        }
        eventPublisher.publish(new MissionEvent(MissionEventType.ELIMINADA, mission, "Borrador eliminado"));
        missionRepository.delete(mission);
    }

    @Transactional(readOnly = true)
    public FlightPlanResponse flightPlan(Long id) {
        Mission mission = get(id);
        accessService.checkView(mission);
        return flightPlanService.describe(mission);
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> history(Long id) {
        Mission mission = get(id);
        accessService.checkView(mission);
        return auditService.findByEntity("MISION", id);
    }

    /** Observación del piloto o del operador durante la operación. */
    @Transactional
    public MissionResponse addObservation(Long id, String text) {
        Mission mission = get(id);
        AuthenticatedUser user = currentUserService.get();
        if (!accessService.canExecute(mission, user) && !accessService.canManage(user)) {
            throw new org.springframework.security.access.AccessDeniedException("No puedes registrar observaciones en esta misión");
        }
        if (text == null || text.isBlank()) {
            throw new BusinessRuleException("La observación no puede estar vacía");
        }
        mission.appendObservation("[" + user.fullName() + "] " + text.trim());
        auditService.log("OBSERVACION", "MISION", id, "Observación: " + text.trim());
        return toResponse(mission);
    }

    /** Al arrancar, genera el plan de vuelo de las misiones que no lo tengan (por ejemplo, las del seed). */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void ensureFlightPlans() {
        List<Mission> missing = missionRepository.findWithoutFlightPlan();
        missing.forEach(flightPlanService::generate);
        if (!missing.isEmpty()) {
            log.info("Planes de vuelo generados para {} misiones", missing.size());
        }
    }

    public Mission get(Long id) {
        return missionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Misión", id));
    }

    public MissionResponse toResponse(Mission mission) {
        return MissionMapper.toResponse(mission, accessService.availableActions(mission));
    }

    private String nextCode() {
        long next = missionRepository.findTopByOrderByIdDesc().map(m -> m.getId() + 1).orElse(1L);
        return String.format("MSN-%04d", next);
    }
}
