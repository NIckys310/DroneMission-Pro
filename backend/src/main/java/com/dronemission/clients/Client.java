package com.dronemission.clients;

import com.dronemission.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Cliente que contrata misiones. Puede tener un usuario para consultar sus misiones. */
@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private ClientType type;

    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "documento", nullable = false, unique = true, length = 30)
    private String document;

    @Column(name = "correo", nullable = false, length = 120)
    private String email;

    @Column(name = "telefono", nullable = false, length = 30)
    private String phone;

    @Column(name = "direccion", nullable = false, length = 200)
    private String address;

    @Column(name = "ciudad", nullable = false, length = 80)
    private String city;

    @Column(name = "activo", nullable = false)
    private boolean active = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User user;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
