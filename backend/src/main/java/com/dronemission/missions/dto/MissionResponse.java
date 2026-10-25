package com.dronemission.missions.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Misión tal como la ve el frontend.
 * availableActions lo calcula el backend según el estado y el rol del usuario,
 * así el frontend solo muestra los botones permitidos sin duplicar reglas.
 */
public record MissionResponse(
        Long id,
        String code,
        String name,
        String description,
        String type,
        ClientRef client,
        String location,
        Double latitude,
        Double longitude,
        Integer targetAltitudeM,
        LocalDate scheduledDate,
        LocalTime startTime,
        LocalTime estimatedEndTime,
        String priority,
        String status,
        String objective,
        String observations,
        DroneRef drone,
        PilotRef pilot,
        String createdBy,
        LocalDateTime actualStart,
        LocalDateTime actualEnd,
        LocalDateTime createdAt,
        List<String> availableActions
) {
    public record ClientRef(Long id, String name) {
    }

    public record DroneRef(Long id, String code, String name, String type, String status, Integer batteryLevel) {
    }

    public record PilotRef(Long id, String fullName, String status, LocalDate licenseExpiresOn) {
    }
}
