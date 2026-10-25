package com.dronemission.missions;

import com.dronemission.missions.dto.FlightPlanResponse;
import com.dronemission.missions.dto.WaypointResponse;
import com.dronemission.missions.strategy.GeoPoint;
import com.dronemission.missions.strategy.MissionStrategy;
import com.dronemission.missions.strategy.MissionStrategyResolver;
import com.dronemission.missions.strategy.PlannedWaypoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Genera el plan de vuelo "programado con anticipación" de cada misión
 * usando la estrategia correspondiente a su tipo.
 */
@Service
@RequiredArgsConstructor
public class FlightPlanService {

    /** Minutos fijos de despegue y aterrizaje añadidos a la estimación. */
    private static final double TAKEOFF_LANDING_MIN = 1.5;

    private final MissionStrategyResolver strategyResolver;

    public void generate(Mission mission) {
        MissionStrategy strategy = strategyResolver.forType(mission.getType());
        List<PlannedWaypoint> planned = strategy.generateFlightPlan(
                new GeoPoint(mission.getLatitude(), mission.getLongitude()), mission.getTargetAltitudeM());
        List<Waypoint> waypoints = new ArrayList<>();
        for (int i = 0; i < planned.size(); i++) {
            PlannedWaypoint p = planned.get(i);
            Waypoint w = new Waypoint();
            w.setSequence(i);
            w.setLatitude(round6(p.point().latitude()));
            w.setLongitude(round6(p.point().longitude()));
            w.setAltitudeM(p.altitudeM());
            w.setAction(p.action());
            waypoints.add(w);
        }
        mission.replaceFlightPlan(waypoints);
    }

    public FlightPlanResponse describe(Mission mission) {
        MissionStrategy strategy = strategyResolver.forType(mission.getType());
        List<Waypoint> plan = mission.getFlightPlan();
        double distance = 0;
        for (int i = 1; i < plan.size(); i++) {
            distance += toPoint(plan.get(i - 1)).distanceTo(toPoint(plan.get(i)));
        }
        double minutes = distance / (strategy.cruiseSpeedKmh() / 3.6) / 60 + TAKEOFF_LANDING_MIN;
        double battery = minutes * strategy.batteryDrainPerMinute();
        List<WaypointResponse> waypoints = plan.stream()
                .map(w -> new WaypointResponse(w.getSequence(), w.getLatitude(), w.getLongitude(), w.getAltitudeM(), w.getAction().name()))
                .toList();
        return new FlightPlanResponse(strategy.name(), strategy.minimumBatteryPercent(),
                strategy.compatibleDroneTypes().stream().map(Enum::name).sorted().toList(),
                strategy.cruiseSpeedKmh(), Math.round(distance), round1(minutes), round1(battery), waypoints);
    }

    public static GeoPoint toPoint(Waypoint w) {
        return new GeoPoint(w.getLatitude(), w.getLongitude());
    }

    private static double round6(double v) {
        return Math.round(v * 1_000_000d) / 1_000_000d;
    }

    private static double round1(double v) {
        return Math.round(v * 10d) / 10d;
    }
}
