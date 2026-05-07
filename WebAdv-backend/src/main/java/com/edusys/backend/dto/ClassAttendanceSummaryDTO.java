package com.edusys.backend.dto;

import java.util.List;

public record ClassAttendanceSummaryDTO(
        Long classId,
        String className,
        int totalStudents,
        DateRange dateRange,
        OverallStatistics overallStatistics,
        List<StudentAttendanceSummary> studentSummaries
) {}
