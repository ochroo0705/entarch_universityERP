package com.edusys.backend.ai.dto;

import com.edusys.backend.ai.model.RiskLevel;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record RiskDashboardQueryRequest(
        Long classId,
        Integer gradeLevel,
        RiskLevel riskLevel,
        String search,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromCalculatedAt,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toCalculatedAt
) {
}
