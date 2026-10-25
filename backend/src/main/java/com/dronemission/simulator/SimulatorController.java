package com.dronemission.simulator;

import com.dronemission.missions.MissionType;
import com.dronemission.missions.dto.FlightPlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulator")
@RequiredArgsConstructor
@Tag(name = "Simulador 3D")
public class SimulatorController {

    private final SimulatorService simulatorService;

    /** Disponible para cualquier usuario autenticado: es una herramienta de práctica. */
    @GetMapping("/plan")
    @Operation(summary = "Plan de vuelo de práctica para el simulador 3D (no crea ninguna misión)")
    public FlightPlanResponse plan(@RequestParam MissionType type,
                                   @RequestParam(defaultValue = "50") int altitudeM) {
        return simulatorService.practicePlan(type, altitudeM);
    }
}
