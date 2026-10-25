package com.dronemission.missions.dto;

import com.dronemission.missions.MissionPriority;
import com.dronemission.missions.MissionType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record MissionRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 150) String name,
        @Size(max = 1000) String description,
        @NotNull(message = "El tipo de misión es obligatorio") MissionType type,
        @NotNull(message = "El cliente es obligatorio") Long clientId,
        @NotBlank(message = "La ubicación es obligatoria") @Size(max = 200) String location,
        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-90", message = "Latitud inválida") @DecimalMax(value = "90", message = "Latitud inválida") Double latitude,
        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-180", message = "Longitud inválida") @DecimalMax(value = "180", message = "Longitud inválida") Double longitude,
        @NotNull(message = "La altitud objetivo es obligatoria")
        @Min(value = 5, message = "La altitud mínima es 5 m") @Max(value = 120, message = "La altitud máxima legal es 120 m") Integer targetAltitudeM,
        @NotNull(message = "La fecha programada es obligatoria") LocalDate scheduledDate,
        @NotNull(message = "La hora de inicio es obligatoria") LocalTime startTime,
        @NotNull(message = "La hora estimada de finalización es obligatoria") LocalTime estimatedEndTime,
        @NotNull(message = "La prioridad es obligatoria") MissionPriority priority,
        @NotBlank(message = "El objetivo es obligatorio") @Size(max = 500) String objective,
        @Size(max = 1000) String observations
) {
}
