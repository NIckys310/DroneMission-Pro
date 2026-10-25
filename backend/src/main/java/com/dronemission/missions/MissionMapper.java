package com.dronemission.missions;

import com.dronemission.missions.dto.MissionResponse;

import java.util.List;

/** Convierte una Mission en el DTO que consume el frontend. */
public final class MissionMapper {

    private MissionMapper() {
    }

    public static MissionResponse toResponse(Mission m, List<String> availableActions) {
        var assignment = m.getActiveAssignment();
        MissionResponse.DroneRef drone = assignment.map(a -> new MissionResponse.DroneRef(a.getDrone().getId(),
                a.getDrone().getCode(), a.getDrone().getName(), a.getDrone().getType().name(),
                a.getDrone().getStatus().name(), a.getDrone().getBatteryLevel())).orElse(null);
        MissionResponse.PilotRef pilot = assignment.map(a -> new MissionResponse.PilotRef(a.getPilot().getId(),
                a.getPilot().getFullName(), a.getPilot().getStatus().name(), a.getPilot().getLicenseExpiresOn())).orElse(null);

        return new MissionResponse(m.getId(), m.getCode(), m.getName(), m.getDescription(), m.getType().name(),
                new MissionResponse.ClientRef(m.getClient().getId(), m.getClient().getName()),
                m.getLocation(), m.getLatitude(), m.getLongitude(), m.getTargetAltitudeM(), m.getScheduledDate(),
                m.getStartTime(), m.getEstimatedEndTime(), m.getPriority().name(), m.getStatus().name(),
                m.getObjective(), m.getObservations(), drone, pilot, m.getCreatedBy().getFullName(),
                m.getActualStart(), m.getActualEnd(), m.getCreatedAt(), availableActions);
    }
}
