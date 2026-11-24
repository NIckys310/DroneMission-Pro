package com.dronemission.reports;

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
 * Historial de reportes generados. El PDF no se guarda: se regenera a partir
 * de los datos actuales cada vez que se descarga (evita duplicar información).
 */
// MongoDB: cada objeto se guarda como un documento en la colección "reportes"
@Document(collection = "reportes")
@Getter
@Setter
@NoArgsConstructor
public class Report {

    // _id numérico: lo asigna SequenceGeneratorService (como el autoincremento de SQL)
    @Id
    private Long id;
    @Field("tipo")
    private ReportType type;

    @Field("titulo")
    private String title;

    // Referencia a otro documento (DBRef). lazy: la misión solo se carga si se usa
    @DBRef(lazy = true)
    @Field("mision_id")
    private Mission mission;

    // Referencia a otro documento (DBRef): en Mongo se guarda {$ref, $id}
    @DBRef
    @Field("generado_por")
    private User generatedBy;

    @Field("generado_en")
    private LocalDateTime generatedAt = LocalDateTime.now();

    @Field("resumen")
    private String summary;
}
