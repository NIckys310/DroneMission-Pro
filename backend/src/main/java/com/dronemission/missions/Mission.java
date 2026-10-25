package com.dronemission.missions;

import com.dronemission.clients.Client;
import com.dronemission.common.exception.InvalidStateTransitionException;
import com.dronemission.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Misión de vuelo: entidad central del sistema. Se construye con MissionBuilder. */
@Entity
@Table(name = "misiones")
@Getter
@Setter
@NoArgsConstructor
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "descripcion", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private MissionType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Client client;

    @Column(name = "ubicacion", nullable = false, length = 200)
    private String location;

    @Column(name = "latitud", nullable = false)
    private Double latitude;

    @Column(name = "longitud", nullable = false)
    private Double longitude;

    @Column(name = "altitud_objetivo_m", nullable = false)
    private Integer targetAltitudeM;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime startTime;

    @Column(name = "hora_fin_estimada", nullable = false)
    private LocalTime estimatedEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 10)
    private MissionPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private MissionStatus status = MissionStatus.BORRADOR;

    @Column(name = "objetivo", nullable = false, length = 500)
    private String objective;

    @Column(name = "observaciones", length = 1000)
    private String observations;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creado_por")
    private User createdBy;

    @Column(name = "inicio_real")
    private LocalDateTime actualStart;

    @Column(name = "fin_real")
    private LocalDateTime actualEnd;

    @OneToMany(mappedBy = "mission")
    @OrderBy("assignedAt ASC")
    private List<MissionAssignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<Waypoint> flightPlan = new ArrayList<>();

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** Cambia el estado validando la máquina de estados. */
    public void changeStatus(MissionStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStateTransitionException(
                    "No se puede pasar la misión " + code + " de " + status + " a " + target);
        }
        this.status = target;
    }

    /** Asignación vigente (drone + piloto), si existe. */
    public Optional<MissionAssignment> getActiveAssignment() {
        return assignments.stream().filter(MissionAssignment::isActive).reduce((first, second) -> second);
    }

    public void replaceFlightPlan(List<Waypoint> waypoints) {
        flightPlan.clear();
        waypoints.forEach(w -> {
            w.setMission(this);
            flightPlan.add(w);
        });
    }

    public void appendObservation(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        observations = (observations == null || observations.isBlank()) ? text.trim() : observations + "\n" + text.trim();
        if (observations.length() > 1000) {
            observations = observations.substring(observations.length() - 1000);
        }
    }
}
