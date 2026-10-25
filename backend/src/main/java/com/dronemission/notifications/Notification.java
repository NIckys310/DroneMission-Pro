package com.dronemission.notifications;

import com.dronemission.missions.Mission;
import com.dronemission.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Notificación dentro de la aplicación (campanita del frontend). */
@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private User user;

    @Column(name = "titulo", nullable = false, length = 150)
    private String title;

    @Column(name = "mensaje", nullable = false, length = 500)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mision_id")
    private Mission mission;

    @Column(name = "leida", nullable = false)
    private boolean read = false;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime date = LocalDateTime.now();
}
