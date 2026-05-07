package com.edusys.backend.dto;

import java.math.BigDecimal;

public record ExamRosterItemDTO(
        Long examScheduleId,
        Long examResultId,
        Long studentId,
        String studentName,
        String examTitle,
        String examDate,
        String subject,
        String className,
        BigDecimal score,
        BigDecimal totalScore,
        BigDecimal percentage,
        BigDecimal weighting,
        String teacherComment,
        String remarks,
        Boolean published
) {
}
