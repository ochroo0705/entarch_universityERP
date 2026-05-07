package com.edusys.backend.ai.service.summary;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class AnalyticsSummaryInputBuilder {

    public static final String PROMPT_VERSION = "phase4-analytics-summary-v1";

    private final ObjectMapper objectMapper;

    public AnalyticsSummaryInputBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PreparedAnalyticsSummaryInput build(AnalyticsAggregateQueryService.SummaryScopeData scopeData, String requestedLanguageCode) {
        try {
            String languageCode = normalizeLanguage(requestedLanguageCode);
            var payload = new java.util.LinkedHashMap<>(scopeData.payload());
            @SuppressWarnings("unchecked")
            var constraints = new java.util.LinkedHashMap<String, Object>((java.util.Map<String, Object>) payload.get("constraints"));
            constraints.put("language_code", languageCode);
            payload.put("constraints", constraints);
            String payloadJson = objectMapper.writeValueAsString(payload);
            String fingerprint = fingerprint(scopeData.summaryType().name() + "|" + scopeData.scopeType().name() + "|" + scopeData.scopeKey() + "|" + scopeData.periodStart() + "|" + scopeData.periodEnd() + "|" + languageCode + "|" + payloadJson + "|" + PROMPT_VERSION);
            AnalyticsSummaryGenerationInput input = new AnalyticsSummaryGenerationInput(
                    scopeData.summaryType(),
                    scopeData.scopeType(),
                    scopeData.scopeKey(),
                    scopeData.scopeLabel(),
                    scopeData.periodStart(),
                    scopeData.periodEnd(),
                    scopeData.comparisonPeriodStart(),
                    scopeData.comparisonPeriodEnd(),
                    languageCode,
                    payloadJson,
                    PROMPT_VERSION
            );
            return new PreparedAnalyticsSummaryInput(input, fingerprint, payloadJson);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to prepare analytics summary input", exception);
        }
    }

    private String fingerprint(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private String normalizeLanguage(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return "mn";
        }
        String normalized = languageCode.trim().toLowerCase();
        return normalized.startsWith("en") ? "en" : "mn";
    }

    public record PreparedAnalyticsSummaryInput(
            AnalyticsSummaryGenerationInput input,
            String fingerprint,
            String redactedPayloadJson
    ) {
    }
}
