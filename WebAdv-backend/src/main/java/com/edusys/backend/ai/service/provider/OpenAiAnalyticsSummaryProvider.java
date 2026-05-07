package com.edusys.backend.ai.service.provider;

import com.edusys.backend.ai.service.summary.AnalyticsSummaryGenerationInput;
import com.edusys.backend.ai.service.summary.AnalyticsSummaryGenerationResult;
import com.edusys.backend.ai.service.summary.AnalyticsSummaryPromptBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiAnalyticsSummaryProvider implements AnalyticsSummaryAiProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AnalyticsSummaryPromptBuilder promptBuilder;
    private final boolean enabled;
    private final String apiKey;
    private final String model;

    public OpenAiAnalyticsSummaryProvider(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            AnalyticsSummaryPromptBuilder promptBuilder,
            @Value("${app.ai.analytics-summary.provider.enabled:false}") boolean enabled,
            @Value("${app.ai.analytics-summary.provider.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${app.ai.analytics-summary.provider.api-key:}") String apiKey,
            @Value("${app.ai.analytics-summary.provider.model:gpt-4o-mini}") String model,
            @Value("${app.ai.analytics-summary.provider.timeout-seconds:20}") long timeoutSeconds
    ) {
        this.objectMapper = objectMapper;
        this.promptBuilder = promptBuilder;
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.model = model;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = restClientBuilder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Override
    public String providerName() {
        return "OPENAI_COMPAT_ANALYTICS_SUMMARY";
    }

    @Override
    public boolean isEnabled() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public AnalyticsSummaryGenerationResult generateSummary(AnalyticsSummaryGenerationInput input) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "temperature", 0.2,
                    "messages", List.of(
                            Map.of("role", "system", "content", promptBuilder.buildSystemPrompt()),
                            Map.of("role", "user", "content", promptBuilder.buildUserPrompt(input))
                    )
            );
            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String content = jsonNode.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new AnalyticsSummaryGenerationException("EMPTY_PROVIDER_RESPONSE", providerName(), "AI provider returned an empty analytics summary");
            }
            JsonNode parsed = parseContent(content);
            java.util.List<String> keyObservations = toStringList(parsed.path("keyObservations"));
            java.util.List<String> watchAreas = toStringList(parsed.path("watchAreas"));
            java.util.List<String> recommendedActions = toStringList(parsed.path("recommendedActions"));
            return new AnalyticsSummaryGenerationResult(
                    parsed.path("headline").asText("Analytics summary"),
                    parsed.path("overallSummary").asText("Summary unavailable."),
                    keyObservations,
                    watchAreas,
                    recommendedActions,
                    parsed.path("confidenceNote").asText("Review the supporting metrics below."),
                    providerName(),
                    jsonNode.path("model").asText(model),
                    jsonNode.path("id").asText(null),
                    objectMapper.writeValueAsString(Map.of(
                            "headline", parsed.path("headline").asText("Analytics summary"),
                            "overallSummary", parsed.path("overallSummary").asText("Summary unavailable."),
                            "keyObservations", keyObservations,
                            "watchAreas", watchAreas,
                            "recommendedActions", recommendedActions,
                            "confidenceNote", parsed.path("confidenceNote").asText("Review the supporting metrics below.")
                    )),
                    false
            );
        } catch (AnalyticsSummaryGenerationException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new AnalyticsSummaryGenerationException("PROVIDER_HTTP_ERROR", providerName(), "AI summary provider request failed", exception);
        } catch (Exception exception) {
            throw new AnalyticsSummaryGenerationException("PROVIDER_PARSE_ERROR", providerName(), "AI summary provider response could not be processed", exception);
        }
    }

    private JsonNode parseContent(String content) throws Exception {
        try {
            return objectMapper.readTree(content);
        } catch (Exception ignored) {
            String trimmed = content.trim();
            if (trimmed.startsWith("```")) {
                int firstNewline = trimmed.indexOf('\n');
                int closingFence = trimmed.lastIndexOf("```");
                if (firstNewline > -1 && closingFence > firstNewline) {
                    trimmed = trimmed.substring(firstNewline + 1, closingFence).trim();
                }
            }
            return objectMapper.readTree(trimmed);
        }
    }

    private java.util.List<String> toStringList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return java.util.List.of();
        }
        java.util.List<String> values = new java.util.ArrayList<>();
        node.forEach(item -> {
            String value = item.asText("");
            if (!value.isBlank()) {
                values.add(value.trim());
            }
        });
        return values;
    }
}
