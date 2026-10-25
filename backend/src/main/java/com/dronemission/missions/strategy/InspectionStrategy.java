package com.dronemission.missions.strategy;

import com.dronemission.drones.DroneType;
import com.dronemission.missions.MissionType;
import com.dronemission.missions.WaypointAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Inspección y fotografía: órbitas alrededor del objetivo tomando fotos. */
@Component
public class InspectionStrategy extends AbstractMissionStrategy {

    private static final int POINTS_PER_ORBIT = 8;
    private static final double RADIUS_M = 60;

    @Override public String name() { return "Órbita de inspección"; }
    @Override public Set<MissionType> supportedTypes() { return Set.of(MissionType.INSPECCION, MissionType.FOTOGRAFIA); }
    @Override public int minimumBatteryPercent() { return 50; }
    @Override public Set<DroneType> compatibleDroneTypes() { return Set.of(DroneType.INSPECCION, DroneType.FOTOGRAFIA, DroneType.TOPOGRAFIA); }
    @Override public double cruiseSpeedKmh() { return 20; }
    @Override public double batteryDrainPerMinute() { return 1.8; }

    @Override
    protected List<PlannedWaypoint> generatePattern(GeoPoint center, int altitudeM) {
        List<PlannedWaypoint> points = new ArrayList<>();
        // Dos órbitas: una a la altitud objetivo y otra 15 m más abajo para ver detalles
        int[] altitudes = {altitudeM, Math.max(10, altitudeM - 15)};
        for (int altitude : altitudes) {
            for (int i = 0; i < POINTS_PER_ORBIT; i++) {
                double angle = 2 * Math.PI * i / POINTS_PER_ORBIT;
                GeoPoint p = center.offset(RADIUS_M * Math.cos(angle), RADIUS_M * Math.sin(angle));
                points.add(new PlannedWaypoint(p, altitude, WaypointAction.FOTO));
            }
        }
        return points;
    }
}
