package com.edusys.backend.dto;

public record OverallStatistics(
        int totalDays,
        double averageAttendanceRate,
        int totalAbsences,
        int totalLateArrivals
) {}

