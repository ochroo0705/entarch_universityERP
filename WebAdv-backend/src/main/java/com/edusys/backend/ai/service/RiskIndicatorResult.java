package com.edusys.backend.ai.service;

import com.edusys.backend.ai.model.RiskIndicatorCode;

import java.math.BigDecimal;

public record RiskIndicatorResult(
        RiskIndicatorCode indicatorCode,
        BigDecimal rawValue,
        BigDecimal normalizedRiskValue,
        int dataPointsCount,
        boolean missingData,
        String detailsJson
) {
}
