package com.edusys.backend.university.dto;

import java.time.LocalDate;

public record FacultyProfileResponse(
        Long id,
        Long facultyUserId,
        String facultyName,
        String email,
        String employeeNumber,
        String department,
        String academicRank,
        String employmentStatus,
        LocalDate hireDate,
        String officeLocation,
        Integer workloadTargetCredits,
        Integer activeTeachingAssignments,
        Integer assignedCredits,
        Integer workloadVariance
) {}
