package com.edusys.backend.ai.dto;

import com.edusys.backend.ai.model.RiskIndicatorCode;

import java.math.BigDecimal;

public record RiskIndicatorResponse(
        RiskIndicatorCode indicatorCode,
        BigDecimal rawValue,
        BigDecimal normalizedRiskValue,
        BigDecimal weight,
        BigDecimal weightedContribution,
        Integer dataPointsCount,
        Boolean isMissingData,
        String detailsJson
) {
}
