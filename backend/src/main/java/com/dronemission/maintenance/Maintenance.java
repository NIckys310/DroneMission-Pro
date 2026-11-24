package com.dronemission.maintenance;

import com.dronemission.drones.Drone;
import com.dronemission.users.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mantenimiento de un drone realizado por un técnico. */
// MongoDB: cada objeto se guarda como un documento en la colección "mantenimientos"
@Document(collection = "mantenimientos")
@Getter
@Setter
@NoArgsConstructor
public class Maintenance {

    // _id numérico: lo asigna SequenceGeneratorService (como el autoincremento de SQL)
    @Id
    private Long id;

    // Referencia a otro documento (DBRef): en Mongo se guarda {$ref, $id}
    @DBRef
    @Field("drone_id")
    private Drone drone;

    // Referencia a otro documento (DBRef): en Mongo se guarda {$ref, $id}
    @DBRef
    @Field("tecnico_id")
    private User technician;
    @Field("tipo")
    private MaintenanceType type;

    @Field("fecha")
    private LocalDate date;

    @Field("descripcion")
    private String description;

    @Field("costo")
    private BigDecimal cost = BigDecimal.ZERO;
    @Field("estado")
    private MaintenanceStatus status = MaintenanceStatus.PROGRAMADO;

    @Field("proxima_fecha")
    private LocalDate nextDate;

    @Field("observaciones")
    private String observations;

    @Field("iniciado_en")
    private LocalDateTime startedAt;

    @Field("completado_en")
    private LocalDateTime completedAt;

    @Field("creado_en")
    private LocalDateTime createdAt = LocalDateTime.now();
}
