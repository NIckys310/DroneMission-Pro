package com.dronemission.telemetry.dto;

/** Resumen de la telemetría de una misión (para el detalle y los reportes). */
public record TelemetrySummary(int readings, double maxAltitudeM, double avgSpeedKmh, double maxSpeedKmh,
                               double distanceM, int flightTimeS, int startBattery, int endBattery,
                               int batteryUsed, double maxTemperatureC) {

    public static TelemetrySummary empty() {
        return new TelemetrySummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}
