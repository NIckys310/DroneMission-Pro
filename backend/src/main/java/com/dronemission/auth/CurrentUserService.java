package com.dronemission.auth;

import com.dronemission.common.exception.ResourceNotFoundException;
import com.dronemission.users.User;
import com.dronemission.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Acceso cómodo al usuario que hace la petición actual. */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public Optional<AuthenticatedUser> find() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public AuthenticatedUser get() {
        return find().orElseThrow(() -> new IllegalStateException("No hay un usuario autenticado"));
    }

    public User getEntity() {
        Long id = get().id();
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }
}
