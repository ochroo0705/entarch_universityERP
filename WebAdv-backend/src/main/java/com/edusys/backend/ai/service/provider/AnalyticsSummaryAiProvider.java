package com.edusys.backend.ai.service.provider;

import com.edusys.backend.ai.service.summary.AnalyticsSummaryGenerationInput;
import com.edusys.backend.ai.service.summary.AnalyticsSummaryGenerationResult;

public interface AnalyticsSummaryAiProvider {
    String providerName();
    boolean isEnabled();
    AnalyticsSummaryGenerationResult generateSummary(AnalyticsSummaryGenerationInput input);
}
