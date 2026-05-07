package com.edusys.backend.ai.service.provider;

import com.edusys.backend.ai.service.summary.AnalyticsSummaryGenerationInput;
import com.edusys.backend.ai.service.summary.AnalyticsSummaryGenerationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FallbackAnalyticsSummaryProvider implements AnalyticsSummaryAiProvider {

    private final ObjectMapper objectMapper;

    public FallbackAnalyticsSummaryProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerName() {
        return "FALLBACK_ANALYTICS_SUMMARY";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public AnalyticsSummaryGenerationResult generateSummary(AnalyticsSummaryGenerationInput input) {
        try {
            JsonNode payload = objectMapper.readTree(input.redactedPayloadJson());
            JsonNode metrics = payload.path("aggregate_metrics");
            int total = metrics.path("totalStudents").asInt(0);
            int high = metrics.path("highRiskCount").asInt(0);
            double attendance = metrics.path("averageAttendanceRate").asDouble(0.0);
            double grades = metrics.path("averageGradeAverage").asDouble(0.0);
            String headline = total == 0
                    ? "No analytics summary available"
                    : "Overview for " + input.scopeLabel();
            String overall = total == 0
                    ? "There are no current risk snapshots in this scope yet."
                    : "This summary is based on aggregated risk analytics for %d students. High-risk cases count is %d, average attendance is %.1f%%, and average grade is %.1f."
                    .formatted(total, high, attendance, grades);
            List<String> observations = List.of(
                    "Review the risk distribution chart alongside this summary.",
                    high > 0 ? "Prioritize students represented in the high-risk bucket." : "No high-risk spike is visible in the current aggregate.",
                    "Use supporting tables to confirm which classes or indicators are driving the trend."
            );
            List<String> watchAreas = List.of(
                    "Track stale snapshot coverage before acting on older trends.",
                    "Compare attendance, homework, and grade averages before escalating."
            );
            List<String> actions = List.of(
                    "Refresh the summary after major snapshot recalculations.",
                    "Use charts and tables below to verify the most affected classes or indicators."
            );
            String json = objectMapper.writeValueAsString(Map.of(
                    "headline", headline,
                    "overallSummary", overall,
                    "keyObservations", observations,
                    "watchAreas", watchAreas,
                    "recommendedActions", actions,
                    "confidenceNote", "Fallback summary based on aggregated metrics only."
            ));
            return new AnalyticsSummaryGenerationResult(
                    headline,
                    overall,
                    observations,
                    watchAreas,
                    actions,
                    "Fallback summary based on aggregated metrics only.",
                    providerName(),
                    "phase4-fallback-v1",
                    null,
                    json,
                    true
            );
        } catch (Exception exception) {
            throw new AnalyticsSummaryGenerationException("FALLBACK_BUILD_ERROR", providerName(), "Fallback analytics summary could not be created", exception);
        }
    }
}
