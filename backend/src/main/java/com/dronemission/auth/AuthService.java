package com.dronemission.auth;

import com.dronemission.audit.AuditService;
import com.dronemission.auth.dto.*;
import com.dronemission.clients.Client;
import com.dronemission.clients.ClientRepository;
import com.dronemission.common.exception.AuthenticationFailedException;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.pilots.Pilot;
import com.dronemission.pilots.PilotRepository;
import com.dronemission.users.Permission;
import com.dronemission.users.User;
import com.dronemission.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/** Casos de uso de autenticación: login, perfil, logout y recuperación de contraseña. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Correo o contraseña incorrectos";
    private static final int RESET_TOKEN_MINUTES = 30;

    private final UserRepository userRepository;
    private final PilotRepository pilotRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    // noRollbackFor: el intento fallido debe quedar auditado aunque se lance la excepción
    @Transactional(noRollbackFor = AuthenticationFailedException.class)
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        Optional<User> found = userRepository.findByEmailIgnoreCase(email);

        // Mismo mensaje para "usuario inexistente" y "contraseña incorrecta": no revela qué correos existen
        if (found.isEmpty() || !passwordEncoder.matches(request.password(), found.get().getPasswordHash())) {
            auditService.logAs(found.orElse(null), email, "LOGIN_FALLIDO", "USUARIO",
                    found.map(User::getId).orElse(null), "Intento de inicio de sesión fallido");
            throw new AuthenticationFailedException(INVALID_CREDENTIALS);
        }
        User user = found.get();
        if (!user.isActive()) {
            throw new AuthenticationFailedException("El usuario está inactivo. Contacta al administrador.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        auditService.logAs(user, user.getEmail(), "LOGIN", "USUARIO", user.getId(), "Inicio de sesión exitoso");
        return new LoginResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationMinutes(), toUserInfo(user));
    }

    @Transactional(readOnly = true)
    public UserInfo me() {
        return toUserInfo(currentUserService.getEntity());
    }

    @Transactional
    public void logout() {
        auditService.log("LOGOUT", "USUARIO", currentUserService.get().id(), "Cierre de sesión");
    }

    /**
     * Recuperación de contraseña SIMULADA: genera un token temporal.
     * En producción se enviaría por correo; en la demo se devuelve en la respuesta.
     */
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String message = "Si el correo está registrado, recibirás instrucciones para restablecer la contraseña.";
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
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        auditService.logAs(user, user.getEmail(), "CAMBIAR_CONTRASENA", "USUARIO", user.getId(), "Contraseña restablecida");
        return new MessageResponse("Contraseña actualizada. Ya puedes iniciar sesión.");
    }

    private UserInfo toUserInfo(User user) {
        Long pilotId = pilotRepository.findByUserId(user.getId()).map(Pilot::getId).orElse(null);
        Long clientId = clientRepository.findByUserId(user.getId()).map(Client::getId).orElse(null);
        return new UserInfo(user.getId(), user.getFullName(), user.getEmail(), user.getRole().getName().name(),
                user.getRole().getPermissions().stream().map(Permission::getCode).sorted().toList(),
                pilotId, clientId);
    }
}
