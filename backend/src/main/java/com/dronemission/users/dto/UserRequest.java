package com.dronemission.users.dto;

import com.dronemission.users.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o editar un usuario.
 * La contraseña es obligatoria al crear y opcional al editar.
 * pilotId / clientId vinculan el usuario con su ficha de piloto o cliente.
 */
public record UserRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String firstName,
        @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 80) String lastName,
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._-]{3,30}$", message = "El usuario debe tener 3 a 30 caracteres: letras, números, punto, guion o guion bajo")
        String username,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") String email,
        @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres") String password,
        @NotNull(message = "El rol es obligatorio") RoleName role,
        Boolean active,
        Long pilotId,
        Long clientId
) {
}
