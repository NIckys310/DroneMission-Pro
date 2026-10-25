package com.dronemission.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Permiso atómico del sistema, por ejemplo MISIONES_GESTIONAR. */
@Entity
@Table(name = "permisos")
@Getter
@Setter
@NoArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "descripcion", nullable = false)
    private String description;
}
