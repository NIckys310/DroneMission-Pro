package com.dronemission.evidence.dto;

import com.dronemission.evidence.EvidenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Evidencia registrada con una URL (sin subir archivo). */
public record EvidenceRequest(
        @NotNull(message = "El tipo de evidencia es obligatorio") EvidenceType type,
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 150) String name,
        @Size(max = 500) String url,
        @Size(max = 1000) String description,
        Boolean visibleToClient
) {
}
