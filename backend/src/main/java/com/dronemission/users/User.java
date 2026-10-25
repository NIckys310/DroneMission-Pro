package com.dronemission.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Usuario que inicia sesión en el sistema. Tiene exactamente un rol. */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 80)
    private String firstName;

    @Column(name = "apellidos", nullable = false, length = 80)
    private String lastName;

    @Column(name = "correo", nullable = false, unique = true, length = 120)
    private String email;

    // Nunca se guarda la contraseña: solo su hash BCrypt
    @Column(name = "contrasena_hash", nullable = false, length = 100)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rol_id")
    private Role role;

    @Column(name = "activo", nullable = false)
    private boolean active = true;

    @Column(name = "token_recuperacion", length = 64)
    private String resetToken;

    @Column(name = "token_expira_en")
    private LocalDateTime resetTokenExpiresAt;

    @Column(name = "ultimo_acceso")
    private LocalDateTime lastLoginAt;

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

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean hasRole(RoleName roleName) {
        return role != null && role.getName() == roleName;
    }
}
