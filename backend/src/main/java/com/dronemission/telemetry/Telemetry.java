package com.dronemission.telemetry;

import com.dronemission.missions.Mission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Lectura de telemetría. En esta versión TODAS son simuladas (simulated = true). */
@Entity
@Table(name = "telemetria")
@Getter
@Setter
@NoArgsConstructor
public class Telemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mision_id")
    private Mission mission;

    @Column(name = "registrado_en", nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    @Column(name = "latitud", nullable = false)
    private Double latitude;

    @Column(name = "longitud", nullable = false)
    private Double longitude;

    @Column(name = "altitud_m", nullable = false)
    private Double altitudeM;

    @Column(name = "velocidad_kmh", nullable = false)
    private Double speedKmh;

    @Column(name = "bateria", nullable = false)
    private Integer battery;

    @Column(name = "distancia_m", nullable = false)
    private Double distanceM;

    @Column(name = "temperatura_c", nullable = false)
    private Double temperatureC;

    @Column(name = "tiempo_vuelo_s", nullable = false)
    private Integer flightTimeS;

    /** Índice del próximo punto del plan de vuelo al que se dirige el drone. */
    @Column(name = "punto_ruta_actual", nullable = false)
    private Integer currentWaypoint;

    @Column(name = "simulado", nullable = false)
    private boolean simulated = true;
}
