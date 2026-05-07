package com.edusys.backend.university.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CourseSelectionRequest(
        @NotNull Long studentId,
        @NotEmpty List<Long> subjectIds,
        String academicYear,
        Integer semester
) {}
