package com.dronemission.maintenance.dto;

import com.dronemission.maintenance.MaintenanceType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaintenanceRequest(
        @NotNull(message = "El drone es obligatorio") Long droneId,
        Long technicianId,
        @NotNull(message = "El tipo de mantenimiento es obligatorio") MaintenanceType type,
        @NotNull(message = "La fecha es obligatoria") LocalDate date,
        @NotBlank(message = "La descripción es obligatoria") @Size(max = 1000) String description,
        @NotNull(message = "El costo es obligatorio") @PositiveOrZero(message = "El costo no puede ser negativo") BigDecimal cost,
        LocalDate nextDate,
        @Size(max = 1000) String observations,
        Boolean startNow
) {
}
