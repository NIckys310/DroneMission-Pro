package com.dronemission.incidents;

import com.dronemission.missions.Mission;
import com.dronemission.users.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Incidente ocurrido durante una misión. reportedBy es null cuando lo registra el sistema. */
// MongoDB: cada objeto se guarda como un documento en la colección "incidentes"
@Document(collection = "incidentes")
@Getter
@Setter
@NoArgsConstructor
public class Incident {

    // _id numérico: lo asigna SequenceGeneratorService (como el autoincremento de SQL)
    @Id
    private Long id;

    // Referencia a otro documento (DBRef). lazy: la misión solo se carga si se usa
    @DBRef(lazy = true)
    @Field("mision_id")
    private Mission mission;

    @Field("fecha")
    private LocalDateTime date = LocalDateTime.now();
    @Field("tipo")
    private IncidentType type;
    @Field("severidad")
    private IncidentSeverity severity;

    @Field("descripcion")
    private String description;

    // Referencia a otro documento (DBRef): en Mongo se guarda {$ref, $id}
    @DBRef
    @Field("registrado_por")
    private User reportedBy;
    @Field("estado")
    private IncidentStatus status = IncidentStatus.ABIERTO;

    @Field("resolucion")
    private String resolution;
}
