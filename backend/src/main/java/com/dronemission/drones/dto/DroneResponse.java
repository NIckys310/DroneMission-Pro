package com.dronemission.drones.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DroneResponse(
        Long id,
        String code,
        String name,
        String manufacturer,
        String model,
        String serialNumber,
        String type,
        String status,
        Integer batteryCapacityMah,
        Integer batteryLevel,
        BigDecimal flightHours,
        Integer maxSpeedKmh,
        Integer autonomyMinutes,
        LocalDate acquiredOn,
        LocalDate lastMaintenanceOn,
        LocalDate nextMaintenanceOn,
        boolean maintenanceDue,
        String description,
        boolean active
) {
}
