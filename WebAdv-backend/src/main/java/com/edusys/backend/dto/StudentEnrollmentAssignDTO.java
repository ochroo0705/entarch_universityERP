package com.edusys.backend.dto;

import com.edusys.backend.model.StudentEnrollment;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StudentEnrollmentAssignDTO(
        @NotNull Long studentId,
        @NotNull Long classId,
        LocalDate enrollmentDate,
        String studentNumber,
        StudentEnrollment.Status status
) {}
