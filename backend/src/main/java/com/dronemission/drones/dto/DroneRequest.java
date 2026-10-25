package com.dronemission.drones.dto;

import com.dronemission.drones.DroneType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos para crear o editar un drone.
 * Capacidad de batería, velocidad y autonomía son opcionales: si no se envían,
 * DroneFactory aplica los valores típicos del tipo de drone.
 */
public record DroneRequest(
        @Size(max = 20) String code,
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String name,
        @NotBlank(message = "El fabricante es obligatorio") @Size(max = 80) String manufacturer,
        @NotBlank(message = "El modelo es obligatorio") @Size(max = 80) String model,
        @NotBlank(message = "El número de serie es obligatorio") @Size(max = 60) String serialNumber,
        @NotNull(message = "El tipo de drone es obligatorio") DroneType type,
        @Positive(message = "La capacidad de batería debe ser mayor que cero") Integer batteryCapacityMah,
        @Min(value = 0, message = "La batería no puede ser menor a 0 %")
        @Max(value = 100, message = "La batería no puede ser mayor a 100 %") Integer batteryLevel,
        @PositiveOrZero(message = "Las horas de vuelo no pueden ser negativas") BigDecimal flightHours,
        @Positive(message = "La velocidad máxima debe ser mayor que cero") Integer maxSpeedKmh,
        @Positive(message = "La autonomía debe ser mayor que cero") Integer autonomyMinutes,
        @NotNull(message = "La fecha de adquisición es obligatoria")
        @PastOrPresent(message = "La fecha de adquisición no puede ser futura") LocalDate acquiredOn,
        LocalDate lastMaintenanceOn,
        LocalDate nextMaintenanceOn,
        @Size(max = 1000) String description
) {
}
