package com.dronemission.pilots;

import com.dronemission.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Piloto certificado que opera los drones. */
@Entity
@Table(name = "pilotos")
@Getter
@Setter
@NoArgsConstructor
public class Pilot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 80)
    private String firstName;

    @Column(name = "apellidos", nullable = false, length = 80)
    private String lastName;

    @Column(name = "documento", nullable = false, unique = true, length = 30)
    private String document;

    @Column(name = "correo", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "telefono", nullable = false, length = 30)
    private String phone;

    @Column(name = "numero_licencia", nullable = false, unique = true, length = 40)
    private String licenseNumber;

    @Column(name = "vencimiento_licencia", nullable = false)
    private LocalDate licenseExpiresOn;

    @Column(name = "horas_vuelo", nullable = false, precision = 10, scale = 2)
    private BigDecimal flightHours = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private PilotStatus status = PilotStatus.DISPONIBLE;

    @Column(name = "activo", nullable = false)
    private boolean active = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User user;

    @OneToMany(mappedBy = "pilot", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("issuedOn DESC")
    private List<PilotCertification> certifications = new ArrayList<>();

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** La licencia es válida si no ha vencido en la fecha indicada. */
    public boolean hasValidLicenseOn(LocalDate date) {
        return licenseExpiresOn != null && !licenseExpiresOn.isBefore(date);
    }

    public void addFlightHours(BigDecimal hours) {
        flightHours = flightHours.add(hours);
    }

    /** Recalcula el estado según la licencia, sin tocar a un piloto que está volando. */
    public void refreshLicenseStatus(LocalDate today) {
        if (status == PilotStatus.EN_MISION) {
            return;
        }
        if (!hasValidLicenseOn(today)) {
            status = PilotStatus.LICENCIA_VENCIDA;
        } else if (status == PilotStatus.LICENCIA_VENCIDA) {
            status = PilotStatus.DISPONIBLE;
        }
    }

    public void replaceCertifications(List<PilotCertification> newCertifications) {
        certifications.clear();
        newCertifications.forEach(c -> {
            c.setPilot(this);
            certifications.add(c);
        });
    }
}
