package com.dronemission.missions.event;

import com.dronemission.missions.Mission;

/** Mensaje que el publicador envía a todos los observadores. */
public record MissionEvent(MissionEventType type, Mission mission, String detail) {
}
