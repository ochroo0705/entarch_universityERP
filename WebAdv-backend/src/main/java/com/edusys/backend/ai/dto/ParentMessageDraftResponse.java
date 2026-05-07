package com.edusys.backend.ai.dto;

import com.edusys.backend.ai.model.DraftChannel;
import com.edusys.backend.ai.model.DraftStatus;
import com.edusys.backend.ai.model.IssueType;

import java.time.LocalDateTime;

public record ParentMessageDraftResponse(
        Long id,
        Long studentId,
        String studentName,
        Long parentUserId,
        String parentName,
        Long riskSnapshotId,
        Long createdByUserId,
        String createdByUserName,
        DraftStatus draftStatus,
        DraftChannel channel,
        IssueType issueType,
        String teacherNote,
        String goalLabel,
        String generatedSubject,
        String generatedMessageBody,
        String currentSubject,
        String currentMessageBody,
        String toneLabel,
        String languageCode,
        String generationSource,
        String generationProvider,
        String generationModel,
        String providerRequestId,
        String generationPromptVersion,
        String generationInputRedactedJson,
        String generationOutputRedactedJson,
        String generationErrorCode,
        String generationErrorMessage,
        Boolean isPlaceholder,
        Long lastEditedByUserId,
        String lastEditedByUserName,
        LocalDateTime generatedAt,
        Long approvedByUserId,
        String approvedByUserName,
        LocalDateTime approvedAt,
        Long rejectedByUserId,
        String rejectedByUserName,
        LocalDateTime rejectedAt,
        String rejectionReason,
        LocalDateTime sentAt,
        LocalDateTime lastEditedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean canEdit,
        boolean canApprove,
        boolean canRetryGeneration
) {
}
