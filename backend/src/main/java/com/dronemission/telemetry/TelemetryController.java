package com.dronemission.telemetry;

import com.dronemission.telemetry.dto.TelemetryFeed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/missions/{missionId}/telemetry")
@RequiredArgsConstructor
@Tag(name = "Telemetría simulada")
public class TelemetryController {

    private final TelemetryService telemetryService;

    @GetMapping
    @PreAuthorize("hasAuthority('MISIONES_VER')")
    @Operation(summary = "Telemetría (SIMULADA) de la misión; el frontend la consulta cada pocos segundos")
    public TelemetryFeed feed(@PathVariable Long missionId) {
        return telemetryService.feed(missionId);
    }
}
