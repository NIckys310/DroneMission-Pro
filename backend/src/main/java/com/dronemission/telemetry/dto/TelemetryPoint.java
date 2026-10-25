package com.dronemission.telemetry.dto;

import java.time.LocalDateTime;

public record TelemetryPoint(Long id, LocalDateTime recordedAt, double latitude, double longitude, double altitudeM,
                             double speedKmh, int battery, double distanceM, double temperatureC, int flightTimeS,
                             int currentWaypoint, boolean simulated) {
}
