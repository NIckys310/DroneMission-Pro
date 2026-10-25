package com.dronemission.missions.strategy;

import com.dronemission.missions.WaypointAction;

import java.util.ArrayList;
import java.util.List;

/** Código común a todas las estrategias: despegue y aterrizaje en el punto base. */
public abstract class AbstractMissionStrategy implements MissionStrategy {

    @Override
    public List<PlannedWaypoint> generateFlightPlan(GeoPoint center, int altitudeM) {
        List<PlannedWaypoint> plan = new ArrayList<>();
        plan.add(new PlannedWaypoint(center, 0, WaypointAction.DESPEGUE));
        plan.addAll(generatePattern(center, altitudeM));
        plan.add(new PlannedWaypoint(center, 0, WaypointAction.ATERRIZAJE));
        return plan;
    }

    /** Recorrido propio de cada tipo de misión (entre el despegue y el aterrizaje). */
    protected abstract List<PlannedWaypoint> generatePattern(GeoPoint center, int altitudeM);
}
