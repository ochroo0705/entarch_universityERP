package com.edusys.backend.ai.service.summary;

import com.edusys.backend.ai.audit.AiAuditContext;
import com.edusys.backend.ai.dto.AiAuditLogQueryRequest;
import com.edusys.backend.ai.dto.AiAuditLogResponse;
import com.edusys.backend.ai.dto.summary.AnalyticsSummaryBundleResponse;
import com.edusys.backend.ai.dto.summary.AnalyticsSummaryGenerateRequest;
import com.edusys.backend.ai.dto.summary.AnalyticsSummaryListItemResponse;
import com.edusys.backend.ai.dto.summary.AnalyticsSummaryQueryRequest;
import com.edusys.backend.ai.dto.summary.AnalyticsSummaryResponse;
import com.edusys.backend.ai.mapper.AiMapper;
import com.edusys.backend.ai.model.*;
import com.edusys.backend.ai.repository.AnalyticsSummaryRepository;
import com.edusys.backend.ai.repository.AnalyticsSummaryRequestRepository;
import com.edusys.backend.ai.service.AiAuditService;
import com.edusys.backend.ai.service.provider.AnalyticsSummaryGenerationException;
import com.edusys.backend.ai.validation.AiAccessService;
import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsSummaryWorkflowService {

    private final AiAccessService aiAccessService;
    private final AnalyticsAggregateQueryService analyticsAggregateQueryService;
    private final AnalyticsSummaryInputBuilder analyticsSummaryInputBuilder;
    private final AnalyticsSummaryGenerationService analyticsSummaryGenerationService;
    private final AnalyticsSummaryCacheService analyticsSummaryCacheService;
    private final AnalyticsSummaryRepository analyticsSummaryRepository;
    private final AnalyticsSummaryRequestRepository analyticsSummaryRequestRepository;
    private final AiAuditService aiAuditService;
    private final ObjectMapper objectMapper;

    public AnalyticsSummaryWorkflowService(
            AiAccessService aiAccessService,
            AnalyticsAggregateQueryService analyticsAggregateQueryService,
            AnalyticsSummaryInputBuilder analyticsSummaryInputBuilder,
            AnalyticsSummaryGenerationService analyticsSummaryGenerationService,
            AnalyticsSummaryCacheService analyticsSummaryCacheService,
            AnalyticsSummaryRepository analyticsSummaryRepository,
            AnalyticsSummaryRequestRepository analyticsSummaryRequestRepository,
            AiAuditService aiAuditService,
            ObjectMapper objectMapper
    ) {
        this.aiAccessService = aiAccessService;
        this.analyticsAggregateQueryService = analyticsAggregateQueryService;
        this.analyticsSummaryInputBuilder = analyticsSummaryInputBuilder;
        this.analyticsSummaryGenerationService = analyticsSummaryGenerationService;
        this.analyticsSummaryCacheService = analyticsSummaryCacheService;
        this.analyticsSummaryRepository = analyticsSummaryRepository;
        this.analyticsSummaryRequestRepository = analyticsSummaryRequestRepository;
        this.aiAuditService = aiAuditService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AnalyticsSummaryResponse generateSummary(AnalyticsSummaryGenerateRequest request, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        LocalDate periodEnd = request.periodEnd() != null ? request.periodEnd() : LocalDate.now();
        LocalDate periodStart = request.periodStart() != null ? request.periodStart() : periodEnd.minusDays(30);
        String languageCode = normalizeLanguage(request.languageCode());

        validateScope(actor, request.summaryType(), request.classId(), request.gradeLevel());
        var scopeData = analyticsAggregateQueryService.buildScopeData(actor, request.summaryType(), request.classId(), request.gradeLevel(), periodStart, periodEnd);
        var prepared = analyticsSummaryInputBuilder.build(scopeData, languageCode);

        AnalyticsSummaryRequest summaryRequest = createRequest(actor, scopeData, Boolean.TRUE.equals(request.forceRefresh()));
        aiAuditService.record(
                AiAuditEventType.ANALYTICS_SUMMARY_REQUESTED,
                AiEntityType.ANALYTICS_SUMMARY,
                null,
                actor,
                null,
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                prepared.redactedPayloadJson(),
                null,
                null,
                auditContext
        );

        if (!Boolean.TRUE.equals(request.forceRefresh())) {
            var reusable = analyticsSummaryCacheService.findReusableSummary(prepared);
            if (reusable.isPresent()) {
                summaryRequest.setAnalyticsSummary(reusable.get());
                summaryRequest.setRequestStatus(SummaryRequestStatus.SKIPPED_REUSED);
                summaryRequest.setInputFingerprint(prepared.fingerprint());
                summaryRequest.setCompletedAt(LocalDateTime.now());
                analyticsSummaryRequestRepository.save(summaryRequest);
                return touchAndMap(reusable.get());
            }
        }

        AnalyticsSummary summary = new AnalyticsSummary();
        summary.setSummaryType(scopeData.summaryType());
        summary.setScopeType(scopeData.scopeType());
        summary.setScopeKey(scopeData.scopeKey());
        summary.setScopeLabel(scopeData.scopeLabel());
        summary.setRequestedByUser(actor);
        summary.setGeneratedForRole(actor.isAdmin() ? "ADMIN" : "TEACHER");
        summary.setPeriodStart(scopeData.periodStart());
        summary.setPeriodEnd(scopeData.periodEnd());
        summary.setComparisonPeriodStart(scopeData.comparisonPeriodStart());
        summary.setComparisonPeriodEnd(scopeData.comparisonPeriodEnd());
        summary.setInputFingerprint(prepared.fingerprint());
        summary.setInputRedactedJson(prepared.redactedPayloadJson());
        summary.setPromptVersion(prepared.input().promptVersion());
        summary.setStatus(SummaryStatus.GENERATING);
        summary.setIsPlaceholder(false);
        summary = analyticsSummaryRepository.save(summary);
        summaryRequest.setAnalyticsSummary(summary);
        summaryRequest.setInputFingerprint(prepared.fingerprint());
        summaryRequest.setRequestStatus(SummaryRequestStatus.RUNNING);
        analyticsSummaryRequestRepository.save(summaryRequest);

        aiAuditService.record(
                AiAuditEventType.ANALYTICS_SUMMARY_GENERATION_STARTED,
                AiEntityType.ANALYTICS_SUMMARY,
                summary.getId(),
                actor,
                null,
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                prepared.redactedPayloadJson(),
                null,
                null,
                auditContext
        );

        try {
            AnalyticsSummaryGenerationResult result = analyticsSummaryGenerationService.generate(prepared.input());
            summary.setHeadline(result.headline());
            summary.setOverallSummaryText(result.overallSummary());
            summary.setSummaryJson(result.outputRedactedJson());
            summary.setProviderName(result.providerName());
            summary.setProviderModel(result.providerModel());
            summary.setProviderRequestId(result.providerRequestId());
            summary.setGeneratedAt(LocalDateTime.now());
            summary.setStaleAfter(LocalDateTime.now().plusHours(24));
            summary.setStatus(SummaryStatus.READY);
            summary.setIsPlaceholder(result.placeholder());
            summary.setGenerationErrorCode(null);
            summary.setGenerationErrorMessage(null);
            AnalyticsSummary saved = analyticsSummaryRepository.save(summary);

            summaryRequest.setRequestStatus(SummaryRequestStatus.SUCCESS);
            summaryRequest.setCompletedAt(LocalDateTime.now());
            analyticsSummaryRequestRepository.save(summaryRequest);

            aiAuditService.record(
                    AiAuditEventType.ANALYTICS_SUMMARY_GENERATED,
                    AiEntityType.ANALYTICS_SUMMARY,
                    saved.getId(),
                    actor,
                    null,
                    null,
                    AiAuditActionStatus.SUCCESS,
                    null,
                    prepared.redactedPayloadJson(),
                    null,
                    saved.getSummaryJson(),
                    saved.getProviderName(),
                    saved.getProviderModel(),
                    saved.getProviderRequestId(),
                    auditContext
            );
            return touchAndMap(saved);
        } catch (AnalyticsSummaryGenerationException exception) {
            summary.setStatus(SummaryStatus.FAILED);
            summary.setGenerationErrorCode(exception.getErrorCode());
            summary.setGenerationErrorMessage(exception.getMessage());
            summary.setProviderName(exception.getProviderName());
            AnalyticsSummary saved = analyticsSummaryRepository.save(summary);

            summaryRequest.setRequestStatus(SummaryRequestStatus.FAILED);
            summaryRequest.setErrorCode(exception.getErrorCode());
            summaryRequest.setErrorMessage(exception.getMessage());
            summaryRequest.setCompletedAt(LocalDateTime.now());
            analyticsSummaryRequestRepository.save(summaryRequest);

            aiAuditService.record(
                    AiAuditEventType.ANALYTICS_SUMMARY_GENERATION_FAILED,
                    AiEntityType.ANALYTICS_SUMMARY,
                    saved.getId(),
                    actor,
                    null,
                    null,
                    AiAuditActionStatus.FAILURE,
                    exception.getErrorCode(),
                    prepared.redactedPayloadJson(),
                    null,
                    aiAuditService.toJson(Map.of("errorCode", exception.getErrorCode(), "message", exception.getMessage())),
                    exception.getProviderName(),
                    saved.getProviderModel(),
                    saved.getProviderRequestId(),
                    auditContext
            );
            return AiMapper.toAnalyticsSummaryResponse(saved, analyticsSummaryCacheService.isStale(saved), readSummarySections(saved.getSummaryJson()));
        }
    }

    public AnalyticsSummaryResponse getSummary(Long id, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        AnalyticsSummary summary = requireSummary(id);
        validateStoredSummaryAccess(actor, summary);
        summary.setLastViewedAt(LocalDateTime.now());
        analyticsSummaryRepository.save(summary);
        aiAuditService.record(
                AiAuditEventType.ANALYTICS_SUMMARY_VIEWED,
                AiEntityType.ANALYTICS_SUMMARY,
                summary.getId(),
                actor,
                null,
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                aiAuditService.toJson(Map.of("summaryType", summary.getSummaryType().name(), "scopeKey", summary.getScopeKey())),
                null,
                null,
                summary.getProviderName(),
                summary.getProviderModel(),
                summary.getProviderRequestId(),
                auditContext
        );
        return AiMapper.toAnalyticsSummaryResponse(summary, analyticsSummaryCacheService.isStale(summary), readSummarySections(summary.getSummaryJson()));
    }

    public List<AnalyticsSummaryListItemResponse> listSummaries(AnalyticsSummaryQueryRequest query, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        List<AnalyticsSummaryListItemResponse> items = analyticsSummaryRepository.findAll().stream()
                .filter(summary -> canAccessStoredSummary(actor, summary))
                .filter(summary -> query.getSummaryType() == null || summary.getSummaryType() == query.getSummaryType())
                .filter(summary -> query.getClassId() == null || summary.getScopeKey().equals("CLASS:" + query.getClassId()))
                .filter(summary -> query.getGradeLevel() == null || summary.getScopeKey().equals("GRADE:" + query.getGradeLevel()))
                .sorted(Comparator.comparing(AnalyticsSummary::getGeneratedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(summary -> AiMapper.toAnalyticsSummaryListItemResponse(summary, analyticsSummaryCacheService.isStale(summary)))
                .toList();
        aiAuditService.record(
                AiAuditEventType.ANALYTICS_SUMMARY_LIST_VIEWED,
                AiEntityType.ANALYTICS_SUMMARY,
                null,
                actor,
                null,
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                aiAuditService.toJson(Map.of("summaryType", query.getSummaryType() == null ? "" : query.getSummaryType().name())),
                null,
                null,
                auditContext
        );
        return items;
    }

    public AnalyticsSummaryBundleResponse getTeacherOverview(Long classId, String languageCode, AiAuditContext auditContext) {
        SummaryType type = classId != null ? SummaryType.TEACHER_CLASS_OVERVIEW : SummaryType.TEACHER_RISK_OVERVIEW;
        return new AnalyticsSummaryBundleResponse(generateSummary(new AnalyticsSummaryGenerateRequest(type, classId, null, null, null, false, languageCode), auditContext));
    }

    public AnalyticsSummaryBundleResponse getAdminOverview(Integer gradeLevel, String languageCode, AiAuditContext auditContext) {
        SummaryType type = gradeLevel != null ? SummaryType.ADMIN_GRADE_OVERVIEW : SummaryType.ADMIN_SCHOOL_OVERVIEW;
        return new AnalyticsSummaryBundleResponse(generateSummary(new AnalyticsSummaryGenerateRequest(type, null, gradeLevel, null, null, false, languageCode), auditContext));
    }

    @Transactional
    public AnalyticsSummaryResponse refreshSummary(Long id, String languageCode, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        AnalyticsSummary existing = requireSummary(id);
        validateStoredSummaryAccess(actor, existing);
        Long classId = existing.getSummaryType() == SummaryType.TEACHER_CLASS_OVERVIEW ? Long.valueOf(existing.getScopeKey().replace("CLASS:", "")) : null;
        Integer gradeLevel = existing.getSummaryType() == SummaryType.ADMIN_GRADE_OVERVIEW ? Integer.valueOf(existing.getScopeKey().replace("GRADE:", "")) : null;
        AnalyticsSummaryResponse refreshed = generateSummary(
                new AnalyticsSummaryGenerateRequest(existing.getSummaryType(), classId, gradeLevel, existing.getPeriodStart(), existing.getPeriodEnd(), true, normalizeLanguage(languageCode)),
                auditContext
        );
        aiAuditService.record(
                AiAuditEventType.ANALYTICS_SUMMARY_REFRESHED,
                AiEntityType.ANALYTICS_SUMMARY,
                refreshed.id(),
                actor,
                null,
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                aiAuditService.toJson(Map.of("sourceSummaryId", id)),
                null,
                null,
                auditContext
        );
        return refreshed;
    }

    public PaginatedResponseDTO<AiAuditLogResponse> getSummaryAuditLogs(Long id, AiAuditLogQueryRequest query, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        AnalyticsSummary summary = requireSummary(id);
        validateStoredSummaryAccess(actor, summary);
        AiAuditLogQueryRequest effective = new AiAuditLogQueryRequest();
        effective.setEntityType(AiEntityType.ANALYTICS_SUMMARY.name());
        effective.setEntityId(id);
        effective.setEventType(query.getEventType());
        effective.setPage(query.getPage());
        effective.setPageSize(query.getPageSize());
        effective.setFrom(query.getFrom());
        effective.setTo(query.getTo());

        aiAuditService.record(
                AiAuditEventType.ANALYTICS_SUMMARY_AUDIT_VIEWED,
                AiEntityType.ANALYTICS_SUMMARY,
                id,
                actor,
                null,
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                aiAuditService.toJson(Map.of("summaryId", id)),
                null,
                null,
                auditContext
        );
        return aiAuditService.query(effective);
    }

    @Transactional
    public AnalyticsSummaryResponse refreshSchoolWideSummary(AiAuditContext auditContext) {
        return generateSummary(
                new AnalyticsSummaryGenerateRequest(SummaryType.ADMIN_SCHOOL_OVERVIEW, null, null, LocalDate.now().minusDays(30), LocalDate.now(), true, "mn"),
                auditContext
        );
    }

    private AnalyticsSummaryRequest createRequest(User actor, AnalyticsAggregateQueryService.SummaryScopeData scopeData, boolean forceRefresh) {
        AnalyticsSummaryRequest request = new AnalyticsSummaryRequest();
        request.setSummaryType(scopeData.summaryType());
        request.setScopeType(scopeData.scopeType());
        request.setScopeKey(scopeData.scopeKey());
        request.setRequestMode(forceRefresh ? SummaryRequestMode.MANUAL : SummaryRequestMode.VIEW_TRIGGERED);
        request.setRequestedByUser(actor);
        request.setForceRefresh(forceRefresh);
        request.setRequestStatus(SummaryRequestStatus.QUEUED);
        return analyticsSummaryRequestRepository.save(request);
    }

    private void validateScope(User actor, SummaryType summaryType, Long classId, Integer gradeLevel) {
        switch (summaryType) {
            case TEACHER_CLASS_OVERVIEW -> aiAccessService.ensureCanAccessClassSummary(actor, classId);
            case TEACHER_RISK_OVERVIEW -> aiAccessService.ensureCanAccessTeacherOverview(actor);
            case ADMIN_GRADE_OVERVIEW -> aiAccessService.ensureCanAccessGradeSummary(actor, gradeLevel);
            case ADMIN_SCHOOL_OVERVIEW -> aiAccessService.ensureCanAccessSchoolSummary(actor);
        }
    }

    private boolean canAccessStoredSummary(User actor, AnalyticsSummary summary) {
        try {
            validateStoredSummaryAccess(actor, summary);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void validateStoredSummaryAccess(User actor, AnalyticsSummary summary) {
        switch (summary.getSummaryType()) {
            case TEACHER_CLASS_OVERVIEW -> aiAccessService.ensureCanAccessClassSummary(actor, Long.valueOf(summary.getScopeKey().replace("CLASS:", "")));
            case TEACHER_RISK_OVERVIEW -> {
                if (actor.isAdmin()) {
                    return;
                }
                if (!summary.getScopeKey().equals("TEACHER:" + actor.getId())) {
                    throw new org.springframework.security.access.AccessDeniedException("You do not have access to this teacher overview");
                }
                aiAccessService.ensureCanAccessTeacherOverview(actor);
            }
            case ADMIN_GRADE_OVERVIEW -> aiAccessService.ensureCanAccessGradeSummary(actor, Integer.valueOf(summary.getScopeKey().replace("GRADE:", "")));
            case ADMIN_SCHOOL_OVERVIEW -> aiAccessService.ensureCanAccessSchoolSummary(actor);
        }
    }

    private AnalyticsSummary requireSummary(Long id) {
        return analyticsSummaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analytics summary not found"));
    }

    private AnalyticsSummaryResponse touchAndMap(AnalyticsSummary summary) {
        summary.setLastViewedAt(LocalDateTime.now());
        analyticsSummaryRepository.save(summary);
        return AiMapper.toAnalyticsSummaryResponse(summary, analyticsSummaryCacheService.isStale(summary), readSummarySections(summary.getSummaryJson()));
    }

    private AiMapper.AnalyticsSummarySections readSummarySections(String summaryJson) {
        if (summaryJson == null || summaryJson.isBlank()) {
            return new AiMapper.AnalyticsSummarySections(List.of(), List.of(), List.of(), null);
        }
        try {
            var jsonNode = objectMapper.readTree(summaryJson);
            return new AiMapper.AnalyticsSummarySections(
                    AiMapper.toStringList(jsonNode.path("keyObservations")),
                    AiMapper.toStringList(jsonNode.path("watchAreas")),
                    AiMapper.toStringList(jsonNode.path("recommendedActions")),
                    jsonNode.path("confidenceNote").asText(null)
            );
        } catch (Exception ignored) {
            return new AiMapper.AnalyticsSummarySections(List.of(), List.of(), List.of(), null);
        }
    }

    private String normalizeLanguage(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return "mn";
        }
        String normalized = languageCode.trim().toLowerCase();
        return normalized.startsWith("en") ? "en" : "mn";
    }
}
