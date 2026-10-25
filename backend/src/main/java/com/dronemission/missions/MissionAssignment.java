package com.dronemission.missions;

import com.dronemission.drones.Drone;
import com.dronemission.pilots.Pilot;
import com.dronemission.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Asignación de un drone y un piloto a una misión.
 * Se guarda el historial: al reasignar, la asignación anterior queda inactiva.
 */
@Entity
@Table(name = "asignaciones_mision")
@Getter
@Setter
@NoArgsConstructor
public class MissionAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mision_id")
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drone_id")
    private Drone drone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "piloto_id")
    private Pilot pilot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asignado_por")
    private User assignedBy;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "activa", nullable = false)
    private boolean active = true;
}
