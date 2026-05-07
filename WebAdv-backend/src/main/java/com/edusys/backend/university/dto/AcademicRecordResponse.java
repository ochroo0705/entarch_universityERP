package com.edusys.backend.university.dto;

import com.edusys.backend.university.model.AcademicRecordStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AcademicRecordResponse(
        Long id,
        Long studentId,
        String studentName,
        Long subjectId,
        String courseName,
        String courseCode,
        String academicYear,
        Integer semester,
        BigDecimal finalGrade,
        AcademicRecordStatus status,
        LocalDateTime completedAt
) {}
