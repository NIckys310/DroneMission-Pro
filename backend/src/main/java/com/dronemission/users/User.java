package com.dronemission.users;

import com.dronemission.common.persistence.BeforeCreate;
import com.dronemission.common.persistence.BeforeUpdate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Usuario que inicia sesión en el sistema. Tiene exactamente un rol. */
// MongoDB: cada objeto se guarda como un documento en la colección "usuarios"
@Document(collection = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class User {

    // _id numérico: lo asigna SequenceGeneratorService (como el autoincremento de SQL)
    @Id
    private Long id;

    @Field("nombre")
    private String firstName;

    @Field("apellidos")
    private String lastName;

    @Indexed(unique = true)
    @Field("correo")
    private String email;

    /** Nombre de usuario para iniciar sesión (alternativa al correo). */
    @Indexed(unique = true)
    @Field("nombre_usuario")
    private String username;

    // Nunca se guarda la contraseña: solo su hash BCrypt
    @Field("contrasena_hash")
    private String passwordHash;

    // Referencia a otro documento (DBRef): en Mongo se guarda {$ref, $id}
    @DBRef
    @Field("rol_id")
    private Role role;

    @Field("activo")
    private boolean active = true;

    /** Cuenta creada por registro público que espera la aprobación de un administrador. */
    @Field("pendiente_aprobacion")
    private boolean pendingApproval = false;

    /** Intentos de login fallidos consecutivos (protección contra fuerza bruta). */
    @Field("intentos_fallidos")
    private int failedAttempts = 0;

    @Field("bloqueado_hasta")
    private LocalDateTime lockedUntil;

    @Field("token_recuperacion")
    private String resetToken;

    @Field("token_expira_en")
    private LocalDateTime resetTokenExpiresAt;

    @Field("ultimo_acceso")
    private LocalDateTime lastLoginAt;

    @Field("creado_en")
    private LocalDateTime createdAt;

    @Field("actualizado_en")
    private LocalDateTime updatedAt;

    // Se ejecuta antes de guardar un documento nuevo (lo llama MongoLifecycleListener)
    @BeforeCreate
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    // Se ejecuta antes de actualizar un documento existente (lo llama MongoLifecycleListener)
    @BeforeUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isLocked(LocalDateTime now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public boolean hasRole(RoleName roleName) {
        return role != null && role.getName() == roleName;
    }
}
