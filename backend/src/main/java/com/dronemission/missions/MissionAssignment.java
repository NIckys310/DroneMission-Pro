package com.dronemission.missions;

import com.dronemission.drones.Drone;
import com.dronemission.pilots.Pilot;
import com.dronemission.users.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Asignación de un drone y un piloto a una misión.
 * Se guarda el historial: al reasignar, la asignación anterior queda inactiva.
 */
// MongoDB: cada objeto se guarda como un documento en la colección "asignaciones_mision"
@Document(collection = "asignaciones_mision")
@Getter
@Setter
@NoArgsConstructor
public class MissionAssignment {

    // _id numérico: lo asigna SequenceGeneratorService (como el autoincremento de SQL)
    @Id
    private Long id;

    // Referencia a otro documento (DBRef). lazy: la misión solo se carga si se usa
    @DBRef(lazy = true)
    @Field("mision_id")
    private Mission mission;

    // Referencia a otro documento (DBRef): en Mongo se guarda {$ref, $id}
    @DBRef
    @Field("drone_id")
    private Drone drone;

    // Referencia a otro documento (DBRef): en Mongo se guarda {$ref, $id}
    @DBRef
    @Field("piloto_id")
    private Pilot pilot;

    // Referencia a otro documento (DBRef): en Mongo se guarda {$ref, $id}
    @DBRef
    @Field("asignado_por")
    private User assignedBy;

    @Field("fecha_asignacion")
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Field("activa")
    private boolean active = true;
}
