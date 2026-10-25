package com.dronemission.missions.dto;

import java.util.List;

/** Plan de vuelo programado que se "carga" en el drone virtual antes del despegue. */
public record FlightPlanResponse(
        String strategy,
        int minimumBatteryPercent,
        List<String> compatibleDroneTypes,
        double cruiseSpeedKmh,
        double estimatedDistanceM,
        double estimatedDurationMin,
        double estimatedBatteryUsePercent,
        List<WaypointResponse> waypoints
) {
}
