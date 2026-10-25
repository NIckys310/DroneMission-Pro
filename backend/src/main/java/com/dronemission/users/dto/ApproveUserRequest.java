package com.dronemission.users.dto;

import com.dronemission.users.RoleName;
import jakarta.validation.constraints.NotNull;

/** El administrador aprueba una cuenta registrada y le asigna su rol. */
public record ApproveUserRequest(
        @NotNull(message = "El rol es obligatorio") RoleName role,
        Long pilotId,
        Long clientId
) {
}
