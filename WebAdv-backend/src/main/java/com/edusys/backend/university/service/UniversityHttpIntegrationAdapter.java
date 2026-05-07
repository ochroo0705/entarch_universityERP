package com.edusys.backend.university.service;

import com.edusys.backend.university.model.UniversityIntegrationConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class UniversityHttpIntegrationAdapter {
    private final HttpClient httpClient;
    private final UniversityIntegrationSecretResolver secretResolver;

    public UniversityHttpIntegrationAdapter(UniversityIntegrationSecretResolver secretResolver) {
        this.secretResolver = secretResolver;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public AdapterResult post(UniversityIntegrationConnection connection, String payload) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(connection.getEndpointUrl()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload == null ? "{}" : payload));
            applyAuth(builder, connection);
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
            String body = response.body() == null ? "" : response.body();
            String preview = body.length() > 500 ? body.substring(0, 500) : body;
            return new AdapterResult(success, response.statusCode(), preview, null);
        } catch (Exception ex) {
            return new AdapterResult(false, null, null, ex.getMessage());
        }
    }

    public boolean canResolveSecret(UniversityIntegrationConnection connection) {
        return "NONE".equalsIgnoreCase(connection.getAuthType())
                || secretResolver.resolve(connection.getSecretRef()).isPresent();
    }

    private void applyAuth(HttpRequest.Builder builder, UniversityIntegrationConnection connection) {
        String authType = connection.getAuthType() == null ? "NONE" : connection.getAuthType();
        if ("NONE".equalsIgnoreCase(authType)) {
            return;
        }
        String secret = secretResolver.resolve(connection.getSecretRef())
                .orElseThrow(() -> new IllegalStateException("Secret reference could not be resolved"));
        if ("API_KEY".equalsIgnoreCase(authType)) {
            builder.header("X-API-Key", secret);
        } else if ("BEARER_TOKEN".equalsIgnoreCase(authType)) {
            builder.header("Authorization", "Bearer " + secret);
        } else if ("BASIC".equalsIgnoreCase(authType)) {
            String value = secret.contains(":")
                    ? Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8))
                    : secret;
            builder.header("Authorization", "Basic " + value);
        }
    }

    public record AdapterResult(
            boolean success,
            Integer statusCode,
            String responsePreview,
            String errorMessage
    ) {}
}
