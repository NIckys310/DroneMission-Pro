package com.dronemission.missions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelRequest(@NotBlank(message = "El motivo de cancelación es obligatorio") @Size(max = 500) String reason) {
}
