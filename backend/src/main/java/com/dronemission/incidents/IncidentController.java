package com.dronemission.incidents;

import com.dronemission.incidents.dto.IncidentRequest;
import com.dronemission.incidents.dto.IncidentResponse;
import com.dronemission.incidents.dto.IncidentStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Incidentes")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping("/incidents")
    @PreAuthorize("hasAuthority('INCIDENTES_VER')")
    @Operation(summary = "Listar incidentes con filtros")
    public List<IncidentResponse> findAll(@RequestParam(required = false) IncidentSeverity severity,
                                          @RequestParam(required = false) IncidentStatus status,
                                          @RequestParam(required = false) IncidentType type) {
        return incidentService.findAll(severity, status, type);
    }

    @GetMapping("/missions/{missionId}/incidents")
    @PreAuthorize("hasAuthority('MISIONES_VER')")
    public List<IncidentResponse> byMission(@PathVariable Long missionId) {
        return incidentService.findByMission(missionId);
    }

    @PostMapping("/missions/{missionId}/incidents")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('INCIDENTES_REGISTRAR')")
    @Operation(summary = "Registrar un incidente en la misión")
    public IncidentResponse create(@PathVariable Long missionId, @Valid @RequestBody IncidentRequest request) {
        return incidentService.create(missionId, request);
    }

    @PatchMapping("/incidents/{id}/status")
    @PreAuthorize("hasAuthority('INCIDENTES_GESTIONAR')")
    @Operation(summary = "Cambiar el estado de un incidente")
    public IncidentResponse updateStatus(@PathVariable Long id, @Valid @RequestBody IncidentStatusRequest request) {
        return incidentService.updateStatus(id, request);
    }
}
