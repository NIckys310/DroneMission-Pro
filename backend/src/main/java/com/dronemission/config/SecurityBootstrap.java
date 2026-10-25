package com.dronemission.config;

import com.dronemission.common.PasswordPolicy;
import com.dronemission.users.RoleName;
import com.dronemission.users.RoleRepository;
import com.dronemission.users.User;
import com.dronemission.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Tareas de seguridad al arrancar:
 *  1. Crea el administrador propietario a partir de variables de entorno
 *     (ADMIN_EMAIL, ADMIN_USERNAME, ADMIN_FIRST_NAME, ADMIN_LAST_NAME, ADMIN_PASSWORD).
 *     La contraseña nunca se guarda en el código ni en el repositorio.
 *  2. Si APP_DEMO_USERS=false, desactiva los usuarios demo (@dronemission.local),
 *     cuya contraseña es pública.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityBootstrap {

    private static final String DEMO_DOMAIN = "@dronemission.local";

    private final SecurityProperties properties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void run() {
        createOwnerAdmin();
        toggleDemoUsers();
    }

    private void createOwnerAdmin() {
        SecurityProperties.Admin admin = properties.admin();
        if (admin == null || !admin.isConfigured()) {
            return;
        }
        String email = admin.email().trim().toLowerCase();
        String username = admin.username() == null || admin.username().isBlank()
                ? email.split("@")[0] : admin.username().trim().toLowerCase();
        List<String> problems = PasswordPolicy.problems(admin.password());
        if (!problems.isEmpty()) {
            log.error("ADMIN_PASSWORD no es segura (requiere: {}). No se creó el administrador.", String.join(", ", problems));
            return;
        }
        userRepository.findByEmailIgnoreCase(email).ifPresentOrElse(existing -> {
            if (admin.resetPassword()) {
                existing.setPasswordHash(passwordEncoder.encode(admin.password()));
                existing.setActive(true);
                existing.setPendingApproval(false);
                existing.setFailedAttempts(0);
                existing.setLockedUntil(null);
                existing.setRole(roleRepository.findByName(RoleName.ADMINISTRADOR).orElseThrow());
                log.info("Contraseña del administrador {} restablecida desde las variables de entorno", email);
            }
        }, () -> {
            if (userRepository.existsByUsernameIgnoreCase(username)) {
                log.error("El usuario {} ya existe; define otro ADMIN_USERNAME", username);
                return;
            }
            User user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setFirstName(blankTo(admin.firstName(), "Administrador"));
            user.setLastName(blankTo(admin.lastName(), "Principal"));
            user.setPasswordHash(passwordEncoder.encode(admin.password()));
            user.setRole(roleRepository.findByName(RoleName.ADMINISTRADOR).orElseThrow());
            user.setActive(true);
            userRepository.save(user);
            log.info("Administrador propietario {} ({}) creado", username, email);
        });
    }

    private void toggleDemoUsers() {
        if (properties.demoUsers()) {
            return;
        }
        List<User> demo = userRepository.findByEmailEndingWithIgnoreCase(DEMO_DOMAIN);
        demo.stream().filter(User::isActive).forEach(u -> u.setActive(false));
        log.info("Usuarios demo desactivados ({}): la contraseña demo es pública", demo.size());
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
