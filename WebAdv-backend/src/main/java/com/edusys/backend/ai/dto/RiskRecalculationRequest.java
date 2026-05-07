package com.edusys.backend.ai.dto;

public record RiskRecalculationRequest(
        Long studentId,
        Long classId
) {
}
