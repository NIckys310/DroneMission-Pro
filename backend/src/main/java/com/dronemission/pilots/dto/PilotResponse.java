package com.dronemission.pilots.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PilotResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String document,
        String email,
        String phone,
        String licenseNumber,
        LocalDate licenseExpiresOn,
        boolean licenseValid,
        BigDecimal flightHours,
        String status,
        boolean active,
        String userEmail,
        List<CertificationDto> certifications,
        long missionCount
) {
}
