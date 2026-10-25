package com.dronemission.incidents.dto;

import java.time.LocalDateTime;

public record IncidentResponse(Long id, Long missionId, String missionCode, String missionName, LocalDateTime date,
                               String type, String severity, String description, String reportedBy,
                               String status, String resolution) {
}
