package com.dronemission.missions.dto;

import jakarta.validation.constraints.Size;

/** Observación opcional (al finalizar) u obligatoria (al registrar observaciones). */
public record ObservationRequest(@Size(max = 500) String text) {
}
