package com.dronemission.reports.dto;

import java.time.LocalDateTime;

/** Registro del historial de reportes generados. */
public record ReportResponse(Long id, String type, String title, Long missionId, String missionCode,
                             String generatedBy, LocalDateTime generatedAt, String summary) {
}
