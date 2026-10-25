package com.dronemission.unit;

import com.dronemission.clients.Client;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.missions.*;
import com.dronemission.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas del patrón Builder de misiones (CP-007). */
class MissionBuilderTest {

    private Client client;
    private User operator;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setName("AgroValle");
        client.setActive(true);
        operator = new User();
    }

    private MissionBuilder validBuilder() {
        return new MissionBuilder()
                .code("MSN-0100").name("Inspección torre").type(MissionType.INSPECCION).client(client)
                .location("Yumbo", 3.58, -76.49).altitude(60)
                .schedule(LocalDate.now().plusDays(1), LocalTime.of(8, 0), LocalTime.of(9, 0))
                .priority(MissionPriority.ALTA).objective("Detectar daños").createdBy(operator);
    }

    @Test
    @DisplayName("Construye una misión válida en estado BORRADOR")
    void buildsValidMission() {
        Mission mission = validBuilder().build();
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.BORRADOR);
        assertThat(mission.getCode()).isEqualTo("MSN-0100");
        assertThat(mission.getClient()).isSameAs(client);
    }

    @Test
    @DisplayName("Rechaza una hora de fin anterior a la de inicio")
    void rejectsInvalidSchedule() {
        assertThatThrownBy(() -> validBuilder().schedule(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(9, 0)).build())
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("posterior");
    }

    @Test
    @DisplayName("Rechaza fechas en el pasado")
    void rejectsPastDate() {
        assertThatThrownBy(() -> validBuilder().schedule(LocalDate.now().minusDays(1), LocalTime.of(8, 0), LocalTime.of(9, 0)).build())
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("pasado");
    }

    @Test
    @DisplayName("Rechaza altitudes por encima del límite legal de 120 m")
    void rejectsAltitude() {
        assertThatThrownBy(() -> validBuilder().altitude(150).build()).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Exige los campos obligatorios")
    void requiresFields() {
        assertThatThrownBy(() -> validBuilder().objective(" ").build()).hasMessageContaining("objetivo");
    }

    @Test
    @DisplayName("No permite misiones para clientes inactivos")
    void rejectsInactiveClient() {
        client.setActive(false);
        assertThatThrownBy(() -> validBuilder().build()).hasMessageContaining("inactivo");
    }
}
