package com.dronemission.auth;

import com.dronemission.audit.AuditService;
import com.dronemission.auth.dto.*;
import com.dronemission.clients.Client;
import com.dronemission.clients.ClientRepository;
import com.dronemission.common.PasswordPolicy;
import com.dronemission.common.exception.AuthenticationFailedException;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.common.exception.DuplicateResourceException;
import com.dronemission.config.SecurityProperties;
import com.dronemission.notifications.NotificationService;
import com.dronemission.pilots.Pilot;
import com.dronemission.pilots.PilotRepository;
import com.dronemission.users.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Casos de uso de autenticación: login, registro, perfil, logout y recuperación de contraseña. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Usuario o contraseña incorrectos";
    private static final int RESET_TOKEN_MINUTES = 30;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PilotRepository pilotRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final SecurityProperties securityProperties;

    /**
     * Login con correo o nombre de usuario.
     * Protección contra fuerza bruta: tras N intentos fallidos la cuenta se bloquea unos minutos.
     */
    // noRollbackFor: el intento fallido y el contador deben guardarse aunque se lance la excepción
    @Transactional(noRollbackFor = AuthenticationFailedException.class)
    public LoginResponse login(LoginRequest request) {
        String identifier = request.identifier().trim();
        Optional<User> found = identifier.contains("@")
                ? userRepository.findByEmailIgnoreCase(identifier)
                : userRepository.findByUsernameIgnoreCase(identifier);
        LocalDateTime now = LocalDateTime.now();

        if (found.isPresent() && found.get().isLocked(now)) {
            long minutes = Math.max(1, Duration.between(now, found.get().getLockedUntil()).toMinutes() + 1);
            throw new AuthenticationFailedException("Cuenta bloqueada temporalmente por varios intentos fallidos. Intenta de nuevo en "
                    + minutes + " minuto(s).");
        }
        // Mismo mensaje para "usuario inexistente" y "contraseña incorrecta": no revela qué cuentas existen
        if (found.isEmpty() || !passwordEncoder.matches(request.password(), found.get().getPasswordHash())) {
            found.ifPresent(user -> registerFailedAttempt(user, now));
            auditService.logAs(found.orElse(null), identifier, "LOGIN_FALLIDO", "USUARIO",
                    found.map(User::getId).orElse(null), "Intento de inicio de sesión fallido");
            throw new AuthenticationFailedException(INVALID_CREDENTIALS);
        }
        User user = found.get();
        if (user.isPendingApproval()) {
            throw new AuthenticationFailedException("Tu cuenta está pendiente de aprobación por un administrador.");
        }
        if (!user.isActive()) {
            throw new AuthenticationFailedException("El usuario está inactivo. Contacta al administrador.");
        }

        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        auditService.logAs(user, user.getEmail(), "LOGIN", "USUARIO", user.getId(), "Inicio de sesión exitoso");
        return new LoginResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationMinutes(), toUserInfo(user));
    }

    /**
     * Registro público. La cuenta se crea INACTIVA y pendiente de aprobación:
     * nadie obtiene permisos hasta que un administrador la apruebe y le asigne un rol.
     */
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (!securityProperties.registrationEnabled()) {
            throw new BusinessRuleException("El registro de nuevas cuentas está deshabilitado");
        }
        String email = request.email().trim().toLowerCase();
        String username = request.username().trim().toLowerCase();
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessRuleException("Las contraseñas no coinciden");
        }
        PasswordPolicy.validate(request.password(), username, request.firstName(), email.split("@")[0]);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Ya existe una cuenta con ese correo");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("El nombre de usuario ya está en uso");
        }

        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        // Rol provisional de mínimo privilegio; la cuenta no puede entrar hasta ser aprobada
        user.setRole(roleRepository.findByName(RoleName.CLIENTE).orElseThrow());
        user.setActive(false);
        user.setPendingApproval(true);
        userRepository.save(user);

        auditService.logAs(user, email, "REGISTRO", "USUARIO", user.getId(), "Nueva cuenta registrada (pendiente de aprobación)");
        List<User> admins = new ArrayList<>(userRepository.findByRoleNameAndActiveTrue(RoleName.ADMINISTRADOR));
        notificationService.notify(admins, "Nueva cuenta por aprobar",
                user.getFullName() + " (" + username + ") se registró y espera aprobación en Usuarios.", null);
        return new MessageResponse("Cuenta creada. Un administrador debe aprobarla y asignarte un rol antes de que puedas iniciar sesión.");
    }

    @Transactional(readOnly = true)
    public UserInfo me() {
        return toUserInfo(currentUserService.getEntity());
    }

    public AuthConfigResponse config() {
        return new AuthConfigResponse(securityProperties.demoUsers(), securityProperties.registrationEnabled(),
                securityProperties.showResetToken(), PasswordPolicy.DESCRIPTION);
    }

    @Transactional
    public void logout() {
        auditService.log("LOGOUT", "USUARIO", currentUserService.get().id(), "Cierre de sesión");
    }

    /**
     * Recuperación de contraseña SIMULADA: genera un token temporal. Solo se muestra en la
     * respuesta si app.security.show-reset-token=true (desarrollo); en la nube está oculto.
     */
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String message = "Si el correo está registrado, recibirás instrucciones para restablecer la contraseña.";
        if (!securityProperties.showResetToken()) {
            return new ForgotPasswordResponse(message + " Si no te llega, pide al administrador que la restablezca.", null);
        }
        return userRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(User::isActive)
                .map(user -> {
                    String token = UUID.randomUUID().toString().replace("-", "");
                    user.setResetToken(token);
                    user.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_MINUTES));
                    auditService.logAs(user, user.getEmail(), "RECUPERAR_CONTRASENA", "USUARIO", user.getId(),
                            "Solicitud de recuperación de contraseña (simulada)");
                    return new ForgotPasswordResponse(message + " (Simulación: usa el token mostrado)", token);
                })
                .orElse(new ForgotPasswordResponse(message, null));
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.token())
                .filter(u -> u.getResetTokenExpiresAt() != null && u.getResetTokenExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new BusinessRuleException("El token de recuperación no es válido o ya expiró"));
        PasswordPolicy.validate(request.newPassword(), user.getUsername(), user.getFirstName());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        auditService.logAs(user, user.getEmail(), "CAMBIAR_CONTRASENA", "USUARIO", user.getId(), "Contraseña restablecida");
        return new MessageResponse("Contraseña actualizada. Ya puedes iniciar sesión.");
    }

    private void registerFailedAttempt(User user, LocalDateTime now) {
        int attempts = user.getFailedAttempts() + 1;
        if (attempts >= securityProperties.maxFailedAttempts()) {
            user.setLockedUntil(now.plusMinutes(securityProperties.lockMinutes()));
            user.setFailedAttempts(0);
            auditService.logAs(user, user.getEmail(), "CUENTA_BLOQUEADA", "USUARIO", user.getId(),
                    "Cuenta bloqueada " + securityProperties.lockMinutes() + " min por intentos fallidos");
        } else {
            user.setFailedAttempts(attempts);
        }
    }

    private UserInfo toUserInfo(User user) {
        Long pilotId = pilotRepository.findByUserId(user.getId()).map(Pilot::getId).orElse(null);
        Long clientId = clientRepository.findByUserId(user.getId()).map(Client::getId).orElse(null);
        return new UserInfo(user.getId(), user.getFullName(), user.getEmail(), user.getRole().getName().name(),
                user.getRole().getPermissions().stream().map(Permission::getCode).sorted().toList(),
                pilotId, clientId);
    }
}
