package com.edusys.backend.ai.service.summary;

import com.edusys.backend.ai.service.provider.AnalyticsSummaryAiProvider;
import com.edusys.backend.ai.service.provider.FallbackAnalyticsSummaryProvider;
import com.edusys.backend.ai.service.provider.OpenAiAnalyticsSummaryProvider;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsSummaryGenerationService {

    private final OpenAiAnalyticsSummaryProvider openAiAnalyticsSummaryProvider;
    private final FallbackAnalyticsSummaryProvider fallbackAnalyticsSummaryProvider;

    public AnalyticsSummaryGenerationService(
            OpenAiAnalyticsSummaryProvider openAiAnalyticsSummaryProvider,
            FallbackAnalyticsSummaryProvider fallbackAnalyticsSummaryProvider
    ) {
        this.openAiAnalyticsSummaryProvider = openAiAnalyticsSummaryProvider;
        this.fallbackAnalyticsSummaryProvider = fallbackAnalyticsSummaryProvider;
    }

    public AnalyticsSummaryGenerationResult generate(AnalyticsSummaryGenerationInput input) {
        AnalyticsSummaryAiProvider provider = openAiAnalyticsSummaryProvider.isEnabled()
                ? openAiAnalyticsSummaryProvider
                : fallbackAnalyticsSummaryProvider;
        return provider.generateSummary(input);
    }
}
