package com.dronemission.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos del registro público. La cuenta queda pendiente de aprobación. */
public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String firstName,
        @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 80) String lastName,
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._-]{3,30}$", message = "El usuario debe tener 3 a 30 caracteres: letras, números, punto, guion o guion bajo")
        String username,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") @Size(max = 120) String email,
        @NotBlank(message = "La contraseña es obligatoria") @Size(max = 64) String password,
        @NotBlank(message = "Confirma la contraseña") String confirmPassword
) {
}
