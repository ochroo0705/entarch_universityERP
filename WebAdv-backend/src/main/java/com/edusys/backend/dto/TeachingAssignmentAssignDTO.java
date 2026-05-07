package com.edusys.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TeachingAssignmentAssignDTO(
        @NotNull Long teacherId,
        @NotNull Long subjectId,
        @NotNull Long classId,
        @NotBlank String academicYear,
        @NotNull Integer semester,
        Boolean isActive
) {}