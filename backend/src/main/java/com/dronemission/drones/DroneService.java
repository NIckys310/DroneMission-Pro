package com.dronemission.drones;

import com.dronemission.audit.AuditService;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.common.exception.DuplicateResourceException;
import com.dronemission.common.exception.ResourceNotFoundException;
import com.dronemission.drones.dto.DroneHistoryResponse;
import com.dronemission.drones.dto.DroneRequest;
import com.dronemission.drones.dto.DroneResponse;
import com.dronemission.drones.dto.DroneStatusRequest;
import com.dronemission.maintenance.MaintenanceRepository;
import com.dronemission.maintenance.MaintenanceStatus;
import com.dronemission.missions.MissionAssignmentRepository;
import com.dronemission.missions.MissionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DroneService {

    private final DroneRepository droneRepository;
    private final DroneFactory droneFactory;
    private final MissionAssignmentRepository assignmentRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final AuditService auditService;

    /** Lista drones con búsqueda por texto y filtros opcionales por estado y tipo. */
    @Transactional(readOnly = true)
    public List<DroneResponse> findAll(String query, DroneStatus status, DroneType type) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return droneRepository.findAllByOrderByCodeAsc().stream()
                .filter(d -> status == null || d.getStatus() == status)
                .filter(d -> type == null || d.getType() == type)
                .filter(d -> q.isEmpty() || d.getCode().toLowerCase().contains(q) || d.getName().toLowerCase().contains(q)
                        || d.getModel().toLowerCase().contains(q) || d.getManufacturer().toLowerCase().contains(q))
                .map(DroneMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DroneResponse findById(Long id) {
        return DroneMapper.toResponse(get(id));
    }

    @Transactional
    public DroneResponse create(DroneRequest req) {
        long next = droneRepository.count() + 1;
        Drone drone = droneFactory.createDrone(req, next);
        while (droneRepository.existsByCode(drone.getCode()) && (req.code() == null || req.code().isBlank())) {
            drone.setCode(String.format("DRN-%03d", ++next));
        }
        if (droneRepository.existsByCode(drone.getCode())) {
            throw new DuplicateResourceException("Ya existe un drone con el código " + drone.getCode());
        }
        if (droneRepository.existsBySerialNumber(drone.getSerialNumber())) {
            throw new DuplicateResourceException("Ya existe un drone con el número de serie " + drone.getSerialNumber());
        }
        droneRepository.save(drone);
        auditService.log("CREAR", "DRONE", drone.getId(), "Drone " + drone.getCode() + " (" + drone.getName() + ") creado");
        return DroneMapper.toResponse(drone);
    }

    @Transactional
    public DroneResponse update(Long id, DroneRequest req) {
        Drone drone = get(id);
        String serial = req.serialNumber().trim().toUpperCase();
        if (droneRepository.existsBySerialNumberAndIdNot(serial, id)) {
            throw new DuplicateResourceException("Ya existe un drone con el número de serie " + serial);
        }
        if (req.code() != null && !req.code().isBlank()) {
            String code = req.code().trim().toUpperCase();
            if (droneRepository.existsByCodeAndIdNot(code, id)) {
                throw new DuplicateResourceException("Ya existe un drone con el código " + code);
            }
            drone.setCode(code);
        }
        DroneMapper.apply(req, drone);
        auditService.log("ACTUALIZAR", "DRONE", id, "Drone " + drone.getCode() + " actualizado");
        return DroneMapper.toResponse(drone);
    }

    /**
     * Cambio manual del estado técnico (técnico o administrador).
     * Solo se permite DISPONIBLE o FUERA_DE_SERVICIO: EN_MISION y EN_MANTENIMIENTO
     * los asigna el sistema automáticamente.
     */
    @Transactional
    public DroneResponse changeTechnicalStatus(Long id, DroneStatusRequest req) {
        Drone drone = get(id);
        if (req.status() == DroneStatus.EN_MISION || req.status() == DroneStatus.EN_MANTENIMIENTO) {
            throw new BusinessRuleException("El estado " + req.status() + " lo asigna el sistema automáticamente");
        }
        if (drone.getStatus() == DroneStatus.EN_MISION) {
            throw new BusinessRuleException("El drone está en una misión; no se puede cambiar su estado");
        }
        if (req.status() == DroneStatus.DISPONIBLE
                && maintenanceRepository.existsByDroneIdAndStatus(id, MaintenanceStatus.EN_PROCESO)) {
            throw new BusinessRuleException("El drone tiene un mantenimiento en proceso; finalízalo para marcarlo disponible");
        }
        DroneStatus before = drone.getStatus();
        drone.setStatus(req.status());
        auditService.log("CAMBIAR_ESTADO", "DRONE", id, "Drone " + drone.getCode() + ": " + before + " -> " + req.status()
                + (req.reason() != null && !req.reason().isBlank() ? " (" + req.reason() + ")" : ""));
        return DroneMapper.toResponse(drone);
    }

    /** Simula la carga completa de la batería (acción del técnico entre misiones). */
    @Transactional
    public DroneResponse chargeBattery(Long id) {
        Drone drone = get(id);
        if (drone.getStatus() == DroneStatus.EN_MISION) {
            throw new BusinessRuleException("No se puede cargar la batería de un drone en misión");
        }
        int before = drone.getBatteryLevel();
        drone.setBatteryLevel(100);
        auditService.log("CARGAR_BATERIA", "DRONE", id, "Batería de " + drone.getCode() + ": " + before + " % -> 100 %");
        return DroneMapper.toResponse(drone);
    }

    /** Baja lógica: el drone se desactiva para conservar su historial. */
    @Transactional
    public void deactivate(Long id) {
        Drone drone = get(id);
        if (assignmentRepository.droneHasActiveMissions(id, MissionStatus.occupyingResources())) {
            throw new BusinessRuleException("El drone tiene misiones programadas o en curso; reasígnalas antes de desactivarlo");
        }
        drone.setActive(false);
        drone.setStatus(DroneStatus.FUERA_DE_SERVICIO);
        auditService.log("DESACTIVAR", "DRONE", id, "Drone " + drone.getCode() + " desactivado");
    }

    @Transactional(readOnly = true)
    public DroneHistoryResponse history(Long id) {
        get(id);
        var missions = assignmentRepository.findByDroneIdOrderByAssignedAtDesc(id).stream()
                .map(a -> new DroneHistoryResponse.MissionEntry(a.getMission().getId(), a.getMission().getCode(),
                        a.getMission().getName(), a.getMission().getStatus().name(), a.getMission().getScheduledDate(),
                        a.getPilot().getFullName(), a.getAssignedAt()))
                .toList();
        var maintenance = maintenanceRepository.findByDroneIdOrderByDateDesc(id).stream()
                .map(m -> new DroneHistoryResponse.MaintenanceEntry(m.getId(), m.getType().name(), m.getStatus().name(),
                        m.getDate(), m.getDescription(), m.getCost(), m.getTechnician().getFullName()))
                .toList();
        return new DroneHistoryResponse(missions, maintenance);
    }

    public Drone get(Long id) {
        return droneRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Drone", id));
    }
}
