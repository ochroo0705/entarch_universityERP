package com.edusys.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ExamResultUpsertRequestDTO(
        @NotNull(message = "Exam schedule ID is required")
        Long examScheduleId,

        @NotNull(message = "Student ID is required")
        Long studentId,

        @NotNull(message = "Score is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Score must be non-negative")
        BigDecimal score,

        @NotNull(message = "Total score is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Total score must be greater than zero")
        BigDecimal totalScore,

        @DecimalMin(value = "0.0", inclusive = true, message = "Weighting must be between 0 and 100")
        @DecimalMax(value = "100.0", inclusive = true, message = "Weighting must be between 0 and 100")
        BigDecimal weighting,

        String teacherComment,

        String remarks,

        Boolean published
) {
}
