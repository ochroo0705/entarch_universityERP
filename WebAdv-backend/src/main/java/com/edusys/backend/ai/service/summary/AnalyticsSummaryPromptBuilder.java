package com.edusys.backend.ai.service.summary;

import org.springframework.stereotype.Component;

@Component
public class AnalyticsSummaryPromptBuilder {

    public String buildSystemPrompt() {
        return """
                You are generating a school analytics summary for teachers or administrators.
                Use only the provided aggregate metrics and trend fields.
                Do not mention or infer student names, private records, diagnoses, or causes not present in the data.
                Keep the summary concise, professional, and grounded in the metrics.
                If coverage is limited, say so plainly.
                Return valid JSON with exactly these fields:
                headline, overallSummary, keyObservations, watchAreas, recommendedActions, confidenceNote.
                keyObservations, watchAreas, and recommendedActions must be arrays of short strings.
                If languageCode is "mn", write natural Mongolian.
                If languageCode is "en", write natural English.
                """;
    }

    public String buildUserPrompt(AnalyticsSummaryGenerationInput input) {
        return """
                Summarize this school analytics payload for dashboard use.

                Summary type: %s
                Scope type: %s
                Scope label: %s
                Period start: %s
                Period end: %s
                Comparison period start: %s
                Comparison period end: %s
                Language code: %s

                Constraints:
                - mention only provided metrics
                - do not invent causes
                - do not reference individual students
                - keep observations actionable and tied to charts/tables
                - keep each list to 2-4 items
                - if cohort size is limited or coverage is weak, reflect that in confidenceNote
                - write the entire response in %s

                Aggregate payload:
                %s
                """.formatted(
                input.summaryType().name(),
                input.scopeType().name(),
                safe(input.scopeLabel()),
                input.periodStart(),
                input.periodEnd(),
                input.comparisonPeriodStart(),
                input.comparisonPeriodEnd(),
                normalizeLanguage(input.languageCode()),
                describeLanguage(input.languageCode()),
                input.redactedPayloadJson()
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Current scope" : value;
    }

    private String normalizeLanguage(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return "mn";
        }
        return languageCode.trim().toLowerCase();
    }

    private String describeLanguage(String languageCode) {
        return switch (normalizeLanguage(languageCode)) {
            case "en", "en-us", "en-gb" -> "English";
            case "mn", "mn-mn" -> "Mongolian";
            default -> "Mongolian";
        };
    }
}
