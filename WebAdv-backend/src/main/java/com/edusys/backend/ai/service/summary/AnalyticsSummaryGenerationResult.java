package com.edusys.backend.ai.service.summary;

public record AnalyticsSummaryGenerationResult(
        String headline,
        String overallSummary,
        java.util.List<String> keyObservations,
        java.util.List<String> watchAreas,
        java.util.List<String> recommendedActions,
        String confidenceNote,
        String providerName,
        String providerModel,
        String providerRequestId,
        String outputRedactedJson,
        boolean placeholder
) {
}
