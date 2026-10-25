package com.dronemission.unit;

import com.dronemission.drones.Drone;
import com.dronemission.drones.DroneStatus;
import com.dronemission.drones.DroneType;
import com.dronemission.missions.*;
import com.dronemission.missions.strategy.InspectionStrategy;
import com.dronemission.missions.strategy.MissionStrategy;
import com.dronemission.pilots.Pilot;
import com.dronemission.pilots.PilotStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;

/** Reglas de asignación de recursos con Mockito (CP-008, reglas de negocio 1-5). */
@ExtendWith(MockitoExtension.class)
class AssignmentRulesTest {

    @Mock
    private MissionAssignmentRepository assignmentRepository;

    @InjectMocks
    private AssignmentService service;

    private final MissionStrategy strategy = new InspectionStrategy();
    private Mission mission;
    private Drone drone;
    private Pilot pilot;

    @BeforeEach
    void setUp() {
        mission = new Mission();
        mission.setId(1L);
        mission.setType(MissionType.INSPECCION);
        mission.setScheduledDate(LocalDate.now().plusDays(1));
        mission.setStartTime(LocalTime.of(9, 0));
        mission.setEstimatedEndTime(LocalTime.of(10, 0));

        drone = new Drone();
        drone.setId(1L);
        drone.setCode("DRN-001");
        drone.setType(DroneType.INSPECCION);
        drone.setStatus(DroneStatus.DISPONIBLE);
        drone.setBatteryLevel(90);

        pilot = new Pilot();
        pilot.setId(1L);
        pilot.setFirstName("Ana");
        pilot.setLastName("Pérez");
        pilot.setStatus(PilotStatus.DISPONIBLE);
        pilot.setLicenseExpiresOn(LocalDate.now().plusYears(1));

        lenient().when(assignmentRepository.droneHasOverlap(anyLong(), anyLong(), any(), any(), any(), any())).thenReturn(false);
        lenient().when(assignmentRepository.pilotHasOverlap(anyLong(), anyLong(), any(), any(), any(), any())).thenReturn(false);
    }

    @Test
    @DisplayName("Un drone y un piloto disponibles son elegibles")
    void eligibleResources() {
        assertThat(service.droneIneligibility(mission, drone, strategy)).isEmpty();
        assertThat(service.pilotIneligibility(mission, pilot)).isEmpty();
    }

    @Test
    @DisplayName("Regla 1: un drone en mantenimiento no puede asignarse")
    void droneInMaintenance() {
        drone.setStatus(DroneStatus.EN_MANTENIMIENTO);
        assertThat(service.droneIneligibility(mission, drone, strategy)).hasValueSatisfying(r -> assertThat(r).contains("mantenimiento"));
    }

    @Test
    @DisplayName("Regla 2: un drone fuera de servicio no puede asignarse")
    void droneOutOfService() {
        drone.setStatus(DroneStatus.FUERA_DE_SERVICIO);
        assertThat(service.droneIneligibility(mission, drone, strategy)).hasValueSatisfying(r -> assertThat(r).contains("fuera de servicio"));
    }

    @Test
    @DisplayName("Un drone sin batería suficiente no puede asignarse")
    void lowBattery() {
        drone.setBatteryLevel(30);
        assertThat(service.droneIneligibility(mission, drone, strategy)).hasValueSatisfying(r -> assertThat(r).contains("batería"));
    }

    @Test
    @DisplayName("Un drone de tipo incompatible no puede asignarse")
    void incompatibleType() {
        drone.setType(DroneType.AGRICULTURA);
        assertThat(service.droneIneligibility(mission, drone, strategy)).hasValueSatisfying(r -> assertThat(r).contains("compatible"));
    }

    @Test
    @DisplayName("Regla 5: un drone no puede tener dos misiones simultáneas")
    void droneOverlap() {
        lenient().when(assignmentRepository.droneHasOverlap(anyLong(), anyLong(), any(), any(), any(), any())).thenReturn(true);
        assertThat(service.droneIneligibility(mission, drone, strategy)).hasValueSatisfying(r -> assertThat(r).contains("horario"));
    }

    @Test
    @DisplayName("Regla 3: un piloto con licencia vencida no puede asignarse")
    void expiredLicense() {
        pilot.setLicenseExpiresOn(LocalDate.now().minusDays(1));
        assertThat(service.pilotIneligibility(mission, pilot)).hasValueSatisfying(r -> assertThat(r).contains("vencida"));
    }

    @Test
    @DisplayName("Regla 4: un piloto no puede tener dos misiones simultáneas")
    void pilotOverlap() {
        lenient().when(assignmentRepository.pilotHasOverlap(anyLong(), anyLong(), any(), any(), any(), any())).thenReturn(true);
        assertThat(service.pilotIneligibility(mission, pilot)).hasValueSatisfying(r -> assertThat(r).contains("horario"));
    }
}
