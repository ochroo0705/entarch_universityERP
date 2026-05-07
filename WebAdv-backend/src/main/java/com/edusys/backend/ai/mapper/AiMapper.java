package com.edusys.backend.ai.mapper;

import com.edusys.backend.ai.dto.summary.AnalyticsSummaryListItemResponse;
import com.edusys.backend.ai.dto.summary.AnalyticsSummaryResponse;
import com.edusys.backend.ai.model.AnalyticsSummary;
import com.edusys.backend.ai.dto.AiAuditLogResponse;
import com.edusys.backend.ai.dto.ParentMessageDraftResponse;
import com.edusys.backend.ai.dto.RiskDashboardItemResponse;
import com.edusys.backend.ai.dto.RiskIndicatorResponse;
import com.edusys.backend.ai.dto.RiskSnapshotResponse;
import com.edusys.backend.ai.model.AiAuditLog;
import com.edusys.backend.ai.model.DraftStatus;
import com.edusys.backend.ai.model.ParentMessageDraft;
import com.edusys.backend.ai.model.StudentRiskIndicatorSnapshot;
import com.edusys.backend.ai.model.StudentRiskSnapshot;
import com.edusys.backend.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AiMapper {

    private AiMapper() {
    }

    public static RiskSnapshotResponse toRiskSnapshotResponse(StudentRiskSnapshot snapshot) {
        return new RiskSnapshotResponse(
                snapshot.getId(),
                snapshot.getStudent().getId(),
                snapshot.getStudent().getFullName().trim(),
                snapshot.getClassEntity() != null ? snapshot.getClassEntity().getId() : null,
                snapshot.getClassEntity() != null ? snapshot.getClassEntity().getClassName() : null,
                snapshot.getGradeLevel(),
                snapshot.getSnapshotStatus(),
                snapshot.getRiskLevel(),
                snapshot.getRiskScore(),
                snapshot.getAttendanceRate(),
                snapshot.getMissingHomeworkCount(),
                snapshot.getGradeAverage(),
                snapshot.getSourceSummaryJson(),
                snapshot.getReasonSummary(),
                snapshot.getRecommendedAction(),
                snapshot.getCalculatedAt(),
                snapshot.getCalculationWindowStart(),
                snapshot.getCalculationWindowEnd(),
                snapshot.getReviewedAt(),
                snapshot.getReviewedByUser() != null ? snapshot.getReviewedByUser().getId() : null,
                fullName(snapshot.getReviewedByUser()),
                snapshot.getModelVersionLabel(),
                snapshot.getScoringConfigVersion(),
                snapshot.getCalculationTrigger() != null ? snapshot.getCalculationTrigger().name() : null,
                snapshot.getCalculationError(),
                snapshot.getIsPlaceholder(),
                toIndicatorResponses(snapshot.getIndicatorSnapshots()),
                snapshot.getCreatedAt(),
                snapshot.getUpdatedAt()
        );
    }

    public static RiskDashboardItemResponse toRiskDashboardItemResponse(StudentRiskSnapshot snapshot) {
        boolean isStale = snapshot.getCalculatedAt() == null
                || snapshot.getCalculatedAt().isBefore(LocalDateTime.now().minusHours(24));
        return new RiskDashboardItemResponse(
                snapshot.getId(),
                snapshot.getStudent().getId(),
                snapshot.getStudent().getFullName().trim(),
                snapshot.getClassEntity() != null ? snapshot.getClassEntity().getId() : null,
                snapshot.getClassEntity() != null ? snapshot.getClassEntity().getClassName() : null,
                snapshot.getGradeLevel(),
                snapshot.getRiskLevel(),
                snapshot.getRiskScore(),
                snapshot.getAttendanceRate(),
                snapshot.getMissingHomeworkCount(),
                snapshot.getGradeAverage(),
                snapshot.getSnapshotStatus(),
                snapshot.getReasonSummary(),
                snapshot.getRecommendedAction(),
                snapshot.getIsPlaceholder(),
                isStale,
                snapshot.getCalculatedAt(),
                topIndicators(snapshot.getIndicatorSnapshots())
        );
    }

    public static ParentMessageDraftResponse toDraftResponse(ParentMessageDraft draft, User actor) {
        boolean canEdit = draft.getDraftStatus() == DraftStatus.READY_FOR_REVIEW
                && actor != null
                && (actor.isAdmin() || actor.getId().equals(draft.getCreatedByUser().getId()) || actor.isTeacher());
        boolean canApprove = draft.getDraftStatus() == DraftStatus.READY_FOR_REVIEW
                && actor != null
                && actor.isTeacher()
                && !actor.isAdmin();
        boolean canRetryGeneration = draft.getDraftStatus() == DraftStatus.GENERATION_FAILED
                && actor != null
                && (actor.isAdmin() || actor.isTeacher());

        return new ParentMessageDraftResponse(
                draft.getId(),
                draft.getStudent().getId(),
                draft.getStudent().getFullName().trim(),
                draft.getParentUser().getId(),
                draft.getParentUser().getFullName().trim(),
                draft.getRiskSnapshot() != null ? draft.getRiskSnapshot().getId() : null,
                draft.getCreatedByUser().getId(),
                draft.getCreatedByUser().getFullName().trim(),
                draft.getDraftStatus(),
                draft.getChannel(),
                draft.getIssueType(),
                draft.getTeacherNote(),
                draft.getGoalLabel(),
                draft.getGeneratedSubject(),
                draft.getGeneratedMessageBody(),
                draft.getCurrentSubject(),
                draft.getCurrentMessageBody(),
                draft.getToneLabel(),
                draft.getLanguageCode(),
                draft.getGenerationSource(),
                draft.getGenerationProvider(),
                draft.getGenerationModel(),
                draft.getProviderRequestId(),
                draft.getGenerationPromptVersion(),
                draft.getGenerationInputRedactedJson(),
                draft.getGenerationOutputRedactedJson(),
                draft.getGenerationErrorCode(),
                draft.getGenerationErrorMessage(),
                draft.getIsPlaceholder(),
                draft.getLastEditedByUser() != null ? draft.getLastEditedByUser().getId() : null,
                fullName(draft.getLastEditedByUser()),
                draft.getGeneratedAt(),
                draft.getApprovedByUser() != null ? draft.getApprovedByUser().getId() : null,
                fullName(draft.getApprovedByUser()),
                draft.getApprovedAt(),
                draft.getRejectedByUser() != null ? draft.getRejectedByUser().getId() : null,
                fullName(draft.getRejectedByUser()),
                draft.getRejectedAt(),
                draft.getRejectionReason(),
                draft.getSentAt(),
                draft.getLastEditedAt(),
                draft.getCreatedAt(),
                draft.getUpdatedAt(),
                canEdit,
                canApprove,
                canRetryGeneration
        );
    }

    public static AiAuditLogResponse toAuditResponse(AiAuditLog log) {
        return new AiAuditLogResponse(
                log.getId(),
                log.getEventType(),
                log.getEntityType(),
                log.getEntityId(),
                log.getActorUser().getId(),
                log.getActorUser().getFullName().trim(),
                log.getTargetStudent() != null ? log.getTargetStudent().getId() : null,
                fullName(log.getTargetStudent()),
                log.getTargetParentUser() != null ? log.getTargetParentUser().getId() : null,
                fullName(log.getTargetParentUser()),
                log.getRequestId(),
                log.getCorrelationId(),
                log.getActionStatus(),
                log.getReasonCode(),
                log.getProviderName(),
                log.getProviderModel(),
                log.getDetailsJson(),
                log.getOldValueJson(),
                log.getNewValueJson(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getCreatedAt()
        );
    }

    public static AnalyticsSummaryListItemResponse toAnalyticsSummaryListItemResponse(AnalyticsSummary summary, boolean isStale) {
        return new AnalyticsSummaryListItemResponse(
                summary.getId(),
                summary.getSummaryType(),
                summary.getScopeType(),
                summary.getScopeKey(),
                summary.getScopeLabel(),
                summary.getPeriodStart(),
                summary.getPeriodEnd(),
                summary.getStatus(),
                summary.getIsPlaceholder(),
                isStale,
                summary.getGeneratedAt(),
                summary.getStaleAfter(),
                summary.getHeadline()
        );
    }

    public static AnalyticsSummaryResponse toAnalyticsSummaryResponse(
            AnalyticsSummary summary,
            boolean isStale,
            AnalyticsSummarySections sections
    ) {
        return new AnalyticsSummaryResponse(
                summary.getId(),
                summary.getSummaryType(),
                summary.getScopeType(),
                summary.getScopeKey(),
                summary.getScopeLabel(),
                summary.getGeneratedForRole(),
                summary.getPeriodStart(),
                summary.getPeriodEnd(),
                summary.getComparisonPeriodStart(),
                summary.getComparisonPeriodEnd(),
                summary.getStatus(),
                summary.getIsPlaceholder(),
                isStale,
                summary.getGeneratedAt(),
                summary.getStaleAfter(),
                summary.getProviderName(),
                summary.getProviderModel(),
                summary.getPromptVersion(),
                summary.getHeadline(),
                summary.getOverallSummaryText(),
                sections.keyObservations(),
                sections.watchAreas(),
                sections.recommendedActions(),
                sections.confidenceNote(),
                summary.getInputRedactedJson(),
                summary.getSummaryJson(),
                summary.getGenerationErrorCode(),
                summary.getGenerationErrorMessage()
        );
    }

    public static List<RiskIndicatorResponse> toIndicatorResponses(List<StudentRiskIndicatorSnapshot> indicators) {
        if (indicators == null) {
            return List.of();
        }
        return indicators.stream()
                .sorted(Comparator.comparing(StudentRiskIndicatorSnapshot::getId))
                .map(indicator -> new RiskIndicatorResponse(
                        indicator.getIndicatorCode(),
                        indicator.getRawValue(),
                        indicator.getNormalizedRiskValue(),
                        indicator.getWeight(),
                        indicator.getWeightedContribution(),
                        indicator.getDataPointsCount(),
                        indicator.getIsMissingData(),
                        indicator.getDetailsJson()
                ))
                .toList();
    }

    private static List<String> topIndicators(List<StudentRiskIndicatorSnapshot> indicators) {
        if (indicators == null) {
            return List.of();
        }
        return indicators.stream()
                .sorted(Comparator.comparing(StudentRiskIndicatorSnapshot::getWeightedContribution).reversed())
                .limit(2)
                .map(indicator -> indicator.getIndicatorCode().name())
                .toList();
    }

    private static String fullName(User user) {
        return user == null ? null : user.getFullName().trim();
    }

    public static List<String> toStringList(com.fasterxml.jackson.databind.JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        node.forEach(item -> {
            String value = item.asText("");
            if (!value.isBlank()) {
                values.add(value.trim());
            }
        });
        return values;
    }

    public record AnalyticsSummarySections(
            List<String> keyObservations,
            List<String> watchAreas,
            List<String> recommendedActions,
            String confidenceNote
    ) {
    }
}
