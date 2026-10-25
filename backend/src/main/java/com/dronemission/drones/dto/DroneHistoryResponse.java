package com.dronemission.drones.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Historial de un drone: misiones en las que participó y mantenimientos. */
public record DroneHistoryResponse(List<MissionEntry> missions, List<MaintenanceEntry> maintenance) {

    public record MissionEntry(Long missionId, String code, String name, String status, LocalDate scheduledDate,
                               String pilotName, LocalDateTime assignedAt) {
    }

    public record MaintenanceEntry(Long id, String type, String status, LocalDate date, String description,
                                   java.math.BigDecimal cost, String technicianName) {
    }
}
