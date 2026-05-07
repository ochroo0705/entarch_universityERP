package com.edusys.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AttendanceRequestDTO(
        @NotNull(message = "Student ID is required")
        Long studentId,

        @NotNull(message = "Teaching assignment ID is required")
        Long teachingAssignmentId,

        @NotNull(message = "Attendance date is required")
        LocalDate attendanceDate,

        @NotNull(message = "Period number is required")
        Integer periodNumber,

        @NotNull(message = "Status is required")
        AttendanceStatus status,

        String remarks
) {
    public enum AttendanceStatus {
        PRESENT, ABSENT, LATE, EXCUSED, SICK
    }
}