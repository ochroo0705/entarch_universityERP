package com.edusys.backend.ai.service;

public record AttendanceWindowMetrics(
        long totalRecords,
        long attendedRecords,
        long lateRecords
) {
}
