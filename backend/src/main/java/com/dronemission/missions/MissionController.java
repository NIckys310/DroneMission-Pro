package com.dronemission.missions;

import com.dronemission.audit.dto.AuditResponse;
import com.dronemission.missions.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de misiones. Los controllers no contienen reglas de negocio:
 * solo reciben la petición, delegan en el servicio y devuelven el DTO.
 */
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Tag(name = "Misiones")
public class MissionController {

    private final MissionService missionService;
    private final MissionLifecycleService lifecycleService;
    private final AssignmentService assignmentService;

    // ----------------------------------------------------------- consultas y CRUD

    @GetMapping
    @PreAuthorize("hasAuthority('MISIONES_VER')")
    @Operation(summary = "Listar misiones (el piloto y el cliente solo ven las suyas)")
    public List<MissionResponse> findAll(@RequestParam(required = false) MissionStatus status,
                                         @RequestParam(required = false) MissionType type,
                                         @RequestParam(required = false) Long clientId,
                                         @RequestParam(required = false) String q) {
        return missionService.findAll(status, type, clientId, q);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MISIONES_VER')")
    public MissionResponse findById(@PathVariable Long id) {
        return missionService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MISIONES_GESTIONAR')")
    @Operation(summary = "Crear misión (queda en BORRADOR con su plan de vuelo generado)")
    public MissionResponse create(@Valid @RequestBody MissionRequest request) {
        return missionService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MISIONES_GESTIONAR')")
    public MissionResponse update(@PathVariable Long id, @Valid @RequestBody MissionRequest request) {
        return missionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MISIONES_GESTIONAR')")
    @Operation(summary = "Eliminar misión (solo en BORRADOR)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        missionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/flight-plan")
    @PreAuthorize("hasAuthority('MISIONES_VER')")
    @Operation(summary = "Plan de vuelo programado (puntos de ruta y estimaciones)")
    public FlightPlanResponse flightPlan(@PathVariable Long id) {
        return missionService.flightPlan(id);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('MISIONES_VER')")
    @Operation(summary = "Historial (auditoría) de la misión")
    public List<AuditResponse> history(@PathVariable Long id) {
        return missionService.history(id);
    }

    @PostMapping("/{id}/observations")
    @PreAuthorize("hasAnyAuthority('MISIONES_EJECUTAR', 'MISIONES_GESTIONAR')")
    public MissionResponse addObservation(@PathVariable Long id, @Valid @RequestBody ObservationRequest request) {
        return missionService.addObservation(id, request.text());
    }

    // ----------------------------------------------------------- planificación (operador)

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('MISIONES_GESTIONAR')")
    @Operation(summary = "BORRADOR -> PENDIENTE")
    public MissionResponse submit(@PathVariable Long id) {
        return lifecycleService.submit(id);
    }

    @GetMapping("/{id}/assignment-options")
    @PreAuthorize("hasAuthority('MISIONES_GESTIONAR')")
    @Operation(summary = "Drones y pilotos elegibles para la misión (con el motivo de los no elegibles)")
    public AssignmentOptionsResponse assignmentOptions(@PathVariable Long id) {
        return assignmentService.options(id);
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('MISIONES_GESTIONAR')")
    @Operation(summary = "Asignar drone y piloto (PENDIENTE -> PROGRAMADA)")
    public MissionResponse assign(@PathVariable Long id, @Valid @RequestBody AssignRequest request) {
        return assignmentService.assign(id, request);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('MISIONES_GESTIONAR')")
    @Operation(summary = "Cancelar misión (antes del vuelo)")
    public MissionResponse cancel(@PathVariable Long id, @Valid @RequestBody CancelRequest request) {
        return lifecycleService.cancel(id, request.reason());
    }

    // ----------------------------------------------------------- ejecución (piloto)

    @PostMapping("/{id}/prepare")
    @PreAuthorize("hasAuthority('MISIONES_EJECUTAR')")
    @Operation(summary = "Chequeo prevuelo: PROGRAMADA -> EN_PREPARACION")
    public MissionResponse prepare(@PathVariable Long id) {
        return lifecycleService.prepare(id);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAuthority('MISIONES_EJECUTAR')")
    @Operation(summary = "Despegar: EN_PREPARACION -> EN_VUELO")
    public MissionResponse start(@PathVariable Long id) {
        return lifecycleService.start(id);
    }

    @PostMapping("/{id}/pause")
    @PreAuthorize("hasAuthority('MISIONES_EJECUTAR')")
    @Operation(summary = "EN_VUELO -> PAUSADA")
    public MissionResponse pause(@PathVariable Long id, @RequestBody(required = false) ObservationRequest request) {
        return lifecycleService.pause(id, request != null ? request.text() : null);
    }

    @PostMapping("/{id}/resume")
    @PreAuthorize("hasAuthority('MISIONES_EJECUTAR')")
    @Operation(summary = "PAUSADA -> EN_VUELO")
    public MissionResponse resume(@PathVariable Long id) {
        return lifecycleService.resume(id);
    }

    @PostMapping("/{id}/finish")
    @PreAuthorize("hasAuthority('MISIONES_EJECUTAR')")
    @Operation(summary = "Finalizar: EN_VUELO | PAUSADA -> FINALIZADA (genera el reporte)")
    public MissionResponse finish(@PathVariable Long id, @RequestBody(required = false) ObservationRequest request) {
        return lifecycleService.finish(id, request != null ? request.text() : null);
    }
}
