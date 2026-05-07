package com.edusys.backend.university.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AcademicPolicyResponse(
        Long id,
        String policyName,
        Integer minTermCredits,
        Integer maxTermCredits,
        Integer probationMaxTermCredits,
        BigDecimal minAverageGradeGoodStanding,
        Boolean blockRegistrationWhenProbation,
        Boolean allowRepeatCompletedCourses,
        Boolean active,
        LocalDateTime updatedAt
) {}
