package com.dronemission.pilots.dto;

import java.time.LocalDate;

/** Misión en el historial de un piloto. */
public record PilotMissionEntry(Long missionId, String code, String name, String status, LocalDate scheduledDate,
                                String droneCode) {
}
