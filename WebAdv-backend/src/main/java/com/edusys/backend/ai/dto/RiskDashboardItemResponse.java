package com.edusys.backend.ai.dto;

import com.edusys.backend.ai.model.RiskLevel;
import com.edusys.backend.ai.model.SnapshotStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RiskDashboardItemResponse(
        Long id,
        Long studentId,
        String studentName,
        Long classId,
        String className,
        Integer gradeLevel,
        RiskLevel riskLevel,
        Integer riskScore,
        BigDecimal attendanceRate,
        Integer missingHomeworkCount,
        BigDecimal gradeAverage,
        SnapshotStatus snapshotStatus,
        String reasonSummary,
        String recommendedAction,
        Boolean isPlaceholder,
        Boolean isStale,
        LocalDateTime calculatedAt,
        List<String> topIndicators
) {
}
