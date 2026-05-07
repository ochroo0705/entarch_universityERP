package com.edusys.backend.ai.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RiskCalculationContext(
        Long studentId,
        LocalDate attendanceWindowStart,
        LocalDate attendanceWindowEnd,
        LocalDate homeworkWindowStart,
        LocalDate homeworkWindowEnd,
        LocalDateTime gradeWindowStart,
        LocalDateTime gradeWindowEnd
) {
}
