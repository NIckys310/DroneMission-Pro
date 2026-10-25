package com.dronemission.pilots.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CertificationDto(
        Long id,
        @NotBlank(message = "El nombre de la certificación es obligatorio") String name,
        @NotBlank(message = "La entidad emisora es obligatoria") String issuer,
        @NotNull(message = "La fecha de emisión es obligatoria") LocalDate issuedOn,
        LocalDate expiresOn
) {
}
