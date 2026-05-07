package com.edusys.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceResponseDTO(
        Long id,
        Long studentId,
        String studentName,
        Long teachingAssignmentId,
        String subjectName,
        String className,
        LocalDate attendanceDate,
        Integer periodNumber,
        AttendanceStatus status,
        String remarks,
        String markedByName,
        LocalDateTime createdAt
) {
    public enum AttendanceStatus {
        PRESENT, ABSENT, LATE, EXCUSED, SICK
    }
}