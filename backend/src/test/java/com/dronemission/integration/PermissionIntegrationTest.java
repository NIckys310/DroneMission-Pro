package com.dronemission.integration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Control de permisos por rol (CP-017): cada rol solo accede a lo que le corresponde. */
class PermissionIntegrationTest extends IntegrationTestBase {

    @ParameterizedTest(name = "{0} GET {1} -> {2}")
    @CsvSource({
            "admin@dronemission.local,    /api/users,       200",
            "admin@dronemission.local,    /api/audit,       200",
            "operador@dronemission.local, /api/users,       403",
            "operador@dronemission.local, /api/dashboard,   200",
            "operador@dronemission.local, /api/pilots,      200",
            "piloto@dronemission.local,   /api/drones,      403",
            "piloto@dronemission.local,   /api/dashboard,   403",
            "piloto@dronemission.local,   /api/pilots/me,   200",
            "tecnico@dronemission.local,  /api/maintenance, 200",
            "tecnico@dronemission.local,  /api/clients,     403",
            "cliente@dronemission.local,  /api/reports,     200",
            "cliente@dronemission.local,  /api/drones,      403",
            "cliente@dronemission.local,  /api/incidents,   403"
    })
    void accessByRole(String email, String url, int expected) throws Exception {
        get(url, token(email)).andExpect(status().is(expected));
    }
}
