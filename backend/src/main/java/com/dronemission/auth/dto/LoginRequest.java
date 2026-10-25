package com.dronemission.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Se puede iniciar sesión con el correo o con el nombre de usuario. */
public record LoginRequest(
        @JsonAlias("email")
        @NotBlank(message = "El correo o usuario es obligatorio") @Size(max = 120) String identifier,
        @NotBlank(message = "La contraseña es obligatoria") @Size(max = 64) String password
) {
}
