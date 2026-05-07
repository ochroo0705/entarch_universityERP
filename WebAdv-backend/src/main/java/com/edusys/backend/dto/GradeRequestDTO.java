package com.edusys.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GradeRequestDTO(
        @NotNull(message = "Student ID is required")
        Long studentId,

        @NotNull(message = "Teaching assignment ID is required")
        Long teachingAssignmentId,

        @NotNull(message = "Quarter is required")
        @Min(value = 1, message = "Quarter must be between 1 and 4")
        @Max(value = 4, message = "Quarter must be between 1 and 4")
        Integer quarter,

        @NotNull(message = "Grade value is required")
        @Min(value = 0, message = "Grade must be between 0 and 100")
        @Max(value = 100, message = "Grade must be between 0 and 100")
        Integer gradeValue,

        @NotNull(message = "Grade type is required")
        GradeType gradeType
) {
    public enum GradeType {
        QUARTER, MIDTERM, FINAL, YEARLY
    }
}