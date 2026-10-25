package com.dronemission.maintenance.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos opcionales al finalizar un mantenimiento (costo final, próxima fecha, observaciones). */
public record MaintenanceCompleteRequest(
        @PositiveOrZero(message = "El costo no puede ser negativo") BigDecimal finalCost,
        LocalDate nextDate,
        @Size(max = 1000) String observations
) {
}
