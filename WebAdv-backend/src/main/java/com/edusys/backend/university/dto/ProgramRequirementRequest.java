package com.edusys.backend.university.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProgramRequirementRequest(
        @NotBlank @Size(max = 160) String programName,
        @NotBlank @Size(max = 160) String requirementName,
        Long subjectId,
        @NotNull @Min(1) @Max(240) Integer requiredCredits,
        Boolean active
) {}
