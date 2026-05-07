package com.edusys.backend.dto;

/**
 * Per-child snapshot for the parent dashboard.
 *
 * Notes:
 * - attendanceRatePercent is null when the student has no attendance records.
 * - overallGpa is the average of recorded grade values (0-100), null when no grades exist.
 */
public record ChildDashboardDTO(
        Long studentId,
        String username,
        String firstName,
        String lastName,
        Double attendanceRatePercent,
        Long homeworkSubmitted,
        Long homeworkTotal,
        Long classesToday,
        Double overallGpa
) {
}
