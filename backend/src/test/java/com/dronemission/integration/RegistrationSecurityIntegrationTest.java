package com.dronemission.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Registro público, aprobación por el administrador y protección contra fuerza bruta (CP-019 a CP-022). */
class RegistrationSecurityIntegrationTest extends IntegrationTestBase {

    private Map<String, Object> registration(String username, String password) {
        return Map.of("firstName", "Laura", "lastName", "Mejía", "username", username,
                "email", username + "@correo.example", "password", password, "confirmPassword", password);
    }

    private org.springframework.test.web.servlet.ResultActions register(Map<String, Object> body) throws Exception {
        return mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)));
    }

    private org.springframework.test.web.servlet.ResultActions login(String identifier, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("identifier", identifier, "password", password))));
    }

    @Test
    @DisplayName("CP-019: la cuenta registrada queda pendiente hasta que el administrador la aprueba")
    void registerApproveLogin() throws Exception {
        register(registration("lmejia", "Vuelo2026Seguro")).andExpect(status().isCreated());
        login("lmejia", "Vuelo2026Seguro").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Tu cuenta está pendiente de aprobación por un administrador."));

        String admin = token(ADMIN);
        var users = body(get("/api/users", admin));
        long id = 0;
        for (var u : users) {
            if (u.get("username").asText().equals("lmejia")) id = u.get("id").asLong();
        }
        postJson("/api/users/" + id + "/approve", admin, Map.of("role", "OPERADOR"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(true));
        login("lmejia", "Vuelo2026Seguro").andExpect(status().isOk()).andExpect(jsonPath("$.user.role").value("OPERADOR"));
    }

    @Test
    @DisplayName("CP-019b: al aprobar se puede vincular la ficha de piloto de una cuenta desactivada")
    void approveLinkingPilotFromInactiveAccount() throws Exception {
        String admin = token(ADMIN);
        delete("/api/users/6", admin).andExpect(status().isNoContent()); // desactiva piloto2 (ficha de piloto 2)
        register(registration("vruiz", "Vuelo2026Seguro")).andExpect(status().isCreated());
        long id = 0;
        for (var u : body(get("/api/users", admin))) {
            if (u.get("username").asText().equals("vruiz")) id = u.get("id").asLong();
        }
        postJson("/api/users/" + id + "/approve", admin, Map.of("role", "PILOTO", "pilotId", 2))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pilotId").value(2));
        postJson("/api/users/" + id + "/approve", admin, Map.of("role", "PILOTO"))
                .andExpect(status().isBadRequest()); // ya no está pendiente
    }

    @Test
    @DisplayName("CP-020: rechaza contraseñas débiles, usuarios repetidos y contraseñas distintas")
    void registrationValidations() throws Exception {
        register(registration("debil", "12345678")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("no es segura")));
        register(registration("operador", "Vuelo2026Seguro")).andExpect(status().isConflict());
        Map<String, Object> mismatch = new java.util.HashMap<>(registration("otro", "Vuelo2026Seguro"));
        mismatch.put("confirmPassword", "Distinta2026");
        register(mismatch).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CP-021: la cuenta se bloquea tras 5 intentos fallidos aunque luego se use la contraseña correcta")
    void lockout() throws Exception {
        for (int i = 0; i < 5; i++) {
            login("tecnico", "Incorrecta123").andExpect(status().isUnauthorized());
        }
        login("tecnico", PASSWORD).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("bloqueada")));
    }

    @Test
    @DisplayName("CP-022: la configuración pública no expone datos sensibles")
    void publicConfig() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationEnabled").value(true))
                .andExpect(jsonPath("$.passwordPolicy").exists());
    }
}
