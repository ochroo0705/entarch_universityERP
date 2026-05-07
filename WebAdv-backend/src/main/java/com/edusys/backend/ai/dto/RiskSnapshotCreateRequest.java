package com.edusys.backend.ai.dto;

import jakarta.validation.constraints.NotNull;

public record RiskSnapshotCreateRequest(
        @NotNull(message = "studentId is required")
        Long studentId
) {
}
