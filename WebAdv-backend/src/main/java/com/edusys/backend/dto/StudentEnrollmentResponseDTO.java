package com.edusys.backend.dto;

import com.edusys.backend.model.StudentEnrollment;

import java.time.LocalDate;

public record StudentEnrollmentResponseDTO(
        Long id,
        Long studentId,
        String studentUsername,
        Long classId,
        String className,
        LocalDate enrollmentDate,
        String studentNumber,
        StudentEnrollment.Status status
) {}
