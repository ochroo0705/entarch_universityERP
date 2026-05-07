package com.edusys.backend.ai.dto.summary;

import com.edusys.backend.ai.model.SummaryScopeType;
import com.edusys.backend.ai.model.SummaryStatus;
import com.edusys.backend.ai.model.SummaryType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AnalyticsSummaryResponse(
        Long id,
        SummaryType summaryType,
        SummaryScopeType scopeType,
        String scopeKey,
        String scopeLabel,
        String generatedForRole,
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDate comparisonPeriodStart,
        LocalDate comparisonPeriodEnd,
        SummaryStatus status,
        Boolean isPlaceholder,
        Boolean isStale,
        LocalDateTime generatedAt,
        LocalDateTime staleAfter,
        String providerName,
        String providerModel,
        String promptVersion,
        String headline,
        String overallSummary,
        List<String> keyObservations,
        List<String> watchAreas,
        List<String> recommendedActions,
        String confidenceNote,
        String inputRedactedJson,
        String summaryJson,
        String generationErrorCode,
        String generationErrorMessage
) {
}
