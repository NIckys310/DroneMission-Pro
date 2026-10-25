package com.dronemission.auth.dto;

/** Configuración pública que el login necesita (sin datos sensibles). */
public record AuthConfigResponse(boolean demoUsersEnabled, boolean registrationEnabled, boolean showResetToken,
                                 String passwordPolicy) {
}
