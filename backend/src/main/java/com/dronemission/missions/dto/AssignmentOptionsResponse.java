package com.dronemission.missions.dto;

import java.util.List;

/**
 * Opciones para asignar recursos a una misión. Cada drone y piloto indica si es
 * elegible y, si no lo es, el motivo (las mismas reglas que valida el backend al asignar).
 */
public record AssignmentOptionsResponse(int minimumBatteryPercent, List<DroneOption> drones, List<PilotOption> pilots) {

    public record DroneOption(Long id, String code, String name, String type, String status, Integer batteryLevel,
                              boolean eligible, String reason) {
    }

    public record PilotOption(Long id, String fullName, String status, String licenseExpiresOn, boolean eligible,
                              String reason) {
    }
}
