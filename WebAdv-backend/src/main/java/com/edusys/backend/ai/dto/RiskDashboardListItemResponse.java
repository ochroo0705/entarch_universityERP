package com.edusys.backend.ai.dto;

import com.edusys.backend.ai.model.RiskLevel;
import com.edusys.backend.ai.model.SnapshotStatus;

import java.time.LocalDateTime;
import java.util.List;

public record RiskDashboardListItemResponse(
        Long snapshotId,
        Long studentId,
        String studentName,
        Long classId,
        String className,
        Integer gradeLevel,
        RiskLevel riskLevel,
        Integer riskScore,
        SnapshotStatus snapshotStatus,
        String reasonSummary,
        Boolean isPlaceholder,
        Boolean isStale,
        LocalDateTime calculatedAt,
        List<String> topIndicators
) {
}
