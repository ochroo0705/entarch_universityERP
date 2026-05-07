package com.edusys.backend.ai.service.provider;

import com.edusys.backend.ai.service.draft.ParentMessageGenerationInput;
import com.edusys.backend.ai.service.draft.ParentMessageGenerationResult;
import org.springframework.stereotype.Service;

@Service
public class ParentMessageGenerationService {

    private final OpenAiParentMessageProvider openAiParentMessageProvider;
    private final FallbackParentMessageProvider fallbackParentMessageProvider;

    public ParentMessageGenerationService(
            OpenAiParentMessageProvider openAiParentMessageProvider,
            FallbackParentMessageProvider fallbackParentMessageProvider
    ) {
        this.openAiParentMessageProvider = openAiParentMessageProvider;
        this.fallbackParentMessageProvider = fallbackParentMessageProvider;
    }

    public ParentMessageGenerationResult generate(ParentMessageGenerationInput input) {
        ParentMessageGenerationResult result = openAiParentMessageProvider.isEnabled()
                ? openAiParentMessageProvider.generateDraft(input)
                : fallbackParentMessageProvider.generateDraft(input);

        validateResult(result);
        return result;
    }

    private void validateResult(ParentMessageGenerationResult result) {
        if (result.subject() == null || result.subject().isBlank()) {
            throw new ParentMessageGenerationException("INVALID_PROVIDER_OUTPUT", result.providerName(), "AI draft subject was empty");
        }
        if (result.messageBody() == null || result.messageBody().isBlank()) {
            throw new ParentMessageGenerationException("INVALID_PROVIDER_OUTPUT", result.providerName(), "AI draft body was empty");
        }
        if (result.messageBody().length() > 4000) {
            throw new ParentMessageGenerationException("INVALID_PROVIDER_OUTPUT", result.providerName(), "AI draft body was too long");
        }
    }
}
