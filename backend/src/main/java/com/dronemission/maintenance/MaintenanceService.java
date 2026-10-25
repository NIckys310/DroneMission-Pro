package com.dronemission.maintenance;

import com.dronemission.audit.AuditService;
import com.dronemission.auth.CurrentUserService;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.common.exception.InvalidStateTransitionException;
import com.dronemission.common.exception.ResourceNotFoundException;
import com.dronemission.drones.Drone;
import com.dronemission.drones.DroneService;
import com.dronemission.drones.DroneStatus;
import com.dronemission.maintenance.dto.MaintenanceCompleteRequest;
import com.dronemission.maintenance.dto.MaintenanceRequest;
import com.dronemission.maintenance.dto.MaintenanceResponse;
import com.dronemission.users.RoleName;
import com.dronemission.users.User;
import com.dronemission.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Gestión de mantenimientos (módulo 15).
 * Regla: cuando un mantenimiento pasa a EN_PROCESO el drone queda EN_MANTENIMIENTO;
 * cuando se completa (o cancela) el drone vuelve a DISPONIBLE.
 */
@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final DroneService droneService;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<MaintenanceResponse> findAll(MaintenanceStatus status, MaintenanceType type, Long droneId) {
        return maintenanceRepository.findAllByOrderByDateDesc().stream()
                .filter(m -> status == null || m.getStatus() == status)
                .filter(m -> type == null || m.getType() == type)
                .filter(m -> droneId == null || m.getDrone().getId().equals(droneId))
                .map(MaintenanceService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MaintenanceResponse findById(Long id) {
        return toResponse(get(id));
    }

    @Transactional
    public MaintenanceResponse create(MaintenanceRequest req) {
        Drone drone = droneService.get(req.droneId());
        if (!drone.isActive()) {
            throw new BusinessRuleException("El drone " + drone.getCode() + " está dado de baja");
        }
        Maintenance m = new Maintenance();
        m.setDrone(drone);
        m.setTechnician(resolveTechnician(req.technicianId()));
        applyEditableFields(m, req);
        m.setStatus(MaintenanceStatus.PROGRAMADO);
        maintenanceRepository.save(m);
        auditService.log("CREAR", "MANTENIMIENTO", m.getId(), "Mantenimiento " + m.getType() + " programado para " + drone.getCode());
        if (Boolean.TRUE.equals(req.startNow())) {
            start(m.getId());
        }
        return toResponse(m);
    }

    @Transactional
    public MaintenanceResponse update(Long id, MaintenanceRequest req) {
        Maintenance m = get(id);
        if (m.getStatus() == MaintenanceStatus.COMPLETADO || m.getStatus() == MaintenanceStatus.CANCELADO) {
            throw new InvalidStateTransitionException("Un mantenimiento " + m.getStatus() + " no se puede editar");
        }
        if (!m.getDrone().getId().equals(req.droneId())) {
            if (m.getStatus() == MaintenanceStatus.EN_PROCESO) {
                throw new BusinessRuleException("No se puede cambiar el drone de un mantenimiento en proceso");
            }
            m.setDrone(droneService.get(req.droneId()));
        }
        if (req.technicianId() != null) {
            m.setTechnician(resolveTechnician(req.technicianId()));
        }
        applyEditableFields(m, req);
        auditService.log("ACTUALIZAR", "MANTENIMIENTO", id, "Mantenimiento #" + id + " actualizado");
        return toResponse(m);
    }

    /** PROGRAMADO -> EN_PROCESO. El drone pasa automáticamente a EN_MANTENIMIENTO. */
    @Transactional
    public MaintenanceResponse start(Long id) {
        Maintenance m = get(id);
        if (m.getStatus() != MaintenanceStatus.PROGRAMADO) {
            throw new InvalidStateTransitionException("Solo se puede iniciar un mantenimiento PROGRAMADO (estado actual: " + m.getStatus() + ")");
        }
        Drone drone = m.getDrone();
        if (drone.getStatus() == DroneStatus.EN_MISION) {
            throw new BusinessRuleException("El drone " + drone.getCode() + " está en una misión; espera a que finalice");
        }
        m.setStatus(MaintenanceStatus.EN_PROCESO);
        m.setStartedAt(LocalDateTime.now());
        drone.setStatus(DroneStatus.EN_MANTENIMIENTO);
        auditService.log("INICIAR", "MANTENIMIENTO", id, "Mantenimiento de " + drone.getCode() + " iniciado; drone EN_MANTENIMIENTO");
        return toResponse(m);
    }

    /** EN_PROCESO -> COMPLETADO. El drone vuelve a DISPONIBLE con la batería cargada. */
    @Transactional
    public MaintenanceResponse complete(Long id, MaintenanceCompleteRequest req) {
        Maintenance m = get(id);
        if (m.getStatus() != MaintenanceStatus.EN_PROCESO) {
            throw new InvalidStateTransitionException("Solo se puede completar un mantenimiento EN_PROCESO (estado actual: " + m.getStatus() + ")");
        }
        if (req != null) {
            if (req.finalCost() != null) m.setCost(req.finalCost());
            if (req.nextDate() != null) m.setNextDate(req.nextDate());
            if (req.observations() != null && !req.observations().isBlank()) m.setObservations(req.observations().trim());
        }
        if (m.getNextDate() != null && !m.getNextDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("La próxima fecha de mantenimiento debe ser futura");
        }
        m.setStatus(MaintenanceStatus.COMPLETADO);
        m.setCompletedAt(LocalDateTime.now());

        Drone drone = m.getDrone();
        drone.setLastMaintenanceOn(LocalDate.now());
        if (m.getNextDate() != null) {
            drone.setNextMaintenanceOn(m.getNextDate());
        }
        drone.setBatteryLevel(100);
        releaseDroneIfFree(m);
        auditService.log("COMPLETAR", "MANTENIMIENTO", id, "Mantenimiento de " + drone.getCode() + " completado; drone " + drone.getStatus());
        return toResponse(m);
    }

    @Transactional
    public MaintenanceResponse cancel(Long id) {
        Maintenance m = get(id);
        if (m.getStatus() == MaintenanceStatus.COMPLETADO || m.getStatus() == MaintenanceStatus.CANCELADO) {
            throw new InvalidStateTransitionException("El mantenimiento ya está " + m.getStatus());
        }
        boolean wasInProgress = m.getStatus() == MaintenanceStatus.EN_PROCESO;
        m.setStatus(MaintenanceStatus.CANCELADO);
        if (wasInProgress) {
            releaseDroneIfFree(m);
        }
        auditService.log("CANCELAR", "MANTENIMIENTO", id, "Mantenimiento #" + id + " cancelado");
        return toResponse(m);
    }

    /** El drone vuelve a DISPONIBLE solo si no tiene otro mantenimiento en proceso. */
    private void releaseDroneIfFree(Maintenance m) {
        Drone drone = m.getDrone();
        boolean otherInProgress = maintenanceRepository.existsByDroneIdAndStatusAndIdNot(drone.getId(), MaintenanceStatus.EN_PROCESO, m.getId());
        if (!otherInProgress && drone.getStatus() == DroneStatus.EN_MANTENIMIENTO) {
            drone.setStatus(DroneStatus.DISPONIBLE);
        }
    }

    private void applyEditableFields(Maintenance m, MaintenanceRequest req) {
        if (req.nextDate() != null && !req.nextDate().isAfter(req.date())) {
            throw new BusinessRuleException("La próxima fecha debe ser posterior a la fecha del mantenimiento");
        }
        m.setType(req.type());
        m.setDate(req.date());
        m.setDescription(req.description().trim());
        m.setCost(req.cost());
        m.setNextDate(req.nextDate());
        m.setObservations(req.observations());
    }

    /** Técnico responsable: el propio técnico autenticado o el indicado por el administrador. */
    private User resolveTechnician(Long technicianId) {
        User current = currentUserService.getEntity();
        if (current.hasRole(RoleName.TECNICO) && (technicianId == null || technicianId.equals(current.getId()))) {
            return current;
        }
        if (technicianId == null) {
            return current;
        }
        User tech = userRepository.findById(technicianId).orElseThrow(() -> new ResourceNotFoundException("Usuario", technicianId));
        if (!tech.hasRole(RoleName.TECNICO) && !tech.hasRole(RoleName.ADMINISTRADOR)) {
            throw new BusinessRuleException("El responsable del mantenimiento debe ser un técnico");
        }
        return tech;
    }

    private Maintenance get(Long id) {
        return maintenanceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Mantenimiento", id));
    }

    public static MaintenanceResponse toResponse(Maintenance m) {
        return new MaintenanceResponse(m.getId(), m.getDrone().getId(), m.getDrone().getCode(), m.getDrone().getName(),
                m.getDrone().getStatus().name(), m.getTechnician().getId(), m.getTechnician().getFullName(),
                m.getType().name(), m.getDate(), m.getDescription(), m.getCost(), m.getStatus().name(), m.getNextDate(),
                m.getObservations(), m.getStartedAt(), m.getCompletedAt());
    }
}
