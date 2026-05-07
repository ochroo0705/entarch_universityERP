package com.edusys.backend.dto;

import java.time.LocalDateTime;

public record GradeResponseDTO(
        Long id,
        Long studentId,
        String studentName,
        Long teachingAssignmentId,
        String subjectName,
        String className,
        Integer quarter,
        Integer gradeValue,
        GradeType gradeType,
        String recordedByName,
        LocalDateTime recordedAt
) {
    public enum GradeType {
        QUARTER, MIDTERM, FINAL, YEARLY
    }
}