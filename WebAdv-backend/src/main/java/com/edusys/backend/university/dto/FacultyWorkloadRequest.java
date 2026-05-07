package com.edusys.backend.university.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FacultyWorkloadRequest(
        @NotNull Long facultyProfileId,
        @NotBlank String academicYear,
        @NotNull @Min(1) Integer semester,
        @Min(0) Integer teachingCredits,
        @Min(0) Integer advisingCredits,
        @Min(0) Integer researchCredits,
        @Min(0) Integer committeeCredits,
        String notes
) {}
