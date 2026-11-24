package com.dronemission.evidence;

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

/**
 * Evidencia de una misión. "url" puede ser:
 *  - una URL externa o ruta pública (/demo/...),
 *  - "file:<nombre>" para archivos subidos al almacenamiento local.
 */
// MongoDB: cada objeto se guarda como un documento en la colección "evidencias"
@Document(collection = "evidencias")
@Getter
@Setter
@NoArgsConstructor
public class Evidence {

    public static final String FILE_PREFIX = "file:";

    // _id numérico: lo asigna SequenceGeneratorService (como el autoincremento de SQL)
    @Id
    private Long id;

    // Referencia a otro documento (DBRef). lazy: la misión solo se carga si se usa
    @DBRef(lazy = true)
    @Field("mision_id")
    private Mission mission;
    @Field("tipo")
    private EvidenceType type;

    @Field("nombre")
    private String name;

    @Field("url")
    private String url;

    @Field("descripcion")
    private String description;

    @Field("visible_cliente")
    private boolean visibleToClient = true;

    // Referencia a otro documento (DBRef): en Mongo se guarda {$ref, $id}
    @DBRef
    @Field("subido_por")
    private User uploadedBy;

    @Field("fecha")
    private LocalDateTime date = LocalDateTime.now();

    public boolean isStoredFile() {
        return url != null && url.startsWith(FILE_PREFIX);
    }
}
