package com.dronemission.clients.dto;

import com.dronemission.clients.ClientType;
import jakarta.validation.constraints.*;

public record ClientRequest(
        @NotNull(message = "El tipo de cliente es obligatorio") ClientType type,
        @NotBlank(message = "El nombre o razón social es obligatorio") @Size(max = 150) String name,
        @NotBlank(message = "El documento/NIT es obligatorio") @Size(max = 30) String document,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") String email,
        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^[0-9+()\\s-]{7,30}$", message = "El teléfono no es válido") String phone,
        @NotBlank(message = "La dirección es obligatoria") @Size(max = 200) String address,
        @NotBlank(message = "La ciudad es obligatoria") @Size(max = 80) String city,
        Boolean active
) {
}
