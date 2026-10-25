package com.dronemission.incidents.dto;

import com.dronemission.incidents.IncidentSeverity;
import com.dronemission.incidents.IncidentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidentRequest(
        @NotNull(message = "El tipo de incidente es obligatorio") IncidentType type,
        @NotNull(message = "La severidad es obligatoria") IncidentSeverity severity,
        @NotBlank(message = "La descripción es obligatoria") @Size(max = 1000) String description
) {
}
