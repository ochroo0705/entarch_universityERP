package com.edusys.backend.ai.dto;

import java.time.LocalDateTime;

public record RiskRecalculationJobResponse(
        String status,
        String scope,
        Integer requestedStudentCount,
        LocalDateTime startedAt,
        String message
) {
}
