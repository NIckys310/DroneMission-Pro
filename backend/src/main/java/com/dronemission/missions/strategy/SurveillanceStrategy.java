package com.dronemission.missions.strategy;

import com.dronemission.drones.DroneType;
import com.dronemission.missions.MissionType;
import com.dronemission.missions.WaypointAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Vigilancia y monitoreo: recorrido perimetral grabando video. */
@Component
public class SurveillanceStrategy extends AbstractMissionStrategy {

    private static final double WIDTH_M = 300;
    private static final double HEIGHT_M = 200;

    @Override public String name() { return "Recorrido perimetral"; }
    @Override public Set<MissionType> supportedTypes() { return Set.of(MissionType.VIGILANCIA, MissionType.MONITOREO); }
    @Override public int minimumBatteryPercent() { return 60; }
    @Override public Set<DroneType> compatibleDroneTypes() { return Set.of(DroneType.VIGILANCIA, DroneType.INSPECCION, DroneType.FOTOGRAFIA); }
    @Override public double cruiseSpeedKmh() { return 30; }
    @Override public double batteryDrainPerMinute() { return 2.2; }

    @Override
    protected List<PlannedWaypoint> generatePattern(GeoPoint center, int altitudeM) {
        double w = WIDTH_M / 2;
        double h = HEIGHT_M / 2;
        // Esquinas y puntos medios del perímetro, en sentido horario
        double[][] perimeter = {{-w, h}, {0, h}, {w, h}, {w, 0}, {w, -h}, {0, -h}, {-w, -h}, {-w, 0}, {-w, h}};
        List<PlannedWaypoint> points = new ArrayList<>();
        for (double[] p : perimeter) {
            points.add(new PlannedWaypoint(center.offset(p[0], p[1]), altitudeM, WaypointAction.VIDEO));
        }
        return points;
    }
}
