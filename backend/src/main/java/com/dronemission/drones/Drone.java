package com.dronemission.drones;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Drone de la flota. Su estado lo cambian las misiones y los mantenimientos. */
@Entity
@Table(name = "drones")
@Getter
@Setter
@NoArgsConstructor
public class Drone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "nombre", nullable = false, length = 100)
    private String name;

    @Column(name = "fabricante", nullable = false, length = 80)
    private String manufacturer;

    @Column(name = "modelo", nullable = false, length = 80)
    private String model;

    @Column(name = "numero_serie", nullable = false, unique = true, length = 60)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private DroneType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private DroneStatus status = DroneStatus.DISPONIBLE;

    @Column(name = "capacidad_bateria_mah", nullable = false)
    private Integer batteryCapacityMah;

    /** Porcentaje actual de batería (0-100). */
    @Column(name = "bateria_actual", nullable = false)
    private Integer batteryLevel = 100;

    @Column(name = "horas_vuelo", nullable = false, precision = 10, scale = 2)
    private BigDecimal flightHours = BigDecimal.ZERO;

    @Column(name = "velocidad_maxima_kmh", nullable = false)
    private Integer maxSpeedKmh;

    @Column(name = "autonomia_minutos", nullable = false)
    private Integer autonomyMinutes;

    @Column(name = "fecha_adquisicion", nullable = false)
    private LocalDate acquiredOn;

    @Column(name = "ultimo_mantenimiento")
    private LocalDate lastMaintenanceOn;

    @Column(name = "proximo_mantenimiento")
    private LocalDate nextMaintenanceOn;

    @Column(name = "descripcion", length = 1000)
    private String description;

    @Column(name = "activo", nullable = false)
    private boolean active = true;

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

    /** Un drone se puede asignar si no está en mantenimiento ni fuera de servicio (reglas 1 y 2). */
    public boolean isAssignable() {
        return active && status != DroneStatus.EN_MANTENIMIENTO && status != DroneStatus.FUERA_DE_SERVICIO;
    }

    public boolean hasBatteryAtLeast(int percentage) {
        return batteryLevel != null && batteryLevel >= percentage;
    }

    public void addFlightHours(BigDecimal hours) {
        flightHours = flightHours.add(hours);
    }
}
