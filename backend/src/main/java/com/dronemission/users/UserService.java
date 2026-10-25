package com.dronemission.users;

import com.dronemission.audit.AuditService;
import com.dronemission.auth.CurrentUserService;
import com.dronemission.clients.Client;
import com.dronemission.clients.ClientRepository;
import com.dronemission.common.exception.BusinessRuleException;
import com.dronemission.common.exception.DuplicateResourceException;
import com.dronemission.common.exception.ResourceNotFoundException;
import com.dronemission.pilots.Pilot;
import com.dronemission.pilots.PilotRepository;
import com.dronemission.users.dto.UserRequest;
import com.dronemission.users.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Gestión de usuarios (solo administrador). */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PilotRepository pilotRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAllByOrderByIdAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(get(id));
    }

    @Transactional
    public UserResponse create(UserRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Ya existe un usuario con el correo " + email);
        }
        if (req.password() == null || req.password().isBlank()) {
            throw new BusinessRuleException("La contraseña es obligatoria para un usuario nuevo");
        }
        User user = new User();
        user.setFirstName(req.firstName().trim());
        user.setLastName(req.lastName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(findRole(req.role()));
        user.setActive(req.active() == null || req.active());
        userRepository.save(user);
        linkProfiles(user, req);
        auditService.log("CREAR", "USUARIO", user.getId(), "Usuario " + email + " creado con rol " + req.role());
        return toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest req) {
        User user = get(id);
        String email = req.email().trim().toLowerCase();
        userRepository.findByEmailIgnoreCase(email)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new DuplicateResourceException("Ya existe un usuario con el correo " + email);
                });
        boolean isSelf = id.equals(currentUserService.get().id());
        if (isSelf && (req.role() != user.getRole().getName() || Boolean.FALSE.equals(req.active()))) {
            throw new BusinessRuleException("No puedes cambiar tu propio rol ni desactivarte");
        }
        user.setFirstName(req.firstName().trim());
        user.setLastName(req.lastName().trim());
        user.setEmail(email);
        user.setRole(findRole(req.role()));
        if (req.active() != null) {
            user.setActive(req.active());
        }
        if (req.password() != null && !req.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        linkProfiles(user, req);
        auditService.log("ACTUALIZAR", "USUARIO", id, "Usuario " + email + " actualizado");
        return toResponse(user);
    }

    /** Los usuarios no se borran (para conservar la auditoría): se desactivan. */
    @Transactional
    public void deactivate(Long id) {
        if (id.equals(currentUserService.get().id())) {
            throw new BusinessRuleException("No puedes desactivar tu propio usuario");
        }
        User user = get(id);
        user.setActive(false);
        auditService.log("DESACTIVAR", "USUARIO", id, "Usuario " + user.getEmail() + " desactivado");
    }

    /** Vincula el usuario con su ficha de piloto o de cliente según el rol. */
    private void linkProfiles(User user, UserRequest req) {
        if (req.role() == RoleName.PILOTO && req.pilotId() != null) {
            Pilot pilot = pilotRepository.findById(req.pilotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Piloto", req.pilotId()));
            if (pilot.getUser() != null && !pilot.getUser().getId().equals(user.getId())) {
                throw new BusinessRuleException("El piloto ya está vinculado a otro usuario");
            }
            pilot.setUser(user);
        }
        if (req.role() == RoleName.CLIENTE && req.clientId() != null) {
            Client client = clientRepository.findById(req.clientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", req.clientId()));
            if (client.getUser() != null && !client.getUser().getId().equals(user.getId())) {
                throw new BusinessRuleException("El cliente ya está vinculado a otro usuario");
            }
            client.setUser(user);
        }
    }

    private Role findRole(RoleName name) {
        return roleRepository.findByName(name).orElseThrow(() -> new ResourceNotFoundException("Rol " + name + " no existe"));
    }

    private User get(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    private UserResponse toResponse(User u) {
        Long pilotId = pilotRepository.findByUserId(u.getId()).map(Pilot::getId).orElse(null);
        Long clientId = clientRepository.findByUserId(u.getId()).map(Client::getId).orElse(null);
        return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getFullName(), u.getEmail(),
                u.getRole().getName().name(), u.isActive(), pilotId, clientId, u.getLastLoginAt(), u.getCreatedAt());
    }
}
