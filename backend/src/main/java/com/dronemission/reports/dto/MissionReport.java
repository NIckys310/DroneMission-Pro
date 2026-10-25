package com.dronemission.reports.dto;

import com.dronemission.evidence.dto.EvidenceResponse;
import com.dronemission.incidents.dto.IncidentResponse;
import com.dronemission.telemetry.dto.TelemetrySummary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Reporte completo de una misión (módulo 16). */
public record MissionReport(
        Long missionId,
        String code,
        String name,
        String type,
        String status,
        String priority,
        String objective,
        String location,
        double latitude,
        double longitude,
        LocalDate scheduledDate,
        LocalTime startTime,
        LocalTime estimatedEndTime,
        LocalDateTime actualStart,
        LocalDateTime actualEnd,
        Long durationMinutes,
        String observations,
        ClientInfo client,
        DroneInfo drone,
        PilotInfo pilot,
        String flightStrategy,
        int waypoints,
        TelemetrySummary telemetry,
        List<IncidentResponse> incidents,
        List<EvidenceResponse> evidences,
        String telemetryNotice
) {
    public record ClientInfo(String name, String type, String document, String email, String phone, String city) {
    }

    public record DroneInfo(String code, String name, String manufacturer, String model, String type) {
    }

    public record PilotInfo(String fullName, String licenseNumber, LocalDate licenseExpiresOn) {
    }
}
