package com.dronemission.integration;

import com.dronemission.telemetry.TelemetrySimulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo completo de una misión: crear, enviar, asignar, preparar, despegar,
 * telemetría simulada, incidente, evidencia, finalizar y reporte (CP-006 a CP-014).
 */
class MissionFlowIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TelemetrySimulator simulator;

    private Map<String, Object> missionBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Inspección de prueba");
        body.put("type", "INSPECCION");
        body.put("clientId", 2);
        body.put("location", "Yumbo");
        body.put("latitude", 3.58);
        body.put("longitude", -76.49);
        body.put("targetAltitudeM", 50);
        body.put("scheduledDate", LocalDate.now().plusDays(3).toString());
        body.put("startTime", "07:00");
        body.put("estimatedEndTime", "07:45");
        body.put("priority", "ALTA");
        body.put("objective", "Prueba de integración");
        return body;
    }

    @Test
    @DisplayName("Ciclo de vida completo de una misión")
    void fullLifecycle() throws Exception {
        String op = token(OPERATOR);
        String pilot = token(PILOT);

        long id = body(postJson("/api/missions", op, missionBody()).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BORRADOR"))).get("id").asLong();
        get("/api/missions/" + id + "/flight-plan", op).andExpect(jsonPath("$.waypoints.length()").value(18));

        postJson("/api/missions/" + id + "/submit", op, null).andExpect(jsonPath("$.status").value("PENDIENTE"));

        // Regla 1: drone en mantenimiento -> 400 con mensaje claro
        postJson("/api/missions/" + id + "/assign", op, Map.of("droneId", 4, "pilotId", 1))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("El drone DRN-004 está en mantenimiento"));
        // Regla 3: piloto con licencia vencida -> 400
        postJson("/api/missions/" + id + "/assign", op, Map.of("droneId", 1, "pilotId", 3))
                .andExpect(status().isBadRequest());

        postJson("/api/missions/" + id + "/assign", op, Map.of("droneId", 1, "pilotId", 1))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PROGRAMADA"))
                .andExpect(jsonPath("$.drone.code").value("DRN-001"));

        // No se puede despegar sin el chequeo prevuelo (transición inválida)
        postJson("/api/missions/" + id + "/start", pilot, null).andExpect(status().isConflict());
        // Solo el piloto asignado opera la misión
        postJson("/api/missions/" + id + "/prepare", token("piloto2@dronemission.local"), null)
                .andExpect(status().isForbidden());

        postJson("/api/missions/" + id + "/prepare", pilot, null)
                .andExpect(jsonPath("$.status").value("EN_PREPARACION")).andExpect(jsonPath("$.drone.status").value("EN_MISION"));
        postJson("/api/missions/" + id + "/start", pilot, null).andExpect(jsonPath("$.status").value("EN_VUELO"));

        simulator.tick();
        simulator.tick();
        get("/api/missions/" + id + "/telemetry", pilot).andExpect(status().isOk())
                .andExpect(jsonPath("$.simulated").value(true))
                .andExpect(jsonPath("$.points.length()").value(2));

        postJson("/api/missions/" + id + "/incidents", pilot, Map.of("type", "OBSTACULO", "severity", "MEDIA", "description", "Ave"))
                .andExpect(status().isCreated());
        postJson("/api/missions/" + id + "/evidences", pilot, Map.of("type", "FOTOGRAFIA", "name", "Foto", "url", "https://example.com/a.jpg"))
                .andExpect(status().isCreated());

        postJson("/api/missions/" + id + "/pause", pilot, null).andExpect(jsonPath("$.status").value("PAUSADA"));
        postJson("/api/missions/" + id + "/resume", pilot, null).andExpect(jsonPath("$.status").value("EN_VUELO"));
        postJson("/api/missions/" + id + "/finish", pilot, Map.of("text", "Sin novedades"))
                .andExpect(jsonPath("$.status").value("FINALIZADA")).andExpect(jsonPath("$.drone.status").value("DISPONIBLE"));

        // Regla 8: una misión finalizada no se reabre
        postJson("/api/missions/" + id + "/prepare", pilot, null).andExpect(status().isConflict());

        get("/api/reports/missions/" + id, op).andExpect(status().isOk())
                .andExpect(jsonPath("$.incidents.length()").value(1))
                .andExpect(jsonPath("$.evidences.length()").value(1))
                .andExpect(jsonPath("$.telemetry.readings").value(2));
        get("/api/missions/" + id + "/history", op).andExpect(jsonPath("$.length()").value(8));
    }

    @Test
    @DisplayName("Regla 9: una misión cancelada no puede iniciarse")
    void cancelledCannotStart() throws Exception {
        String op = token(OPERATOR);
        postJson("/api/missions/4/cancel", op, Map.of("reason", "Lluvia")).andExpect(jsonPath("$.status").value("CANCELADA"));
        postJson("/api/missions/4/prepare", token("piloto2@dronemission.local"), null).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Regla 4/5: no se asignan recursos con horarios cruzados")
    void overlapIsRejected() throws Exception {
        String op = token(OPERATOR);
        Map<String, Object> body = missionBody();
        body.put("scheduledDate", LocalDate.now().toString());
        body.put("type", "TOPOGRAFIA");
        body.put("startTime", "10:00");
        body.put("estimatedEndTime", "10:30"); // se cruza con MSN-0003 (09:00-11:00, DRN-008 y piloto 1)
        long id = body(postJson("/api/missions", op, body)).get("id").asLong();
        postJson("/api/missions/" + id + "/submit", op, null);
        postJson("/api/missions/" + id + "/assign", op, Map.of("droneId", 8, "pilotId", 2))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("El drone DRN-008 ya tiene otra misión en ese horario"));
    }

    @Test
    @DisplayName("Regla 12: el cliente solo ve sus misiones; el piloto solo las asignadas")
    void scopedVisibility() throws Exception {
        get("/api/missions", token(CLIENT)).andExpect(jsonPath("$.length()").value(3));
        get("/api/missions/1", token(CLIENT)).andExpect(status().isForbidden());
        get("/api/missions", token(PILOT)).andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("Solo se eliminan borradores")
    void deleteOnlyDrafts() throws Exception {
        String op = token(OPERATOR);
        delete("/api/missions/5", op).andExpect(status().isConflict());
        delete("/api/missions/6", op).andExpect(status().isNoContent());
    }
}
