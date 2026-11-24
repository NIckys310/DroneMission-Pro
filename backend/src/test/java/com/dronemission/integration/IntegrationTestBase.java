package com.dronemission.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.dronemission.config.DataSeeder;
import com.dronemission.missions.MissionService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base de las pruebas de integración: levanta toda la aplicación con MongoDB embebido
 * (con los datos demo) y hace peticiones HTTP reales con MockMvc.
 * Antes de cada prueba la base se borra y se vuelve a llenar, así cada prueba empieza
 * con los mismos datos (antes lo hacía el rollback de @Transactional con H2).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"embedded", "test"})
abstract class IntegrationTestBase {

    protected static final String PASSWORD = "Demo2026!";
    protected static final String ADMIN = "admin@dronemission.local";
    protected static final String OPERATOR = "operador@dronemission.local";
    protected static final String PILOT = "piloto@dronemission.local";
    protected static final String TECHNICIAN = "tecnico@dronemission.local";
    protected static final String CLIENT = "cliente@dronemission.local";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DataSeeder dataSeeder;

    @Autowired
    private MissionService missionService;

    /** Base limpia en cada prueba: se borra la base de pruebas y se cargan los datos demo otra vez. */
    @BeforeEach
    void resetDatabase() {
        mongoTemplate.getDb().drop();
        dataSeeder.seed();
        missionService.ensureFlightPlans();
    }

    protected String token(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }

    protected ResultActions get(String url, String token) throws Exception {
        return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url)
                .header("Authorization", "Bearer " + token));
    }

    protected ResultActions postJson(String url, String token, Object body) throws Exception {
        return mvc.perform(post(url).header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body == null ? "" : json.writeValueAsString(body)));
    }

    protected ResultActions putJson(String url, String token, Object body) throws Exception {
        return mvc.perform(put(url).header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)));
    }

    protected ResultActions delete(String url, String token) throws Exception {
        return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(url)
                .header("Authorization", "Bearer " + token));
    }

    protected JsonNode body(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }
}
