package com.edusys.backend.ai.service.provider;

import com.edusys.backend.ai.service.draft.ParentMessageGenerationInput;
import com.edusys.backend.ai.service.draft.ParentMessageGenerationResult;
import com.edusys.backend.ai.service.draft.ParentMessagePromptBuilder;
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
public class OpenAiParentMessageProvider implements ParentMessageAiProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final ParentMessagePromptBuilder promptBuilder;
    private final boolean enabled;
    private final String apiKey;
    private final String model;

    public OpenAiParentMessageProvider(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            ParentMessagePromptBuilder promptBuilder,
            @Value("${app.ai.parent-message.provider.enabled:false}") boolean enabled,
            @Value("${app.ai.parent-message.provider.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${app.ai.parent-message.provider.api-key:}") String apiKey,
            @Value("${app.ai.parent-message.provider.model:gpt-4o-mini}") String model,
            @Value("${app.ai.parent-message.provider.timeout-seconds:20}") long timeoutSeconds
    ) {
        this.objectMapper = objectMapper;
        this.promptBuilder = promptBuilder;
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.model = model;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public String providerName() {
        return "OPENAI_COMPAT";
    }

    @Override
    public boolean isEnabled() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public ParentMessageGenerationResult generateDraft(ParentMessageGenerationInput input) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "temperature", 0.4,
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
                throw new ParentMessageGenerationException("EMPTY_PROVIDER_RESPONSE", providerName(), "AI provider returned an empty draft");
            }

            JsonNode contentNode = tryParseJson(content);
            String subject = contentNode != null && contentNode.hasNonNull("subject")
                    ? contentNode.get("subject").asText()
                    : deriveSubject(content, input.studentFirstName());
            String body = contentNode != null && contentNode.hasNonNull("body")
                    ? contentNode.get("body").asText()
                    : content;

            return new ParentMessageGenerationResult(
                    subject,
                    body.trim(),
                    providerName(),
                    jsonNode.path("model").asText(model),
                    jsonNode.path("id").asText(null),
                    safeOutputJson(subject, body),
                    false
            );
        } catch (ParentMessageGenerationException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new ParentMessageGenerationException("PROVIDER_HTTP_ERROR", providerName(), "AI provider request failed", exception);
        } catch (Exception exception) {
            throw new ParentMessageGenerationException("PROVIDER_PARSE_ERROR", providerName(), "AI provider response could not be processed", exception);
        }
    }

    private JsonNode tryParseJson(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readTree(content);
        } catch (Exception ignored) {
            String normalized = unwrapMarkdownCodeFence(content);
            if (normalized == null) {
                return null;
            }
            try {
                return objectMapper.readTree(normalized);
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }

    private String unwrapMarkdownCodeFence(String content) {
        String trimmed = content == null ? null : content.trim();
        if (trimmed == null || trimmed.isBlank()) {
            return null;
        }
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }

        int firstNewline = trimmed.indexOf('\n');
        if (firstNewline < 0) {
            return trimmed;
        }

        String withoutOpeningFence = trimmed.substring(firstNewline + 1);
        int closingFence = withoutOpeningFence.lastIndexOf("```");
        if (closingFence < 0) {
            return trimmed;
        }

        return withoutOpeningFence.substring(0, closingFence).trim();
    }

    private String deriveSubject(String content, String studentFirstName) {
        String firstLine = content.lines().findFirst().orElse("").trim();
        if (!firstLine.isBlank() && firstLine.length() <= 120) {
            return firstLine.replaceFirst("^[^A-Za-z0-9]+", "");
        }
        return "Support update for " + studentFirstName;
    }

    private String safeOutputJson(String subject, String body) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "subject", subject,
                "body", body
        ));
    }
}
