package com.edusys.backend.dto;

public record StudentAttendanceSummary(
        Long studentId,
        String studentName,
        int presentDays,
        int absentDays,
        int lateDays,
        double attendanceRate
) {}
