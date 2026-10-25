package com.dronemission.pilots;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Certificación obtenida por un piloto (relación 1:N con Pilot). */
@Entity
@Table(name = "certificaciones_piloto")
@Getter
@Setter
@NoArgsConstructor
public class PilotCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "piloto_id")
    private Pilot pilot;

    @Column(name = "nombre", nullable = false, length = 120)
    private String name;

    @Column(name = "entidad_emisora", nullable = false, length = 120)
    private String issuer;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate issuedOn;

    @Column(name = "fecha_vencimiento")
    private LocalDate expiresOn;
}
