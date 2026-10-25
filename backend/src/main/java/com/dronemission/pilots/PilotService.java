package com.dronemission.pilots;

import com.dronemission.audit.AuditService;
import com.dronemission.auth.CurrentUserService;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.common.exception.DuplicateResourceException;
import com.dronemission.common.exception.ResourceNotFoundException;
import com.dronemission.missions.MissionAssignment;
import com.dronemission.missions.MissionAssignmentRepository;
import com.dronemission.missions.MissionStatus;
import com.dronemission.pilots.dto.PilotMissionEntry;
import com.dronemission.pilots.dto.PilotRequest;
import com.dronemission.pilots.dto.PilotResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PilotService {

    private final PilotRepository pilotRepository;
    private final MissionAssignmentRepository assignmentRepository;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<PilotResponse> findAll(String query, PilotStatus status) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return pilotRepository.findAllByOrderByFirstNameAsc().stream()
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> q.isEmpty() || p.getFullName().toLowerCase().contains(q)
                        || p.getDocument().contains(q) || p.getLicenseNumber().toLowerCase().contains(q))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PilotResponse findById(Long id) {
        return toResponse(get(id));
    }

    /** Perfil del piloto que inició sesión. */
    @Transactional(readOnly = true)
    public PilotResponse findMine() {
        Long userId = currentUserService.get().id();
        return pilotRepository.findByUserId(userId).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Tu usuario no está vinculado a un piloto"));
    }

    @Transactional
    public PilotResponse create(PilotRequest req) {
        validateUnique(req, null);
        Pilot pilot = new Pilot();
        PilotMapper.apply(req, pilot);
        pilot.setStatus(initialStatus(req.status()));
        pilot.refreshLicenseStatus(LocalDate.now());
        pilotRepository.save(pilot);
        auditService.log("CREAR", "PILOTO", pilot.getId(), "Piloto " + pilot.getFullName() + " creado");
        return toResponse(pilot);
    }

    @Transactional
    public PilotResponse update(Long id, PilotRequest req) {
        Pilot pilot = get(id);
        validateUnique(req, id);
        PilotMapper.apply(req, pilot);
        // EN_MISION lo controla el sistema; el usuario solo elige entre disponible / no disponible
        if (pilot.getStatus() != PilotStatus.EN_MISION && req.status() != null) {
            pilot.setStatus(initialStatus(req.status()));
        }
        pilot.refreshLicenseStatus(LocalDate.now());
        auditService.log("ACTUALIZAR", "PILOTO", id, "Piloto " + pilot.getFullName() + " actualizado");
        return toResponse(pilot);
    }

    @Transactional
    public void deactivate(Long id) {
        Pilot pilot = get(id);
        if (assignmentRepository.pilotHasActiveMissions(id, MissionStatus.occupyingResources())) {
            throw new BusinessRuleException("El piloto tiene misiones programadas o en curso; reasígnalas antes de desactivarlo");
        }
        pilot.setActive(false);
        pilot.setStatus(PilotStatus.NO_DISPONIBLE);
        auditService.log("DESACTIVAR", "PILOTO", id, "Piloto " + pilot.getFullName() + " desactivado");
    }

    /** Historial de misiones del piloto. */
    @Transactional(readOnly = true)
    public List<PilotMissionEntry> missionsOf(Long pilotId) {
        get(pilotId);
        return assignmentRepository.findByPilotIdAndActiveTrueOrderByAssignedAtDesc(pilotId).stream()
                .map(PilotService::toEntry)
                .toList();
    }

    private static PilotMissionEntry toEntry(MissionAssignment a) {
        return new PilotMissionEntry(a.getMission().getId(), a.getMission().getCode(), a.getMission().getName(),
                a.getMission().getStatus().name(), a.getMission().getScheduledDate(), a.getDrone().getCode());
    }

    /**
     * Marca como LICENCIA_VENCIDA a los pilotos cuya licencia expiró.
     * Se ejecuta al arrancar y todos los días a la 1:00 a. m.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void refreshLicenseStatuses() {
        LocalDate today = LocalDate.now();
        pilotRepository.findByActiveTrue().forEach(p -> {
            PilotStatus before = p.getStatus();
            p.refreshLicenseStatus(today);
            if (before != p.getStatus()) {
                log.info("Piloto {} cambió de {} a {} por vigencia de licencia", p.getFullName(), before, p.getStatus());
            }
        });
    }

    public Pilot get(Long id) {
        return pilotRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Piloto", id));
    }

    private PilotStatus initialStatus(PilotStatus requested) {
        return requested == PilotStatus.NO_DISPONIBLE ? PilotStatus.NO_DISPONIBLE : PilotStatus.DISPONIBLE;
    }

    private void validateUnique(PilotRequest req, Long id) {
        String doc = req.document().trim();
        String email = req.email().trim();
        String license = req.licenseNumber().trim().toUpperCase();
        boolean docTaken = id == null ? pilotRepository.existsByDocument(doc) : pilotRepository.existsByDocumentAndIdNot(doc, id);
        boolean emailTaken = id == null ? pilotRepository.existsByEmailIgnoreCase(email) : pilotRepository.existsByEmailIgnoreCaseAndIdNot(email, id);
        boolean licenseTaken = id == null ? pilotRepository.existsByLicenseNumber(license) : pilotRepository.existsByLicenseNumberAndIdNot(license, id);
        if (docTaken) throw new DuplicateResourceException("Ya existe un piloto con el documento " + doc);
        if (emailTaken) throw new DuplicateResourceException("Ya existe un piloto con el correo " + email);
        if (licenseTaken) throw new DuplicateResourceException("Ya existe un piloto con la licencia " + license);
    }

    private PilotResponse toResponse(Pilot p) {
        return PilotMapper.toResponse(p, assignmentRepository.countByPilotIdAndActiveTrue(p.getId()));
    }
}
