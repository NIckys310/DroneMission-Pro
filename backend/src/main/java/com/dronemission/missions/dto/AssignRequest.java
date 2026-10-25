package com.dronemission.missions.dto;

import jakarta.validation.constraints.NotNull;

public record AssignRequest(
        @NotNull(message = "Debes seleccionar un drone") Long droneId,
        @NotNull(message = "Debes seleccionar un piloto") Long pilotId
) {
}
