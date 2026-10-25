package com.dronemission.pilots;

import com.dronemission.pilots.dto.PilotMissionEntry;
import com.dronemission.pilots.dto.PilotRequest;
import com.dronemission.pilots.dto.PilotResponse;
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
@RequestMapping("/api/pilots")
@RequiredArgsConstructor
@Tag(name = "Pilotos")
public class PilotController {

    private final PilotService pilotService;

    @GetMapping
    @PreAuthorize("hasAuthority('PILOTOS_VER')")
    public List<PilotResponse> findAll(@RequestParam(required = false) String q,
                                       @RequestParam(required = false) PilotStatus status) {
        return pilotService.findAll(q, status);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PILOTO')")
    @Operation(summary = "Perfil del piloto autenticado")
    public PilotResponse me() {
        return pilotService.findMine();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PILOTOS_VER')")
    public PilotResponse findById(@PathVariable Long id) {
        return pilotService.findById(id);
    }

    @GetMapping("/{id}/missions")
    @PreAuthorize("hasAuthority('PILOTOS_VER')")
    @Operation(summary = "Historial de misiones del piloto")
    public List<PilotMissionEntry> missions(@PathVariable Long id) {
        return pilotService.missionsOf(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PILOTOS_GESTIONAR')")
    public PilotResponse create(@Valid @RequestBody PilotRequest request) {
        return pilotService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PILOTOS_GESTIONAR')")
    public PilotResponse update(@PathVariable Long id, @Valid @RequestBody PilotRequest request) {
        return pilotService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PILOTOS_GESTIONAR')")
    @Operation(summary = "Desactivar piloto")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        pilotService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
