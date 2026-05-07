package com.edusys.backend.dto;

import com.edusys.backend.model.StudentEnrollment;

import java.time.LocalDate;

public record StudentEnrollmentListItemDTO(
        Long id,
        StudentDTO student,
        ClassDTO classEntity,
        LocalDate enrollmentDate,
        String studentNumber,
        StudentEnrollment.Status status
) {
    public record StudentDTO(
            Long id,
            String username,
            String firstName,
            String lastName
    ) {}

    public record ClassDTO(
            Long id,
            String className,
            Integer grade,
            String section
    ) {}
}
