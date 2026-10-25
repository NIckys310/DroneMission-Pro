package com.dronemission.telemetry.dto;

import java.util.List;

/** Respuesta del endpoint de telemetría que consulta periódicamente el frontend. */
public record TelemetryFeed(
        boolean simulated,
        String notice,
        String missionStatus,
        boolean live,
        boolean routeCompleted,
        int totalWaypoints,
        TelemetrySummary summary,
        List<TelemetryPoint> points
) {
}
