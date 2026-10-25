package com.dronemission.evidence.dto;

import java.time.LocalDateTime;

public record EvidenceResponse(Long id, Long missionId, String type, String name, String url, boolean storedFile,
                               String description, boolean visibleToClient, String uploadedBy, LocalDateTime date) {
}
