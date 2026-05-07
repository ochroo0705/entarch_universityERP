package com.edusys.backend.university.dto;

public record ProgramRequirementResponse(
        Long id,
        String programName,
        String requirementName,
        Long subjectId,
        String courseName,
        String courseCode,
        Integer requiredCredits,
        Boolean active
) {}
