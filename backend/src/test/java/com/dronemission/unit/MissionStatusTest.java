package com.dronemission.unit;

import com.dronemission.missions.MissionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas de la máquina de estados de la misión (CP-010 a CP-013). */
class MissionStatusTest {

    @ParameterizedTest(name = "{0} -> {1} es válida")
    @CsvSource({
            "BORRADOR,PENDIENTE", "PENDIENTE,PROGRAMADA", "PROGRAMADA,EN_PREPARACION", "EN_PREPARACION,EN_VUELO",
            "EN_VUELO,PAUSADA", "PAUSADA,EN_VUELO", "EN_VUELO,FINALIZADA", "PAUSADA,FINALIZADA",
            "BORRADOR,CANCELADA", "PROGRAMADA,CANCELADA", "EN_PREPARACION,CANCELADA"
    })
    void allowsValidTransitions(MissionStatus from, MissionStatus to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} es inválida")
    @CsvSource({
            "FINALIZADA,PROGRAMADA", "FINALIZADA,EN_VUELO", "CANCELADA,EN_VUELO", "CANCELADA,PENDIENTE",
            "BORRADOR,EN_VUELO", "PROGRAMADA,EN_VUELO", "EN_VUELO,CANCELADA", "PENDIENTE,FINALIZADA"
    })
    void rejectsInvalidTransitions(MissionStatus from, MissionStatus to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }

    @Test
    @DisplayName("Una misión finalizada o cancelada es terminal y no admite transiciones")
    void terminalStates() {
        assertThat(MissionStatus.FINALIZADA.isTerminal()).isTrue();
        assertThat(MissionStatus.CANCELADA.allowedTransitions()).isEmpty();
    }

    @Test
    @DisplayName("Solo se editan misiones antes del vuelo")
    void editableStates() {
        assertThat(MissionStatus.PROGRAMADA.isEditable()).isTrue();
        assertThat(MissionStatus.EN_VUELO.isEditable()).isFalse();
    }
}
