package com.dronemission.audit;

import com.dronemission.users.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Registro de auditoría: quién hizo qué, sobre qué entidad y cuándo. */
// MongoDB: cada objeto se guarda como un documento en la colección "auditoria"
@Document(collection = "auditoria")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    // _id numérico: lo asigna SequenceGeneratorService (como el autoincremento de SQL)
    @Id
    private Long id;

    // Referencia a otro documento (DBRef): en Mongo se guarda {$ref, $id}
    @DBRef
    @Field("usuario_id")
    private User user;

    @Field("correo_usuario")
    private String userEmail;

    @Field("accion")
    private String action;

    @Field("entidad")
    private String entity;

    @Field("entidad_id")
    private Long entityId;

    @Field("descripcion")
    private String description;

    @Field("fecha")
    private LocalDateTime date = LocalDateTime.now();
}
