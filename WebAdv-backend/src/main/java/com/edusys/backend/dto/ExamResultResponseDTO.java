package com.edusys.backend.dto;

import java.math.BigDecimal;

public record ExamResultResponseDTO(
        Long id,
        Long examScheduleId,
        Long teachingAssignmentId,
        Long studentId,
        String studentName,
        Long teacherId,
        String teacherName,
        Long classId,
        String className,
        Long subjectId,
        String subject,
        String examTitle,
        String examDate,
        String startTime,
        String endTime,
        String roomNumber,
        String notes,
        BigDecimal score,
        BigDecimal totalScore,
        BigDecimal percentage,
        BigDecimal weighting,
        String teacherComment,
        String remarks,
        Boolean published,
        String createdAt,
        String updatedAt
) {
}
