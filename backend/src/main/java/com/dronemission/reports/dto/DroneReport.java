package com.dronemission.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DroneReport(long total, long available, long inMission, long inMaintenance, long outOfService,
                          BigDecimal totalFlightHours, List<Row> drones) {

    public record Row(String code, String name, String model, String type, String status, int batteryLevel,
                      BigDecimal flightHours, LocalDate nextMaintenanceOn) {
    }
}
