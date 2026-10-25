package com.dronemission.users.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String username,
        String email,
        String role,
        boolean active,
        boolean pendingApproval,
        LocalDateTime lockedUntil,
        Long pilotId,
        Long clientId,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
}
