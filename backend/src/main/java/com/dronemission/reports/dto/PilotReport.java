package com.dronemission.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PilotReport(long total, long available, long expiredLicenses, BigDecimal totalFlightHours, List<Row> pilots) {

    public record Row(String fullName, String licenseNumber, LocalDate licenseExpiresOn, boolean licenseValid,
                      String status, BigDecimal flightHours, long missions, long finishedMissions) {
    }
}
