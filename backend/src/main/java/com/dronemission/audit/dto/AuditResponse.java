package com.dronemission.audit.dto;

import java.time.LocalDateTime;

public record AuditResponse(Long id, String userEmail, String action, String entity, Long entityId,
                            String description, LocalDateTime date) {
}
