package com.dronemission.unit;

import com.dronemission.drones.DroneType;
import com.dronemission.missions.MissionType;
import com.dronemission.missions.WaypointAction;
import com.dronemission.missions.strategy.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas del patrón Strategy: cada tipo de misión tiene su patrón de vuelo. */
class MissionStrategyTest {

    private final MissionStrategyResolver resolver = new MissionStrategyResolver(List.of(
            new InspectionStrategy(), new AgricultureStrategy(), new SurveillanceStrategy(), new MappingStrategy()));

    @ParameterizedTest
    @EnumSource(MissionType.class)
    @DisplayName("Todo plan de vuelo empieza con despegue y termina con aterrizaje en la base")
    void flightPlanStartsAndEndsAtBase(MissionType type) {
        GeoPoint center = new GeoPoint(3.45, -76.53);
        List<PlannedWaypoint> plan = resolver.forType(type).generateFlightPlan(center, 50);
        assertThat(plan.get(0).action()).isEqualTo(WaypointAction.DESPEGUE);
        assertThat(plan.get(plan.size() - 1).action()).isEqualTo(WaypointAction.ATERRIZAJE);
        assertThat(plan.size()).isGreaterThan(5);
    }

    @Test
    @DisplayName("Agricultura exige drones agrícolas y más batería")
    void agricultureRules() {
        MissionStrategy s = resolver.forType(MissionType.AGRICULTURA);
        assertThat(s.compatibleDroneTypes()).containsExactly(DroneType.AGRICULTURA);
        assertThat(s.minimumBatteryPercent()).isEqualTo(70);
        assertThat(s.generateFlightPlan(new GeoPoint(3.5, -76.3), 15))
                .anyMatch(w -> w.action() == WaypointAction.FUMIGACION);
    }

    @Test
    @DisplayName("Vigilancia y monitoreo comparten la estrategia perimetral")
    void sharedStrategy() {
        assertThat(resolver.forType(MissionType.MONITOREO)).isSameAs(resolver.forType(MissionType.VIGILANCIA));
    }

    @Test
    @DisplayName("La distancia de Haversine es coherente con el desplazamiento")
    void geoOffset() {
        GeoPoint a = new GeoPoint(3.45, -76.53);
        assertThat(a.distanceTo(a.offset(100, 0))).isBetween(99.0, 101.0);
    }
}
