package com.dronemission.unit;

import com.dronemission.drones.Drone;
import com.dronemission.drones.DroneFactory;
import com.dronemission.drones.DroneStatus;
import com.dronemission.drones.DroneType;
import com.dronemission.drones.dto.DroneRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas del patrón Factory de drones. */
class DroneFactoryTest {

    private final DroneFactory factory = new DroneFactory();

    private DroneRequest request(DroneType type, String code, Integer autonomy) {
        return new DroneRequest(code, "Nuevo", "DJI", "M30", "SN-1", type, null, 90, null, null, autonomy,
                LocalDate.now().minusDays(10), null, null, null);
    }

    @Test
    @DisplayName("Aplica los valores típicos del tipo cuando no se indican")
    void appliesTypeDefaults() {
        Drone drone = factory.createDrone(request(DroneType.AGRICULTURA, null, null), 12);
        assertThat(drone.getBatteryCapacityMah()).isEqualTo(30000);
        assertThat(drone.getAutonomyMinutes()).isEqualTo(18);
        assertThat(drone.getCode()).isEqualTo("DRN-012");
        assertThat(drone.getStatus()).isEqualTo(DroneStatus.DISPONIBLE);
    }

    @Test
    @DisplayName("Respeta los valores que envía el usuario")
    void keepsUserValues() {
        Drone drone = factory.createDrone(request(DroneType.INSPECCION, "drn-500", 33), 1);
        assertThat(drone.getAutonomyMinutes()).isEqualTo(33);
        assertThat(drone.getCode()).isEqualTo("DRN-500");
    }
}
