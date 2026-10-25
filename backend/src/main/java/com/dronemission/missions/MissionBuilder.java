package com.dronemission.missions;

import com.dronemission.clients.Client;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.users.User;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * PATRÓN BUILDER.
 * Una misión tiene muchos atributos obligatorios y reglas entre ellos
 * (horario coherente, fecha no pasada, altitud legal). El Builder permite
 * construirla paso a paso con una API fluida y valida todo en build(),
 * de modo que nunca existe una Mission a medio construir o inconsistente.
 *
 * Uso:
 *   Mission m = new MissionBuilder()
 *       .code("MSN-0009").name("Inspección torre").type(MissionType.INSPECCION)
 *       .client(cliente).location("Yumbo", 3.58, -76.49).altitude(60)
 *       .schedule(fecha, inicio, fin).priority(MissionPriority.ALTA)
 *       .objective("Detectar daños").createdBy(usuario)
 *       .build();
 */
public class MissionBuilder {

    /** Altitud máxima permitida para drones en Colombia (RAC 100). */
    public static final int MAX_ALTITUDE_M = 120;
    public static final int MIN_ALTITUDE_M = 5;

    private String code;
    private String name;
    private String description;
    private MissionType type;
    private Client client;
    private String location;
    private Double latitude;
    private Double longitude;
    private Integer altitudeM;
    private LocalDate scheduledDate;
    private LocalTime startTime;
    private LocalTime estimatedEndTime;
    private MissionPriority priority = MissionPriority.MEDIA;
    private String objective;
    private String observations;
    private User createdBy;
    private boolean allowPastDate = false;

    public MissionBuilder code(String code) { this.code = code; return this; }
    public MissionBuilder name(String name) { this.name = name; return this; }
    public MissionBuilder description(String description) { this.description = description; return this; }
    public MissionBuilder type(MissionType type) { this.type = type; return this; }
    public MissionBuilder client(Client client) { this.client = client; return this; }
    public MissionBuilder altitude(Integer altitudeM) { this.altitudeM = altitudeM; return this; }
    public MissionBuilder priority(MissionPriority priority) { this.priority = priority; return this; }
    public MissionBuilder objective(String objective) { this.objective = objective; return this; }
    public MissionBuilder observations(String observations) { this.observations = observations; return this; }
    public MissionBuilder createdBy(User createdBy) { this.createdBy = createdBy; return this; }

    /** Solo para cargar datos históricos: permite fechas pasadas. */
    public MissionBuilder allowPastDate() { this.allowPastDate = true; return this; }

    public MissionBuilder location(String location, Double latitude, Double longitude) {
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        return this;
    }

    public MissionBuilder schedule(LocalDate date, LocalTime start, LocalTime estimatedEnd) {
        this.scheduledDate = date;
        this.startTime = start;
        this.estimatedEndTime = estimatedEnd;
        return this;
    }

    /** Valida las reglas y devuelve la misión en estado BORRADOR. */
    public Mission build() {
        require(code, "el código");
        require(name, "el nombre");
        require(type, "el tipo de misión");
        require(client, "el cliente");
        require(location, "la ubicación");
        require(latitude, "la latitud");
        require(longitude, "la longitud");
        require(altitudeM, "la altitud objetivo");
        require(scheduledDate, "la fecha programada");
        require(startTime, "la hora de inicio");
        require(estimatedEndTime, "la hora estimada de finalización");
        require(objective, "el objetivo");
        require(createdBy, "el usuario creador");

        validateSchedule(scheduledDate, startTime, estimatedEndTime, allowPastDate);
        validateAltitude(altitudeM);
        if (!client.isActive()) {
            throw new BusinessRuleException("El cliente " + client.getName() + " está inactivo");
        }

        Mission mission = new Mission();
        mission.setCode(code);
        mission.setName(name.trim());
        mission.setDescription(description);
        mission.setType(type);
        mission.setClient(client);
        mission.setLocation(location.trim());
        mission.setLatitude(latitude);
        mission.setLongitude(longitude);
        mission.setTargetAltitudeM(altitudeM);
        mission.setScheduledDate(scheduledDate);
        mission.setStartTime(startTime);
        mission.setEstimatedEndTime(estimatedEndTime);
        mission.setPriority(priority);
        mission.setObjective(objective.trim());
        mission.setObservations(observations);
        mission.setCreatedBy(createdBy);
        mission.setStatus(MissionStatus.BORRADOR);
        return mission;
    }

    public static void validateSchedule(LocalDate date, LocalTime start, LocalTime end, boolean allowPastDate) {
        if (!allowPastDate && date.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("La fecha programada no puede estar en el pasado");
        }
        if (!end.isAfter(start)) {
            throw new BusinessRuleException("La hora estimada de finalización debe ser posterior a la hora de inicio");
        }
    }

    public static void validateAltitude(int altitudeM) {
        if (altitudeM < MIN_ALTITUDE_M || altitudeM > MAX_ALTITUDE_M) {
            throw new BusinessRuleException("La altitud objetivo debe estar entre " + MIN_ALTITUDE_M
                    + " y " + MAX_ALTITUDE_M + " metros");
        }
    }

    private static void require(Object value, String field) {
        if (value == null || (value instanceof String s && s.isBlank())) {
            throw new BusinessRuleException("Falta " + field + " de la misión");
        }
    }
}
