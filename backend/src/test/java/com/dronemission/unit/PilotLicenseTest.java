package com.dronemission.unit;

import com.dronemission.pilots.Pilot;
import com.dronemission.pilots.PilotStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Regla: un piloto con licencia vencida queda en estado LICENCIA_VENCIDA. */
class PilotLicenseTest {

    @Test
    @DisplayName("Marca la licencia vencida y la libera al renovarla")
    void refreshesLicenseStatus() {
        Pilot pilot = new Pilot();
        pilot.setStatus(PilotStatus.DISPONIBLE);
        pilot.setLicenseExpiresOn(LocalDate.now().minusDays(1));
        pilot.refreshLicenseStatus(LocalDate.now());
        assertThat(pilot.getStatus()).isEqualTo(PilotStatus.LICENCIA_VENCIDA);

        pilot.setLicenseExpiresOn(LocalDate.now().plusYears(1));
        pilot.refreshLicenseStatus(LocalDate.now());
        assertThat(pilot.getStatus()).isEqualTo(PilotStatus.DISPONIBLE);
    }

    @Test
    @DisplayName("No cambia el estado de un piloto que está volando")
    void doesNotTouchFlyingPilot() {
        Pilot pilot = new Pilot();
        pilot.setStatus(PilotStatus.EN_MISION);
        pilot.setLicenseExpiresOn(LocalDate.now().minusDays(1));
        pilot.refreshLicenseStatus(LocalDate.now());
        assertThat(pilot.getStatus()).isEqualTo(PilotStatus.EN_MISION);
    }
}
