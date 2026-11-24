package com.dronemission.missions;

import com.dronemission.clients.Client;
import com.dronemission.common.exception.InvalidStateTransitionException;
import com.dronemission.users.User;
import com.dronemission.common.persistence.BeforeCreate;
import com.dronemission.common.persistence.BeforeUpdate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Misión de vuelo: entidad central del sistema. Se construye con MissionBuilder. */
// MongoDB: cada objeto se guarda como un documento en la colección "misiones"
@Document(collection = "misiones")
@Getter
@Setter
@NoArgsConstructor
public class Mission {

    // _id numérico: lo asigna SequenceGeneratorService (como el autoincremento de SQL)
    @Id
    private Long id;

    @Indexed(unique = true)
    @Field("codigo")
    private String code;

    @Field("nombre")
    private String name;

    @Field("descripcion")
    private String description;
    @Field("tipo")
    private MissionType type;

    // Referencia a otro documento (DBRef): en Mongo se guarda {$ref, $id}
    @DBRef
    @Field("cliente_id")
    private Client client;

    @Field("ubicacion")
    private String location;

    @Field("latitud")
    private Double latitude;

    @Field("longitud")
    private Double longitude;

    @Field("altitud_objetivo_m")
    private Integer targetAltitudeM;

    @Field("fecha_programada")
    private LocalDate scheduledDate;

    @Field("hora_inicio")
    private LocalTime startTime;

    @Field("hora_fin_estimada")
    private LocalTime estimatedEndTime;
    @Field("prioridad")
    private MissionPriority priority;
    @Field("estado")
    private MissionStatus status = MissionStatus.BORRADOR;

    @Field("objetivo")
    private String objective;

    @Field("observaciones")
    private String observations;

    // Referencia a otro documento (DBRef): en Mongo se guarda {$ref, $id}
    @DBRef
    @Field("creado_por")
    private User createdBy;

    @Field("inicio_real")
    private LocalDateTime actualStart;

    @Field("fin_real")
    private LocalDateTime actualEnd;

    // Historial de asignaciones (drone + piloto). Cada asignación es un documento de la colección
    // "asignaciones_mision"; aquí se guarda la lista de referencias en orden de asignación.
    @DBRef
    @Field("asignaciones")
    private List<MissionAssignment> assignments = new ArrayList<>();

    // Plan de vuelo EMBEBIDO en la misión (composición): los puntos se guardan dentro del mismo documento
    @Field("plan_vuelo")
    private List<Waypoint> flightPlan = new ArrayList<>();

    @Field("creado_en")
    private LocalDateTime createdAt;

    @Field("actualizado_en")
    private LocalDateTime updatedAt;

    // Se ejecuta antes de guardar un documento nuevo (lo llama MongoLifecycleListener)
    @BeforeCreate
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    // Se ejecuta antes de actualizar un documento existente (lo llama MongoLifecycleListener)
    @BeforeUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** Cambia el estado validando la máquina de estados. */
    public void changeStatus(MissionStatus target) {
        assertCanTransitionTo(target);
        this.status = target;
    }

    /** Lanza una excepción si la transición no está permitida (sin cambiar el estado). */
    public void assertCanTransitionTo(MissionStatus target) {
        if (status == MissionStatus.CANCELADA) {
            throw new InvalidStateTransitionException("La misión " + code + " está cancelada: no puede reabrirse ni iniciarse");
        }
        if (!status.canTransitionTo(target)) {
            throw new InvalidStateTransitionException(
                    "No se puede pasar la misión " + code + " de " + status + " a " + target);
        }
    }

    /** Asignación vigente (drone + piloto), si existe. */
    public Optional<MissionAssignment> getActiveAssignment() {
        return assignments.stream().filter(MissionAssignment::isActive).reduce((first, second) -> second);
    }

    public void replaceFlightPlan(List<Waypoint> waypoints) {
        flightPlan.clear();
        // Los puntos van embebidos en la misión, por eso no necesitan referencia a ella
        flightPlan.addAll(waypoints);
    }

    public void appendObservation(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        observations = (observations == null || observations.isBlank()) ? text.trim() : observations + "\n" + text.trim();
        if (observations.length() > 1000) {
            observations = observations.substring(observations.length() - 1000);
        }
    }
}
