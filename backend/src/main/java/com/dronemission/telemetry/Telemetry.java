package com.dronemission.telemetry;

import com.dronemission.missions.Mission;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Lectura de telemetría. En esta versión TODAS son simuladas (simulated = true). */
// MongoDB: cada objeto se guarda como un documento en la colección "telemetria"
@Document(collection = "telemetria")
@Getter
@Setter
@NoArgsConstructor
public class Telemetry {

    // _id numérico: lo asigna SequenceGeneratorService (como el autoincremento de SQL)
    @Id
    private Long id;

    // Referencia a otro documento (DBRef). lazy: la misión solo se carga si se usa
    @DBRef(lazy = true)
    @Field("mision_id")
    private Mission mission;

    @Field("registrado_en")
    private LocalDateTime recordedAt = LocalDateTime.now();

    @Field("latitud")
    private Double latitude;

    @Field("longitud")
    private Double longitude;

    @Field("altitud_m")
    private Double altitudeM;

    @Field("velocidad_kmh")
    private Double speedKmh;

    @Field("bateria")
    private Integer battery;

    @Field("distancia_m")
    private Double distanceM;

    @Field("temperatura_c")
    private Double temperatureC;

    @Field("tiempo_vuelo_s")
    private Integer flightTimeS;

    /** Índice del próximo punto del plan de vuelo al que se dirige el drone. */
    @Field("punto_ruta_actual")
    private Integer currentWaypoint;

    @Field("simulado")
    private boolean simulated = true;
}
