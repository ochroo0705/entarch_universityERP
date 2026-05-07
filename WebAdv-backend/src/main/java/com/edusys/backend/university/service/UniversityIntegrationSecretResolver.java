package com.edusys.backend.university.service;

import java.util.Locale;
import java.util.Optional;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class UniversityIntegrationSecretResolver {
    private final Environment environment;

    public UniversityIntegrationSecretResolver(Environment environment) {
        this.environment = environment;
    }

    public Optional<String> resolve(String secretRef) {
        if (secretRef == null || secretRef.isBlank()) {
            return Optional.empty();
        }
        String trimmed = secretRef.trim();
        if (trimmed.regionMatches(true, 0, "env:", 0, 4)) {
            return fromEnvironment(trimmed.substring(4));
        }
        if (trimmed.regionMatches(true, 0, "property:", 0, 9)) {
            return Optional.ofNullable(environment.getProperty(trimmed.substring(9)))
                    .filter(value -> !value.isBlank());
        }
        return fromEnvironment(trimmed)
                .or(() -> fromEnvironment(trimmed.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_]", "_")))
                .or(() -> Optional.ofNullable(environment.getProperty(trimmed)).filter(value -> !value.isBlank()));
    }

    private Optional<String> fromEnvironment(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        String value = System.getenv(name.trim());
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }
}
