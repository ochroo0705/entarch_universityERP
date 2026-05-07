package com.edusys.backend.university.dto;

import java.util.List;

public record DegreeAuditResponse(
        Long studentId,
        String studentName,
        String programName,
        Integer totalRequiredCredits,
        Integer totalCompletedCredits,
        Integer matchedRequiredCredits,
        Integer remainingCredits,
        Double progressPercent,
        Boolean graduationEligible,
        List<DegreeAuditRequirementResponse> requirements
) {}
