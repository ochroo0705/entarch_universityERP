package com.edusys.backend.university.dto;

import com.edusys.backend.university.model.AcademicRecordStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AcademicRecordRequest(
        @NotNull Long studentId,
        @NotNull Long subjectId,
        String academicYear,
        Integer semester,
        BigDecimal finalGrade,
        AcademicRecordStatus status
) {}
