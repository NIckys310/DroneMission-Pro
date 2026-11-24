package com.dronemission.pilots;

import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Certificación obtenida por un piloto (relación 1:N con Pilot).
 *
 * MongoDB: se guarda EMBEBIDA dentro del documento del piloto (campo "certificaciones"),
 * porque la certificación pertenece solo a ese piloto (composición).
 */
@Getter
@Setter
@NoArgsConstructor
public class PilotCertification {

    // Número de la certificación dentro del piloto (1, 2, 3...). Lo asigna Pilot.replaceCertifications()
    private Long id;

    @Field("nombre")
    private String name;

    @Field("entidad_emisora")
    private String issuer;

    @Field("fecha_emision")
    private LocalDate issuedOn;

    @Field("fecha_vencimiento")
    private LocalDate expiresOn;
}
