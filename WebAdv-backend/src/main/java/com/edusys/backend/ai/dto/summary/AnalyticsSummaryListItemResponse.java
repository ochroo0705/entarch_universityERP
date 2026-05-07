package com.edusys.backend.ai.dto.summary;

import com.edusys.backend.ai.model.SummaryScopeType;
import com.edusys.backend.ai.model.SummaryStatus;
import com.edusys.backend.ai.model.SummaryType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AnalyticsSummaryListItemResponse(
        Long id,
        SummaryType summaryType,
        SummaryScopeType scopeType,
        String scopeKey,
        String scopeLabel,
        LocalDate periodStart,
        LocalDate periodEnd,
        SummaryStatus status,
        Boolean isPlaceholder,
        Boolean isStale,
        LocalDateTime generatedAt,
        LocalDateTime staleAfter,
        String headline
) {
}
