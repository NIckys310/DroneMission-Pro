package com.dronemission.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** CU-004 Gestionar pilotos (CP-005). */
class PilotCrudIntegrationTest extends IntegrationTestBase {

    private Map<String, Object> pilotBody(String license, LocalDate expires) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Mateo");
        body.put("lastName", "Gil Rendón");
        body.put("document", "1100" + license.hashCode());
        body.put("email", "mateo." + Math.abs(license.hashCode()) + "@dronemission.local");
        body.put("phone", "300 555 0000");
        body.put("licenseNumber", license);
        body.put("licenseExpiresOn", expires.toString());
        body.put("certifications", List.of(Map.of("name", "RPAS", "issuer", "Aerocivil", "issuedOn", "2025-01-10")));
        return body;
    }

    @Test
    @DisplayName("Crear y editar un piloto con certificaciones")
    void createAndUpdate() throws Exception {
        String admin = token(ADMIN);
        var created = body(postJson("/api/pilots", admin, pilotBody("UAEAC-NEW-1", LocalDate.now().plusYears(1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DISPONIBLE"))
                .andExpect(jsonPath("$.certifications.length()").value(1)));
        Map<String, Object> update = pilotBody("UAEAC-NEW-1", LocalDate.now().plusYears(2));
        update.put("document", created.get("document").asText());
        update.put("email", created.get("email").asText());
        update.put("status", "NO_DISPONIBLE");
        putJson("/api/pilots/" + created.get("id").asLong(), admin, update)
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("NO_DISPONIBLE"));
    }

    @Test
    @DisplayName("Un piloto creado con licencia vencida queda LICENCIA_VENCIDA")
    void expiredLicenseOnCreate() throws Exception {
        postJson("/api/pilots", token(ADMIN), pilotBody("UAEAC-OLD-1", LocalDate.now().minusDays(3)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("LICENCIA_VENCIDA"));
    }

    @Test
    @DisplayName("Rechaza licencias duplicadas")
    void duplicateLicense() throws Exception {
        postJson("/api/pilots", token(ADMIN), pilotBody("UAEAC-RPA-2023-0145", LocalDate.now().plusYears(1)))
                .andExpect(status().isConflict());
    }
}
