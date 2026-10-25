package com.dronemission.auth.dto;

import java.util.List;

/** Datos del usuario autenticado que necesita el frontend (menú, permisos, perfil). */
public record UserInfo(Long id, String fullName, String email, String role, List<String> permissions,
                       Long pilotId, Long clientId) {
}
