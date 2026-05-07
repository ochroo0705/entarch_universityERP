package com.edusys.backend.university.dto;

public record DegreeAuditRequirementResponse(
        Long requirementId,
        String requirementName,
        Long subjectId,
        String courseName,
        String courseCode,
        Integer requiredCredits,
        Integer completedCredits,
        Boolean satisfied
) {}
