package com.edusys.backend.university.dto;

public record FacultyWorkloadResponse(
        Long id,
        Long facultyProfileId,
        String facultyName,
        String academicYear,
        Integer semester,
        Integer teachingCredits,
        Integer advisingCredits,
        Integer researchCredits,
        Integer committeeCredits,
        Integer totalCredits,
        String notes
) {}
