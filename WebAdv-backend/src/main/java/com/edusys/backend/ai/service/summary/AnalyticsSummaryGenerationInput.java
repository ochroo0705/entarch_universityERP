package com.edusys.backend.ai.service.summary;

import com.edusys.backend.ai.model.SummaryScopeType;
import com.edusys.backend.ai.model.SummaryType;

import java.time.LocalDate;

public record AnalyticsSummaryGenerationInput(
        SummaryType summaryType,
        SummaryScopeType scopeType,
        String scopeKey,
        String scopeLabel,
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDate comparisonPeriodStart,
        LocalDate comparisonPeriodEnd,
        String languageCode,
        String redactedPayloadJson,
        String promptVersion
) {
}
