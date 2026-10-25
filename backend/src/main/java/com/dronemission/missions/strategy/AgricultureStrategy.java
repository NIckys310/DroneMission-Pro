package com.dronemission.missions.strategy;

import com.dronemission.drones.DroneType;
import com.dronemission.missions.MissionType;
import com.dronemission.missions.WaypointAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Agricultura: recorrido en "zigzag" (pasadas paralelas) a baja altura fumigando. */
@Component
public class AgricultureStrategy extends AbstractMissionStrategy {

    private static final double FIELD_WIDTH_M = 200;
    private static final double FIELD_HEIGHT_M = 150;
    private static final double ROW_SPACING_M = 25;

    @Override public String name() { return "Zigzag de fumigación"; }
    @Override public Set<MissionType> supportedTypes() { return Set.of(MissionType.AGRICULTURA); }
    @Override public int minimumBatteryPercent() { return 70; }
    @Override public Set<DroneType> compatibleDroneTypes() { return Set.of(DroneType.AGRICULTURA); }
    @Override public double cruiseSpeedKmh() { return 18; }
    @Override public double batteryDrainPerMinute() { return 3.5; }

    @Override
    protected List<PlannedWaypoint> generatePattern(GeoPoint center, int altitudeM) {
        List<PlannedWaypoint> points = new ArrayList<>();
        int rows = (int) (FIELD_HEIGHT_M / ROW_SPACING_M) + 1;
        for (int row = 0; row < rows; row++) {
            double north = -FIELD_HEIGHT_M / 2 + row * ROW_SPACING_M;
            boolean leftToRight = row % 2 == 0;
            double startEast = leftToRight ? -FIELD_WIDTH_M / 2 : FIELD_WIDTH_M / 2;
            points.add(new PlannedWaypoint(center.offset(startEast, north), altitudeM, WaypointAction.FUMIGACION));
            points.add(new PlannedWaypoint(center.offset(-startEast, north), altitudeM, WaypointAction.PASO));
        }
        return points;
    }
}
