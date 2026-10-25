package com.dronemission.missions.dto;

public record WaypointResponse(int sequence, double latitude, double longitude, int altitudeM, String action) {
}
