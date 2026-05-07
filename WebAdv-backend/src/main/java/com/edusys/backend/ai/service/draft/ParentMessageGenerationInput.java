package com.edusys.backend.ai.service.draft;

import com.edusys.backend.ai.model.DraftChannel;
import com.edusys.backend.ai.model.IssueType;

import java.math.BigDecimal;
import java.util.List;

public record ParentMessageGenerationInput(
        Long draftId,
        String studentFirstName,
        String className,
        IssueType issueType,
        BigDecimal attendanceRate,
        String attendanceSummary,
        int missingAssignmentCount,
        String gradeTrendSummary,
        String riskLevel,
        List<String> topIndicators,
        String teacherNote,
        String desiredTone,
        String languageCode,
        DraftChannel channel,
        String goalLabel,
        String redactedPayloadJson,
        String promptVersion
) {
}
