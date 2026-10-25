package com.dronemission.incidents.dto;

import com.dronemission.incidents.IncidentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidentStatusRequest(
        @NotNull(message = "El estado es obligatorio") IncidentStatus status,
        @Size(max = 1000) String resolution
) {
}
