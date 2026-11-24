package com.dronemission.missions;

import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Punto del plan de vuelo programado con anticipación.
 * El drone virtual recorre los puntos en orden durante la simulación.
 *
 * MongoDB: NO tiene colección propia. Se guarda EMBEBIDO dentro del documento de la misión
 * (campo "plan_vuelo"), porque un punto de ruta no existe sin su misión (composición).
 */
@Getter
@Setter
@NoArgsConstructor
public class Waypoint {

    @Field("orden")
    private Integer sequence;

    @Field("latitud")
    private Double latitude;

    @Field("longitud")
    private Double longitude;

    @Field("altitud_m")
    private Integer altitudeM;
    @Field("accion")
    private WaypointAction action;
}
