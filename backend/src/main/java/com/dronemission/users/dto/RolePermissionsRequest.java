package com.dronemission.users.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RolePermissionsRequest(@NotNull(message = "La lista de permisos es obligatoria") List<String> permissions) {
}
