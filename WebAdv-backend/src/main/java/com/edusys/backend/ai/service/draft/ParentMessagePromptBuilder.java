package com.edusys.backend.ai.service.draft;

import org.springframework.stereotype.Component;

@Component
public class ParentMessagePromptBuilder {

    public String buildSystemPrompt() {
        return """
                You are generating a school parent communication draft for teacher review.
                Use only the provided facts.
                Do not invent incidents, diagnoses, or sensitive details.
                Keep the message supportive, concise, and actionable.
                Write the subject and body in the requested language.
                If the language code is "mn", write natural Mongolian for a parent in Mongolia.
                The output must be valid JSON with exactly two fields: subject and body.
                The body must be ready for teacher review and must not say the message was sent.
                """;
    }

    public String buildUserPrompt(ParentMessageGenerationInput input) {
        return """
                Create a parent communication draft from this structured context.

                Student first name: %s
                Class or course: %s
                Issue type: %s
                Attendance summary: %s
                Missing assignment count: %d
                Grade trend summary: %s
                Risk level: %s
                Top indicators: %s
                Teacher note: %s
                Desired tone: %s
                Language code: %s
                Channel: %s
                Goal: %s

                Requirements:
                - Mention only facts from the context.
                - Encourage partnership with the parent.
                - Avoid threats or punitive wording.
                - Suggest a practical next step.
                - Keep subject under 12 words.
                - Write the full output in %s.
                """.formatted(
                safe(input.studentFirstName()),
                safe(input.className()),
                input.issueType().name(),
                safe(input.attendanceSummary()),
                input.missingAssignmentCount(),
                safe(input.gradeTrendSummary()),
                safe(input.riskLevel()),
                input.topIndicators() == null || input.topIndicators().isEmpty() ? "None" : String.join(", ", input.topIndicators()),
                safe(input.teacherNote()),
                safe(input.desiredTone()),
                safe(input.languageCode()),
                input.channel().name(),
                safe(input.goalLabel()),
                describeLanguage(input.languageCode())
        );
    }

    private String describeLanguage(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return "Mongolian";
        }
        return switch (languageCode.trim().toLowerCase()) {
            case "mn", "mn-mn" -> "Mongolian";
            case "en", "en-us", "en-gb" -> "English";
            default -> languageCode;
        };
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Not provided" : value;
    }
}
