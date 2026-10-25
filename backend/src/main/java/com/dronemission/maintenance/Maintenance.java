package com.dronemission.maintenance;

import com.dronemission.drones.Drone;
import com.dronemission.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mantenimiento de un drone realizado por un técnico. */
@Entity
@Table(name = "mantenimientos")
@Getter
@Setter
@NoArgsConstructor
public class Maintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drone_id")
    private Drone drone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tecnico_id")
    private User technician;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private MaintenanceType type;

    @Column(name = "fecha", nullable = false)
    private LocalDate date;

    @Column(name = "descripcion", nullable = false, length = 1000)
    private String description;

    @Column(name = "costo", nullable = false, precision = 12, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private MaintenanceStatus status = MaintenanceStatus.PROGRAMADO;

    @Column(name = "proxima_fecha")
    private LocalDate nextDate;

    @Column(name = "observaciones", length = 1000)
    private String observations;

    @Column(name = "iniciado_en")
    private LocalDateTime startedAt;

    @Column(name = "completado_en")
    private LocalDateTime completedAt;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
