package com.dronemission.clients;

import com.dronemission.users.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Cliente que contrata misiones. Puede tener un usuario para consultar sus misiones. */
// MongoDB: cada objeto se guarda como un documento en la colección "clientes"
@Document(collection = "clientes")
@Getter
@Setter
@NoArgsConstructor
public class Client {

    // _id numérico: lo asigna SequenceGeneratorService (como el autoincremento de SQL)
    @Id
    private Long id;
    @Field("tipo")
    private ClientType type;

    @Field("nombre")
    private String name;

    @Indexed(unique = true)
    @Field("documento")
    private String document;

    @Field("correo")
    private String email;

    @Field("telefono")
    private String phone;

    @Field("direccion")
    private String address;

    @Field("ciudad")
    private String city;

    @Field("activo")
    private boolean active = true;

    // Referencia a otro documento (DBRef): en Mongo se guarda {$ref, $id}
    @DBRef
    @Field("usuario_id")
    private User user;

    @Field("creado_en")
    private LocalDateTime createdAt = LocalDateTime.now();
}
