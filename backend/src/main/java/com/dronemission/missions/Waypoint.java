package com.dronemission.missions;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Punto del plan de vuelo programado con anticipación.
 * El drone virtual recorre los puntos en orden durante la simulación.
 */
@Entity
@Table(name = "puntos_ruta")
@Getter
@Setter
@NoArgsConstructor
public class Waypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mision_id")
    private Mission mission;

    @Column(name = "orden", nullable = false)
    private Integer sequence;

    @Column(name = "latitud", nullable = false)
    private Double latitude;

    @Column(name = "longitud", nullable = false)
    private Double longitude;

    @Column(name = "altitud_m", nullable = false)
    private Integer altitudeM;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 20)
    private WaypointAction action;
}
