package com.dronemission.maintenance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MaintenanceResponse(Long id, Long droneId, String droneCode, String droneName, String droneStatus,
                                  Long technicianId, String technicianName, String type, LocalDate date,
                                  String description, BigDecimal cost, String status, LocalDate nextDate,
                                  String observations, LocalDateTime startedAt, LocalDateTime completedAt) {
}
