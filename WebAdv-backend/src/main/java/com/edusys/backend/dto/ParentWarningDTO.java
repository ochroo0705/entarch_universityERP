package com.edusys.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record ParentWarningDTO(
        Long studentId,
        String studentName,
        String className,
        int totalAbsent,
        int totalLate,
        int totalExcused,
        int totalSick,
        LocalDate reportStartDate,
        LocalDate reportEndDate,
        String warningLevel,
        List<AbsentDetail> recentAbsences
) {
    public record AbsentDetail(
            LocalDate date,
            String subjectName,
            String status,
            String remarks
    ) {}

    public enum WarningLevel {
        NORMAL, ATTENTION, WARNING, CRITICAL
    }
}