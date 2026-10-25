package com.dronemission.dashboard.dto;

import com.dronemission.incidents.dto.IncidentResponse;
import com.dronemission.maintenance.dto.MaintenanceResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/** Datos del dashboard central (módulo 5.2). */
public record DashboardResponse(
        DroneStats drones,
        PilotStats pilots,
        Map<String, Long> missionsByStatus,
        List<CountItem> missionsByType,
        List<HoursItem> flightHoursByDrone,
        List<DayItem> missionsPerDay,
        List<MissionItem> activeMissions,
        List<MissionItem> upcomingMissions,
        List<IncidentResponse> recentIncidents,
        List<MaintenanceResponse> pendingMaintenance,
        List<Alert> alerts,
        long openIncidents
) {
    public record DroneStats(long total, long available, long inMission, long inMaintenance, long outOfService,
                             BigDecimal totalFlightHours) {
    }

    public record PilotStats(long total, long available, long inMission, long expiredLicenses) {
    }

    public record CountItem(String label, long count) {
    }

    public record HoursItem(String drone, BigDecimal hours) {
    }

    public record DayItem(LocalDate date, long scheduled, long finished) {
    }

    public record MissionItem(Long id, String code, String name, String status, String type, LocalDate date,
                              LocalTime startTime, String clientName, String droneCode, String pilotName) {
    }

    /** level: info | warning | error */
    public record Alert(String level, String message, String link) {
    }
}
