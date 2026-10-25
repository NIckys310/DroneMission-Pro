package com.dronemission.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MaintenanceReport(long total, long scheduled, long inProgress, long completed, long cancelled,
                                BigDecimal totalCost, List<CostByDrone> costByDrone, List<Row> history) {

    public record CostByDrone(String droneCode, String droneName, long count, BigDecimal cost) {
    }

    public record Row(LocalDate date, String droneCode, String type, String status, String technician,
                      BigDecimal cost, String description) {
    }
}
