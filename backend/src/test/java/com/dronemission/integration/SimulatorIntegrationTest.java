package com.dronemission.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Plan de práctica del simulador 3D (CP-023). */
class SimulatorIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("CP-023: genera el plan con la misma estrategia que las misiones reales y valida la altitud")
    void practicePlan() throws Exception {
        String technician = token(TECHNICIAN); // cualquier rol puede practicar
        get("/api/simulator/plan?type=AGRICULTURA&altitudeM=15", technician).andExpect(status().isOk())
                .andExpect(jsonPath("$.strategy").value("Zigzag de fumigación"))
                .andExpect(jsonPath("$.waypoints[0].action").value("DESPEGUE"));
        get("/api/simulator/plan?type=INSPECCION&altitudeM=200", technician).andExpect(status().isBadRequest());
    }
}
