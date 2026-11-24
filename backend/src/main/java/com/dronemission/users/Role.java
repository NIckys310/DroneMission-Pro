package com.dronemission.users;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/** Rol del sistema. Un rol agrupa varios permisos (embebidos en el mismo documento). */
// MongoDB: cada objeto se guarda como un documento en la colección "roles"
@Document(collection = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    // _id numérico: lo asigna SequenceGeneratorService (como el autoincremento de SQL)
    @Id
    private Long id;
    @Indexed(unique = true)
    @Field("nombre")
    private RoleName name;

    @Field("descripcion")
    private String description;

    // MongoDB: los permisos se guardan EMBEBIDOS dentro del rol (campo "permisos").
    // Reemplaza a la tabla intermedia rol_permisos de SQL y evita otra consulta en cada petición.
    @Field("permisos")
    private Set<Permission> permissions = new HashSet<>();

    public boolean hasPermission(String code) {
        return permissions.stream().anyMatch(p -> p.getCode().equals(code));
    }
}
