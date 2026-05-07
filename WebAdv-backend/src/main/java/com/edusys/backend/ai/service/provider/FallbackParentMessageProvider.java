package com.edusys.backend.ai.service.provider;

import com.edusys.backend.ai.service.draft.ParentMessageGenerationInput;
import com.edusys.backend.ai.service.draft.ParentMessageGenerationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class FallbackParentMessageProvider implements ParentMessageAiProvider {

    private final ObjectMapper objectMapper;

    public FallbackParentMessageProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerName() {
        return "LOCAL_FALLBACK";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public ParentMessageGenerationResult generateDraft(ParentMessageGenerationInput input) {
        boolean mongolian = input.languageCode() != null && input.languageCode().toLowerCase().startsWith("mn");
        String subject = mongolian
                ? input.studentFirstName() + "-ийн талаар товч мэдээлэл"
                : "Support update for " + input.studentFirstName();
        String body = mongolian
                ? """
                Сайн байна уу, эцэг эх асран хамгаалагч аа.

                %s сурагчийн %s хичээл, ангитай холбоотой товч мэдээллийг хүргэж байна. %s

                Одоогоор %d дутуу даалгавар болон %s ажиглагдаж байна. %s

                Дараагийн алхам болгон гэртээ хуваарь, хичээлийн дадал, шаардлагатай дэмжлэгийн талаар хамтдаа ярилцахыг санал болгож байна.

                Хүндэтгэсэн,
                Багш
                """.formatted(
                        input.studentFirstName(),
                        input.className(),
                        input.attendanceSummary(),
                        input.missingAssignmentCount(),
                        input.gradeTrendSummary().toLowerCase(),
                        input.teacherNote() == null || input.teacherNote().isBlank() ? "" : "Багшийн тэмдэглэл: " + input.teacherNote()
                ).replaceAll("\\n{3,}", "\n\n").trim()
                : """
                Dear parent,

                I wanted to share a %s update about %s in %s. %s

                We have noticed %d missing assignments and a %s. %s

                A helpful next step would be to check in together on routines, expectations, and any support your child may need.

                Kind regards,
                Teacher
                """.formatted(
                        input.desiredTone().toLowerCase(),
                        input.studentFirstName(),
                        input.className(),
                        input.attendanceSummary(),
                        input.missingAssignmentCount(),
                        input.gradeTrendSummary().toLowerCase(),
                        input.teacherNote() == null || input.teacherNote().isBlank() ? "" : "Teacher note: " + input.teacherNote()
                ).replaceAll("\\n{3,}", "\n\n").trim();

        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("subject", subject);
            payload.put("body", body);
            payload.put("mode", "fallback");
            return new ParentMessageGenerationResult(
                    subject,
                    body,
                    providerName(),
                    "template-v1",
                    "fallback-" + input.draftId(),
                    objectMapper.writeValueAsString(payload),
                    true
            );
        } catch (Exception exception) {
            throw new ParentMessageGenerationException("FALLBACK_SERIALIZATION_FAILED", providerName(), "Fallback generation failed", exception);
        }
    }
}
