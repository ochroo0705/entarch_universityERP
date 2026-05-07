package com.edusys.backend.ai.dto.summary;

import com.edusys.backend.ai.model.SummaryType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AnalyticsSummaryGenerateRequest(
        @NotNull SummaryType summaryType,
        Long classId,
        Integer gradeLevel,
        LocalDate periodStart,
        LocalDate periodEnd,
        Boolean forceRefresh,
        String languageCode
) {
}
