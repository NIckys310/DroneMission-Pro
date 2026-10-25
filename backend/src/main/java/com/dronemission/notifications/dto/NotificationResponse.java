package com.dronemission.notifications.dto;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, String title, String message, Long missionId, boolean read,
                                   LocalDateTime date) {
}
