package com.edusys.backend.ai.dto;

import com.edusys.backend.ai.model.RiskLevel;
import com.edusys.backend.ai.model.SnapshotStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RiskSnapshotResponse(
        Long id,
        Long studentId,
        String studentName,
        Long classId,
        String className,
        Integer gradeLevel,
        SnapshotStatus snapshotStatus,
        RiskLevel riskLevel,
        Integer riskScore,
        BigDecimal attendanceRate,
        Integer missingHomeworkCount,
        BigDecimal gradeAverage,
        String sourceSummaryJson,
        String reasonSummary,
        String recommendedAction,
        LocalDateTime calculatedAt,
        LocalDateTime calculationWindowStart,
        LocalDateTime calculationWindowEnd,
        LocalDateTime reviewedAt,
        Long reviewedByUserId,
        String reviewedByUserName,
        String modelVersionLabel,
        String scoringConfigVersion,
        String calculationTrigger,
        String calculationError,
        Boolean isPlaceholder,
        List<RiskIndicatorResponse> indicators,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
