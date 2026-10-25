package com.dronemission.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** CU-001 Iniciar sesión (CP-001 a CP-003). */
class AuthIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("CP-001: login correcto devuelve token, rol y permisos")
    void loginOk() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@dronemission.local\",\"password\":\"Demo2026!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.user.permissions").isArray());
    }

    @Test
    @DisplayName("CP-002: contraseña incorrecta devuelve 401 con mensaje genérico")
    void wrongPassword() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@dronemission.local\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Correo o contraseña incorrectos"));
    }

    @Test
    @DisplayName("CP-002: correo con formato inválido devuelve 400")
    void invalidEmail() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"no-es-correo\",\"password\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    @DisplayName("CP-003: sin token la API responde 401; con token, /me devuelve el usuario")
    void meRequiresToken() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
        get("/api/auth/me", token(PILOT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PILOTO"))
                .andExpect(jsonPath("$.pilotId").value(1));
    }
}
