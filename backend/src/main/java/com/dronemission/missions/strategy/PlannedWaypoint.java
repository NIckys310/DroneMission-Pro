package com.dronemission.missions.strategy;

import com.dronemission.missions.WaypointAction;

/** Punto calculado por una estrategia antes de guardarse como Waypoint. */
public record PlannedWaypoint(GeoPoint point, int altitudeM, WaypointAction action) {
}
