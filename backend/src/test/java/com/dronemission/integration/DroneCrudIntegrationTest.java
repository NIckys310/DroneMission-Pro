package com.dronemission.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** CU-003 Gestionar drones (CP-004). */
class DroneCrudIntegrationTest extends IntegrationTestBase {

    private Map<String, Object> droneBody(String serial) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Halcón III");
        body.put("manufacturer", "DJI");
        body.put("model", "Matrice 30T");
        body.put("serialNumber", serial);
        body.put("type", "INSPECCION");
        body.put("batteryLevel", 100);
        body.put("acquiredOn", LocalDate.now().minusDays(5).toString());
        return body;
    }

    @Test
    @DisplayName("Crear, consultar, editar y desactivar un drone")
    void crud() throws Exception {
        String admin = token(ADMIN);
        var created = body(postJson("/api/drones", admin, droneBody("M30T-0001")).andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("DRN-009"))
                .andExpect(jsonPath("$.autonomyMinutes").value(50))); // valor por defecto del Factory
        long id = created.get("id").asLong();

        get("/api/drones/" + id, admin).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DISPONIBLE"));

        Map<String, Object> update = droneBody("M30T-0001");
        update.put("name", "Halcón III Térmico");
        putJson("/api/drones/" + id, admin, update).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Halcón III Térmico"));

        delete("/api/drones/" + id, admin).andExpect(status().isNoContent());
        get("/api/drones/" + id, admin).andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Rechaza números de serie duplicados (409) y datos inválidos (400)")
    void validations() throws Exception {
        String admin = token(ADMIN);
        postJson("/api/drones", admin, droneBody("M300-8841-A")).andExpect(status().isConflict());
        Map<String, Object> invalid = droneBody("X-1");
        invalid.put("batteryLevel", 150);
        postJson("/api/drones", admin, invalid).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.batteryLevel").exists());
    }

    @Test
    @DisplayName("Búsqueda y filtros por estado")
    void filters() throws Exception {
        get("/api/drones?status=EN_MANTENIMIENTO", token(TECHNICIAN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].code").value("DRN-004"));
        get("/api/drones?q=halc", token(OPERATOR)).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("El operador puede consultar pero no crear drones (403)")
    void operatorCannotCreate() throws Exception {
        postJson("/api/drones", token(OPERATOR), droneBody("SN-X")).andExpect(status().isForbidden());
    }
}
