package com.dronemission.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Opciones de seguridad configurables por variables de entorno (ver application.yml).
 * En la nube: usuarios demo desactivados, token de recuperación oculto y
 * administrador creado a partir de variables secretas.
 */
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        boolean demoUsers,
        boolean registrationEnabled,
        boolean showResetToken,
        int maxFailedAttempts,
        int lockMinutes,
        int authRequestsPerMinute,
        Admin admin
) {
    public record Admin(String email, String username, String firstName, String lastName, String password,
                        boolean resetPassword) {
        public boolean isConfigured() {
            return email != null && !email.isBlank() && password != null && !password.isBlank();
        }
    }
}
