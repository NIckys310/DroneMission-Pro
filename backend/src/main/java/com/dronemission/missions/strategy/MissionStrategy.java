package com.dronemission.missions.strategy;

import com.dronemission.drones.DroneType;
import com.dronemission.missions.MissionType;

import java.util.List;
import java.util.Set;

/**
 * PATRÓN STRATEGY.
 * Cada tipo de misión vuela y consume batería de forma distinta. En lugar de llenar
 * el código de "if (tipo == AGRICULTURA) ... else if ...", cada comportamiento vive
 * en su propia clase que implementa esta interfaz:
 *  - qué tipos de misión atiende,
 *  - batería mínima para asignar el drone,
 *  - qué tipos de drone son compatibles,
 *  - velocidad de crucero y consumo de batería (usados por el simulador),
 *  - cómo generar el plan de vuelo (patrón de recorrido).
 */
public interface MissionStrategy {

    /** Nombre legible de la estrategia (se muestra en el frontend). */
    String name();

    Set<MissionType> supportedTypes();

    int minimumBatteryPercent();

    Set<DroneType> compatibleDroneTypes();

    double cruiseSpeedKmh();

    /** Porcentaje de batería que se consume por minuto de vuelo. */
    double batteryDrainPerMinute();

    /** Genera el plan de vuelo alrededor del punto central de la misión. */
    List<PlannedWaypoint> generateFlightPlan(GeoPoint center, int altitudeM);
}
