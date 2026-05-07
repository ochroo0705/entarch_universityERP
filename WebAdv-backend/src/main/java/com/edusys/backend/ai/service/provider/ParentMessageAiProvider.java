package com.edusys.backend.ai.service.provider;

import com.edusys.backend.ai.service.draft.ParentMessageGenerationInput;
import com.edusys.backend.ai.service.draft.ParentMessageGenerationResult;

public interface ParentMessageAiProvider {
    String providerName();
    boolean isEnabled();
    ParentMessageGenerationResult generateDraft(ParentMessageGenerationInput input);
}
