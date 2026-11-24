package com.dronemission.drones;

import com.dronemission.common.persistence.BeforeCreate;
import com.dronemission.common.persistence.BeforeUpdate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Drone de la flota. Su estado lo cambian las misiones y los mantenimientos. */
// MongoDB: cada objeto se guarda como un documento en la colección "drones"
@Document(collection = "drones")
@Getter
@Setter
@NoArgsConstructor
public class Drone {

    // _id numérico: lo asigna SequenceGeneratorService (como el autoincremento de SQL)
    @Id
    private Long id;

    @Indexed(unique = true)
    @Field("codigo")
    private String code;

    @Field("nombre")
    private String name;

    @Field("fabricante")
    private String manufacturer;

    @Field("modelo")
    private String model;

    @Indexed(unique = true)
    @Field("numero_serie")
    private String serialNumber;
    @Field("tipo")
    private DroneType type;
    @Field("estado")
    private DroneStatus status = DroneStatus.DISPONIBLE;

    @Field("capacidad_bateria_mah")
    private Integer batteryCapacityMah;

    /** Porcentaje actual de batería (0-100). */
    @Field("bateria_actual")
    private Integer batteryLevel = 100;

    @Field("horas_vuelo")
    private BigDecimal flightHours = BigDecimal.ZERO;

    @Field("velocidad_maxima_kmh")
    private Integer maxSpeedKmh;

    @Field("autonomia_minutos")
    private Integer autonomyMinutes;

    @Field("fecha_adquisicion")
    private LocalDate acquiredOn;

    @Field("ultimo_mantenimiento")
    private LocalDate lastMaintenanceOn;

    @Field("proximo_mantenimiento")
    private LocalDate nextMaintenanceOn;

    @Field("descripcion")
    private String description;

    @Field("activo")
    private boolean active = true;

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

    /** Un drone se puede asignar si no está en mantenimiento ni fuera de servicio (reglas 1 y 2). */
    public boolean isAssignable() {
        return active && status != DroneStatus.EN_MANTENIMIENTO && status != DroneStatus.FUERA_DE_SERVICIO;
    }

    public boolean hasBatteryAtLeast(int percentage) {
        return batteryLevel != null && batteryLevel >= percentage;
    }

    public void addFlightHours(BigDecimal hours) {
        flightHours = flightHours.add(hours);
    }
}
