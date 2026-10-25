package com.dronemission.incidents;

import com.dronemission.audit.AuditService;
import com.dronemission.auth.AuthenticatedUser;
import com.dronemission.auth.CurrentUserService;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.common.exception.ResourceNotFoundException;
import com.dronemission.incidents.dto.IncidentRequest;
import com.dronemission.incidents.dto.IncidentResponse;
import com.dronemission.incidents.dto.IncidentStatusRequest;
import com.dronemission.missions.*;
import com.dronemission.missions.event.MissionEvent;
import com.dronemission.missions.event.MissionEventPublisher;
import com.dronemission.missions.event.MissionEventType;
import com.dronemission.notifications.NotificationService;
import com.dronemission.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final MissionRepository missionRepository;
    private final MissionAccessService accessService;
    private final MissionEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<IncidentResponse> findAll(IncidentSeverity severity, IncidentStatus status, IncidentType type) {
        return incidentRepository.findAllByOrderByDateDesc().stream()
                .filter(i -> severity == null || i.getSeverity() == severity)
                .filter(i -> status == null || i.getStatus() == status)
                .filter(i -> type == null || i.getType() == type)
                .map(IncidentService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> findByMission(Long missionId) {
        Mission mission = getMission(missionId);
        accessService.checkView(mission);
        return incidentRepository.findByMissionIdOrderByDateDesc(missionId).stream().map(IncidentService::toResponse).toList();
    }

    @Transactional
    public IncidentResponse create(Long missionId, IncidentRequest req) {
        Mission mission = getMission(missionId);
        AuthenticatedUser user = currentUserService.get();
        if (!accessService.canContribute(mission, user, "INCIDENTES_REGISTRAR")) {
            throw new AccessDeniedException("No puedes registrar incidentes en la misión " + mission.getCode());
        }
        if (mission.getStatus() == MissionStatus.BORRADOR || mission.getStatus() == MissionStatus.CANCELADA) {
            throw new BusinessRuleException("No se registran incidentes en una misión en borrador o cancelada");
        }
        Incident incident = buildIncident(mission, req.type(), req.severity(), req.description().trim(), currentUserService.getEntity());
        auditService.log("REGISTRAR", "INCIDENTE", incident.getId(), "Incidente " + req.type() + " (" + req.severity()
                + ") en la misión " + mission.getCode());
        handleCritical(mission, incident);
        return toResponse(incident);
    }

    /** Incidente detectado automáticamente (por ejemplo, batería baja en el simulador). */
    @Transactional
    public void registerSystemIncident(Mission mission, IncidentType type, IncidentSeverity severity, String description) {
        if (incidentRepository.existsByMissionIdAndType(mission.getId(), type)) {
            return; // evita alertas repetidas del mismo tipo en la misma misión
        }
        Incident incident = buildIncident(mission, type, severity, description, null);
        auditService.log("REGISTRAR", "INCIDENTE", incident.getId(), "Incidente automático " + type + " en " + mission.getCode());
        List<User> recipients = new ArrayList<>();
        mission.getActiveAssignment().map(a -> a.getPilot().getUser()).ifPresent(recipients::add);
        recipients.add(mission.getCreatedBy());
        notificationService.notify(recipients, "Alerta: " + type, description, mission);
        handleCritical(mission, incident);
    }

    @Transactional
    public IncidentResponse updateStatus(Long id, IncidentStatusRequest req) {
        Incident incident = incidentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Incidente", id));
        if (req.status() == IncidentStatus.RESUELTO && (req.resolution() == null || req.resolution().isBlank())) {
            throw new BusinessRuleException("Para resolver un incidente debes describir la resolución");
        }
        incident.setStatus(req.status());
        if (req.resolution() != null && !req.resolution().isBlank()) {
            incident.setResolution(req.resolution().trim());
        }
        auditService.log("CAMBIAR_ESTADO", "INCIDENTE", id, "Incidente #" + id + " -> " + req.status());
        return toResponse(incident);
    }

    private Incident buildIncident(Mission mission, IncidentType type, IncidentSeverity severity, String description, User reportedBy) {
        Incident incident = new Incident();
        incident.setMission(mission);
        incident.setType(type);
        incident.setSeverity(severity);
        incident.setDescription(description);
        incident.setReportedBy(reportedBy);
        return incidentRepository.save(incident);
    }

    /** Regla: un incidente CRÍTICO avisa a operaciones y pausa automáticamente una misión en vuelo. */
    private void handleCritical(Mission mission, Incident incident) {
        if (incident.getSeverity() != IncidentSeverity.CRITICA) {
            return;
        }
        eventPublisher.publish(new MissionEvent(MissionEventType.INCIDENTE_CRITICO, mission,
                incident.getType() + ": " + incident.getDescription()));
        if (mission.getStatus() == MissionStatus.EN_VUELO) {
            mission.changeStatus(MissionStatus.PAUSADA);
            eventPublisher.publish(new MissionEvent(MissionEventType.PAUSADA, mission, "Pausa automática por incidente crítico"));
        }
    }

    private Mission getMission(Long id) {
        return missionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Misión", id));
    }

    public static IncidentResponse toResponse(Incident i) {
        return new IncidentResponse(i.getId(), i.getMission().getId(), i.getMission().getCode(), i.getMission().getName(),
                i.getDate(), i.getType().name(), i.getSeverity().name(), i.getDescription(),
                i.getReportedBy() != null ? i.getReportedBy().getFullName() : "Sistema (automático)",
                i.getStatus().name(), i.getResolution());
    }
}
