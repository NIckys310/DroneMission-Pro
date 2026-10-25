package com.dronemission.incidents;

import com.dronemission.missions.Mission;
import com.dronemission.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Incidente ocurrido durante una misión. reportedBy es null cuando lo registra el sistema. */
@Entity
@Table(name = "incidentes")
@Getter
@Setter
@NoArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mision_id")
    private Mission mission;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private IncidentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "severidad", nullable = false, length = 10)
    private IncidentSeverity severity;

    @Column(name = "descripcion", nullable = false, length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por")
    private User reportedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private IncidentStatus status = IncidentStatus.ABIERTO;

    @Column(name = "resolucion", length = 1000)
    private String resolution;
}
