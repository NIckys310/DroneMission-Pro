package com.dronemission.missions.strategy;

import com.dronemission.drones.DroneType;
import com.dronemission.missions.MissionType;
import com.dronemission.missions.WaypointAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Topografía: cuadrícula de líneas paralelas escaneando el terreno (fotogrametría). */
@Component
public class MappingStrategy extends AbstractMissionStrategy {

    private static final double AREA_WIDTH_M = 250;
    private static final double AREA_HEIGHT_M = 200;
    private static final double LINE_SPACING_M = 40;

    @Override public String name() { return "Cuadrícula fotogramétrica"; }
    @Override public Set<MissionType> supportedTypes() { return Set.of(MissionType.TOPOGRAFIA); }
    @Override public int minimumBatteryPercent() { return 60; }
    @Override public Set<DroneType> compatibleDroneTypes() { return Set.of(DroneType.TOPOGRAFIA, DroneType.FOTOGRAFIA); }
    @Override public double cruiseSpeedKmh() { return 36; }
    @Override public double batteryDrainPerMinute() { return 2.0; }

    @Override
    protected List<PlannedWaypoint> generatePattern(GeoPoint center, int altitudeM) {
        List<PlannedWaypoint> points = new ArrayList<>();
        int lines = (int) (AREA_WIDTH_M / LINE_SPACING_M) + 1;
        for (int line = 0; line < lines; line++) {
            double east = -AREA_WIDTH_M / 2 + line * LINE_SPACING_M;
            boolean southToNorth = line % 2 == 0;
            double startNorth = southToNorth ? -AREA_HEIGHT_M / 2 : AREA_HEIGHT_M / 2;
            points.add(new PlannedWaypoint(center.offset(east, startNorth), altitudeM, WaypointAction.ESCANEO));
            points.add(new PlannedWaypoint(center.offset(east, -startNorth), altitudeM, WaypointAction.ESCANEO));
        }
        return points;
    }
}
