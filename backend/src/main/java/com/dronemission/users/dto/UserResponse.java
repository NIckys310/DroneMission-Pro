package com.dronemission.users.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String role,
        boolean active,
        Long pilotId,
        Long clientId,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
}
