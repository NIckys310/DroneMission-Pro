package com.dronemission.audit;

import com.dronemission.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Registro de auditoría: quién hizo qué, sobre qué entidad y cuándo. */
@Entity
@Table(name = "auditoria")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User user;

    @Column(name = "correo_usuario", nullable = false, length = 120)
    private String userEmail;

    @Column(name = "accion", nullable = false, length = 40)
    private String action;

    @Column(name = "entidad", nullable = false, length = 40)
    private String entity;

    @Column(name = "entidad_id")
    private Long entityId;

    @Column(name = "descripcion", nullable = false, length = 1000)
    private String description;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime date = LocalDateTime.now();
}
