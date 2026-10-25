package com.dronemission.drones.dto;

import com.dronemission.drones.DroneStatus;
import jakarta.validation.constraints.NotNull;

public record DroneStatusRequest(
        @NotNull(message = "El estado es obligatorio") DroneStatus status,
        String reason
) {
}
