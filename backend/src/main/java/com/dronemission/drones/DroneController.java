package com.dronemission.drones;

import com.dronemission.drones.dto.DroneHistoryResponse;
import com.dronemission.drones.dto.DroneRequest;
import com.dronemission.drones.dto.DroneResponse;
import com.dronemission.drones.dto.DroneStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drones")
@RequiredArgsConstructor
@Tag(name = "Drones")
public class DroneController {

    private final DroneService droneService;

    @GetMapping
    @PreAuthorize("hasAuthority('DRONES_VER')")
    @Operation(summary = "Listar drones (búsqueda por texto y filtros por estado/tipo)")
    public List<DroneResponse> findAll(@RequestParam(required = false) String q,
                                       @RequestParam(required = false) DroneStatus status,
                                       @RequestParam(required = false) DroneType type) {
        return droneService.findAll(q, status, type);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DRONES_VER')")
    public DroneResponse findById(@PathVariable Long id) {
        return droneService.findById(id);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('DRONES_VER')")
    @Operation(summary = "Historial de misiones y mantenimientos del drone")
    public DroneHistoryResponse history(@PathVariable Long id) {
        return droneService.history(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('DRONES_GESTIONAR')")
    public DroneResponse create(@Valid @RequestBody DroneRequest request) {
        return droneService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DRONES_GESTIONAR')")
    public DroneResponse update(@PathVariable Long id, @Valid @RequestBody DroneRequest request) {
        return droneService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('DRONES_ESTADO_TECNICO')")
    @Operation(summary = "Cambiar el estado técnico (DISPONIBLE / FUERA_DE_SERVICIO)")
    public DroneResponse changeStatus(@PathVariable Long id, @Valid @RequestBody DroneStatusRequest request) {
        return droneService.changeTechnicalStatus(id, request);
    }

    @PostMapping("/{id}/charge")
    @PreAuthorize("hasAuthority('DRONES_ESTADO_TECNICO')")
    @Operation(summary = "Cargar la batería del drone al 100 %")
    public DroneResponse charge(@PathVariable Long id) {
        return droneService.chargeBattery(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DRONES_GESTIONAR')")
    @Operation(summary = "Desactivar drone (baja lógica)")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        droneService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
