package com.dronemission.auth;

import com.dronemission.users.RoleName;

import java.util.Set;

/**
 * Usuario autenticado de la petición actual (se guarda en el SecurityContext).
 * Es inmutable y no depende de JPA.
 */
public record AuthenticatedUser(Long id, String email, String fullName, RoleName role, Set<String> permissions) {

    public boolean hasRole(RoleName roleName) {
        return role == roleName;
    }

    public boolean isAdmin() {
        return role == RoleName.ADMINISTRADOR;
    }
}
