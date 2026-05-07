package com.edusys.backend.university.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AcademicPolicyRequest(
        @NotBlank @Size(max = 120) String policyName,
        @NotNull @Min(0) @Max(30) Integer minTermCredits,
        @NotNull @Min(1) @Max(30) Integer maxTermCredits,
        @NotNull @Min(1) @Max(30) Integer probationMaxTermCredits,
        @NotNull @DecimalMin("0.00") BigDecimal minAverageGradeGoodStanding,
        Boolean blockRegistrationWhenProbation,
        Boolean allowRepeatCompletedCourses
) {}
