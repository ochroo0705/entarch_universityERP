package com.edusys.backend.ai.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RiskSummaryResponse(
        long totalStudents,
        long lowRiskCount,
        long mediumRiskCount,
        long highRiskCount,
        LocalDateTime latestCalculatedAt,
        List<RiskSummaryBucketResponse> classBreakdown
) {
    public record RiskSummaryBucketResponse(
            Long classId,
            String className,
            Integer gradeLevel,
            long totalStudents,
            long highRiskCount
    ) {
    }
}
