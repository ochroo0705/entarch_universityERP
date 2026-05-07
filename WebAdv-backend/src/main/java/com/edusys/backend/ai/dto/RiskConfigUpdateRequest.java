package com.edusys.backend.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RiskConfigUpdateRequest(
        @NotBlank String configVersion,
        @NotNull BigDecimal attendanceWeight,
        @NotNull BigDecimal latenessWeight,
        @NotNull BigDecimal homeworkWeight,
        @NotNull BigDecimal gradeWeight,
        @NotNull @Min(0) @Max(100) Integer lowMaxScore,
        @NotNull @Min(0) @Max(100) Integer mediumMaxScore,
        @NotNull @Min(1) Integer attendanceWindowDays,
        @NotNull @Min(1) Integer homeworkWindowDays,
        @NotNull @Min(1) Integer gradeWindowDays,
        Boolean activate
) {
}
