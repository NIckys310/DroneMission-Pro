package com.dronemission.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** CU-013 Gestionar mantenimiento y CU-011 Registrar incidente (CP-015, CP-016). */
class MaintenanceAndIncidentIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("Al iniciar un mantenimiento el drone queda EN_MANTENIMIENTO y al completarlo DISPONIBLE")
    void maintenanceChangesDroneStatus() throws Exception {
        String tech = token(TECHNICIAN);
        long id = body(postJson("/api/maintenance", tech, Map.of("droneId", 5, "type", "CALIBRACION",
                "date", LocalDate.now().toString(), "description", "Calibrar gimbal", "cost", 90000))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PROGRAMADO"))).get("id").asLong();

        postJson("/api/maintenance/" + id + "/start", tech, null)
                .andExpect(jsonPath("$.status").value("EN_PROCESO")).andExpect(jsonPath("$.droneStatus").value("EN_MANTENIMIENTO"));
        postJson("/api/maintenance/" + id + "/complete", tech, Map.of("observations", "OK", "nextDate", LocalDate.now().plusMonths(3).toString()))
                .andExpect(jsonPath("$.status").value("COMPLETADO")).andExpect(jsonPath("$.droneStatus").value("DISPONIBLE"));
        get("/api/drones/5", tech).andExpect(jsonPath("$.batteryLevel").value(100));
    }

    @Test
    @DisplayName("El técnico no puede gestionar misiones (regla 13)")
    void technicianCannotManageMissions() throws Exception {
        get("/api/missions", token(TECHNICIAN)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("El piloto asignado registra incidentes; el operador los resuelve")
    void incidents() throws Exception {
        long id = body(postJson("/api/missions/3/incidents", token(PILOT), Map.of("type", "CLIMA", "severity", "BAJA", "description", "Viento"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.reportedBy").value("Andrés Torres Molina"))).get("id").asLong();
        postJson("/api/missions/4/incidents", token(PILOT), Map.of("type", "CLIMA", "severity", "BAJA", "description", "x"))
                .andExpect(status().isForbidden()); // no es su misión
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/incidents/" + id + "/status")
                        .header("Authorization", "Bearer " + token(OPERATOR)).contentType("application/json")
                        .content("{\"status\":\"RESUELTO\",\"resolution\":\"Se esperó\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESUELTO"));
    }
}
