package com.edusys.backend.ai.service.draft;

import com.edusys.backend.ai.audit.AiAuditContext;
import com.edusys.backend.ai.dto.*;
import com.edusys.backend.ai.mapper.AiMapper;
import com.edusys.backend.ai.model.*;
import com.edusys.backend.ai.repository.ParentMessageDraftRepository;
import com.edusys.backend.ai.repository.StudentRiskSnapshotRepository;
import com.edusys.backend.ai.service.AiAuditService;
import com.edusys.backend.ai.service.provider.ParentMessageGenerationException;
import com.edusys.backend.ai.service.provider.ParentMessageGenerationService;
import com.edusys.backend.ai.validation.AiAccessService;
import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.ParentStudentRepository;
import com.edusys.backend.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParentMessageDraftWorkflowService {

    private final ParentMessageDraftRepository parentMessageDraftRepository;
    private final StudentRiskSnapshotRepository studentRiskSnapshotRepository;
    private final UserRepository userRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final AiAccessService aiAccessService;
    private final ParentMessageContextService parentMessageContextService;
    private final ParentMessageGenerationService parentMessageGenerationService;
    private final AiAuditService aiAuditService;

    public ParentMessageDraftWorkflowService(
            ParentMessageDraftRepository parentMessageDraftRepository,
            StudentRiskSnapshotRepository studentRiskSnapshotRepository,
            UserRepository userRepository,
            ParentStudentRepository parentStudentRepository,
            AiAccessService aiAccessService,
            ParentMessageContextService parentMessageContextService,
            ParentMessageGenerationService parentMessageGenerationService,
            AiAuditService aiAuditService
    ) {
        this.parentMessageDraftRepository = parentMessageDraftRepository;
        this.studentRiskSnapshotRepository = studentRiskSnapshotRepository;
        this.userRepository = userRepository;
        this.parentStudentRepository = parentStudentRepository;
        this.aiAccessService = aiAccessService;
        this.parentMessageContextService = parentMessageContextService;
        this.parentMessageGenerationService = parentMessageGenerationService;
        this.aiAuditService = aiAuditService;
    }

    public List<ParentMessageDraftResponse> listDrafts(ParentMessageDraftQueryRequest query, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        Specification<ParentMessageDraft> specification = Specification.where(null);

        if (!actor.isAdmin()) {
            List<Long> accessibleStudentIds = aiAccessService.getAccessibleStudentIds(actor);
            specification = specification.and((root, cq, cb) -> root.get("student").get("id").in(accessibleStudentIds));
        }

        if (query.getStudentId() != null) {
            aiAccessService.ensureCanAccessStudent(actor, query.getStudentId());
            specification = specification.and((root, cq, cb) -> cb.equal(root.get("student").get("id"), query.getStudentId()));
        }
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            DraftStatus status = DraftStatus.valueOf(query.getStatus().toUpperCase());
            specification = specification.and((root, cq, cb) -> cb.equal(root.get("draftStatus"), status));
        }
        if (query.getTeacherId() != null) {
            specification = specification.and((root, cq, cb) -> cb.equal(root.get("createdByUser").get("id"), query.getTeacherId()));
        }
        if (query.getProvider() != null && !query.getProvider().isBlank()) {
            specification = specification.and((root, cq, cb) -> cb.equal(root.get("generationProvider"), query.getProvider()));
        }
        if (query.getFrom() != null) {
            specification = specification.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), query.getFrom()));
        }
        if (query.getTo() != null) {
            specification = specification.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), query.getTo()));
        }

        List<ParentMessageDraftResponse> responses = parentMessageDraftRepository.findAll(
                        specification,
                        Sort.by(Sort.Direction.DESC, "createdAt", "id")
                ).stream()
                .map(draft -> AiMapper.toDraftResponse(draft, actor))
                .toList();

        aiAuditService.record(
                AiAuditEventType.MESSAGE_DRAFT_LIST_VIEWED,
                AiEntityType.PARENT_MESSAGE_DRAFT,
                null,
                actor,
                null,
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                safeJson(nullableMap(
                        "studentId", query.getStudentId(),
                        "status", query.getStatus(),
                        "teacherId", query.getTeacherId(),
                        "provider", query.getProvider()
                )),
                null,
                null,
                null,
                null,
                null,
                auditContext
        );

        return responses;
    }

    public ParentMessageDraftResponse getDraft(Long id, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        ParentMessageDraft draft = requireDraft(id);
        aiAccessService.ensureCanAccessStudent(actor, draft.getStudent().getId());

        aiAuditService.record(
                AiAuditEventType.MESSAGE_DRAFT_VIEWED,
                AiEntityType.PARENT_MESSAGE_DRAFT,
                draft.getId(),
                actor,
                draft.getStudent(),
                draft.getParentUser(),
                AiAuditActionStatus.SUCCESS,
                null,
                safeJson(nullableMap("draftId", draft.getId(), "status", draft.getDraftStatus().name())),
                null,
                null,
                draft.getGenerationProvider(),
                draft.getGenerationModel(),
                draft.getProviderRequestId(),
                auditContext
        );
        return AiMapper.toDraftResponse(draft, actor);
    }

    public ParentMessageDraftResponse createDraft(ParentMessageDraftCreateRequest request, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        User student = requireStudent(request.studentId());
        User parentUser = requireParent(request.parentUserId());
        aiAccessService.ensureCanAccessStudent(actor, student.getId());

        if (!parentStudentRepository.existsByParent_IdAndStudent_Id(parentUser.getId(), student.getId())) {
            throw new IllegalArgumentException("Parent is not linked to the student");
        }

        StudentRiskSnapshot riskSnapshot = requireRiskSnapshot(student.getId(), request.riskSnapshotId());

        ParentMessageDraft draft = new ParentMessageDraft();
        draft.setStudent(student);
        draft.setParentUser(parentUser);
        draft.setRiskSnapshot(riskSnapshot);
        draft.setCreatedByUser(actor);
        draft.setDraftStatus(DraftStatus.REQUESTED);
        draft.setChannel(parseChannel(request.channel()));
        draft.setIssueType(parseIssueType(request.issueType()));
        draft.setTeacherNote(sanitizeTeacherNote(request.teacherNote()));
        draft.setGoalLabel(request.goalLabel());
        draft.setToneLabel(defaultValue(request.toneLabel(), "supportive"));
        draft.setLanguageCode(defaultValue(request.languageCode(), "mn"));
        draft.setGenerationSource(actor.isAdmin() ? "ADMIN_REQUEST" : "TEACHER_REQUEST");
        draft.setCurrentSubject("Pending AI generation");
        draft.setCurrentMessageBody("Draft generation has been requested.");
        draft.setLastEditedByUser(actor);

        ParentMessageDraft saved = parentMessageDraftRepository.save(draft);

        aiAuditService.record(
                AiAuditEventType.MESSAGE_DRAFT_REQUESTED,
                AiEntityType.PARENT_MESSAGE_DRAFT,
                saved.getId(),
                actor,
                student,
                parentUser,
                AiAuditActionStatus.SUCCESS,
                null,
                safeJson(nullableMap(
                        "issueType", saved.getIssueType().name(),
                        "toneLabel", saved.getToneLabel(),
                        "languageCode", saved.getLanguageCode()
                )),
                null,
                null,
                null,
                null,
                null,
                auditContext
        );

        ParentMessageDraft generated = generateDraftContent(saved, actor, auditContext);
        return AiMapper.toDraftResponse(generated, actor);
    }

    public ParentMessageDraftResponse retryGeneration(Long id, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        ParentMessageDraft draft = requireDraft(id);
        aiAccessService.ensureCanAccessStudent(actor, draft.getStudent().getId());
        if (draft.getDraftStatus() != DraftStatus.GENERATION_FAILED) {
            throw new IllegalArgumentException("Only failed drafts can be retried");
        }

        aiAuditService.record(
                AiAuditEventType.MESSAGE_DRAFT_RETRY_REQUESTED,
                AiEntityType.PARENT_MESSAGE_DRAFT,
                draft.getId(),
                actor,
                draft.getStudent(),
                draft.getParentUser(),
                AiAuditActionStatus.SUCCESS,
                null,
                safeJson(nullableMap("draftStatus", draft.getDraftStatus().name())),
                null,
                null,
                draft.getGenerationProvider(),
                draft.getGenerationModel(),
                draft.getProviderRequestId(),
                auditContext
        );

        ParentMessageDraft generated = generateDraftContent(draft, actor, auditContext);
        return AiMapper.toDraftResponse(generated, actor);
    }

    public ParentMessageDraftResponse updateDraft(Long id, ParentMessageDraftUpdateRequest request, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        ParentMessageDraft draft = requireDraft(id);
        aiAccessService.ensureCanAccessStudent(actor, draft.getStudent().getId());
        if (draft.getDraftStatus() != DraftStatus.READY_FOR_REVIEW) {
            throw new IllegalArgumentException("Only drafts ready for review can be edited");
        }

        String oldJson = safeJson(nullableMap(
                "currentSubject", draft.getCurrentSubject(),
                "currentMessageBody", draft.getCurrentMessageBody()
        ));

        draft.setCurrentSubject(request.currentSubject().trim());
        draft.setCurrentMessageBody(request.currentMessageBody().trim());
        draft.setLastEditedAt(LocalDateTime.now());
        draft.setLastEditedByUser(actor);

        ParentMessageDraft saved = parentMessageDraftRepository.save(draft);
        aiAuditService.record(
                AiAuditEventType.MESSAGE_DRAFT_UPDATED,
                AiEntityType.PARENT_MESSAGE_DRAFT,
                saved.getId(),
                actor,
                saved.getStudent(),
                saved.getParentUser(),
                AiAuditActionStatus.SUCCESS,
                null,
                null,
                oldJson,
                safeJson(nullableMap(
                        "currentSubject", saved.getCurrentSubject(),
                        "currentMessageBody", saved.getCurrentMessageBody()
                )),
                saved.getGenerationProvider(),
                saved.getGenerationModel(),
                saved.getProviderRequestId(),
                auditContext
        );
        return AiMapper.toDraftResponse(saved, actor);
    }

    public ParentMessageDraftResponse approveDraft(Long id, DraftDecisionRequest request, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        ensureTeacherApprovalActor(actor);
        ParentMessageDraft draft = requireDraft(id);
        aiAccessService.ensureCanAccessStudent(actor, draft.getStudent().getId());
        if (draft.getDraftStatus() != DraftStatus.READY_FOR_REVIEW) {
            throw new IllegalArgumentException("Only drafts ready for review can be approved");
        }

        draft.setDraftStatus(DraftStatus.APPROVED);
        draft.setApprovedByUser(actor);
        draft.setApprovedAt(LocalDateTime.now());
        draft.setRejectedByUser(null);
        draft.setRejectedAt(null);
        draft.setRejectionReason(null);

        ParentMessageDraft saved = parentMessageDraftRepository.save(draft);
        aiAuditService.record(
                AiAuditEventType.MESSAGE_DRAFT_APPROVED,
                AiEntityType.PARENT_MESSAGE_DRAFT,
                saved.getId(),
                actor,
                saved.getStudent(),
                saved.getParentUser(),
                AiAuditActionStatus.SUCCESS,
                request.note(),
                null,
                null,
                safeJson(nullableMap("draftStatus", saved.getDraftStatus().name())),
                saved.getGenerationProvider(),
                saved.getGenerationModel(),
                saved.getProviderRequestId(),
                auditContext
        );
        return AiMapper.toDraftResponse(saved, actor);
    }

    public ParentMessageDraftResponse rejectDraft(Long id, DraftDecisionRequest request, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        ensureTeacherApprovalActor(actor);
        ParentMessageDraft draft = requireDraft(id);
        aiAccessService.ensureCanAccessStudent(actor, draft.getStudent().getId());
        if (draft.getDraftStatus() != DraftStatus.READY_FOR_REVIEW) {
            throw new IllegalArgumentException("Only drafts ready for review can be rejected");
        }

        draft.setDraftStatus(DraftStatus.REJECTED);
        draft.setRejectedByUser(actor);
        draft.setRejectedAt(LocalDateTime.now());
        draft.setRejectionReason(request.note());
        draft.setApprovedByUser(null);
        draft.setApprovedAt(null);

        ParentMessageDraft saved = parentMessageDraftRepository.save(draft);
        aiAuditService.record(
                AiAuditEventType.MESSAGE_DRAFT_REJECTED,
                AiEntityType.PARENT_MESSAGE_DRAFT,
                saved.getId(),
                actor,
                saved.getStudent(),
                saved.getParentUser(),
                AiAuditActionStatus.SUCCESS,
                request.note(),
                null,
                null,
                safeJson(nullableMap("draftStatus", saved.getDraftStatus().name())),
                saved.getGenerationProvider(),
                saved.getGenerationModel(),
                saved.getProviderRequestId(),
                auditContext
        );
        return AiMapper.toDraftResponse(saved, actor);
    }

    public PaginatedResponseDTO<AiAuditLogResponse> getDraftAuditLogs(Long id, AiAuditLogQueryRequest query, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        ParentMessageDraft draft = requireDraft(id);
        aiAccessService.ensureCanAccessStudent(actor, draft.getStudent().getId());

        AiAuditLogQueryRequest effective = new AiAuditLogQueryRequest();
        effective.setEntityType(AiEntityType.PARENT_MESSAGE_DRAFT.name());
        effective.setEntityId(id);
        effective.setEventType(query.getEventType());
        effective.setStudentId(query.getStudentId());
        effective.setPage(query.getPage());
        effective.setPageSize(query.getPageSize());
        effective.setFrom(query.getFrom());
        effective.setTo(query.getTo());

        aiAuditService.record(
                AiAuditEventType.MESSAGE_DRAFT_AUDIT_VIEWED,
                AiEntityType.PARENT_MESSAGE_DRAFT,
                id,
                actor,
                draft.getStudent(),
                draft.getParentUser(),
                AiAuditActionStatus.SUCCESS,
                null,
                safeJson(nullableMap("draftId", id)),
                null,
                null,
                draft.getGenerationProvider(),
                draft.getGenerationModel(),
                draft.getProviderRequestId(),
                auditContext
        );

        return aiAuditService.query(effective);
    }

    private ParentMessageDraft generateDraftContent(ParentMessageDraft draft, User actor, AiAuditContext auditContext) {
        draft.setDraftStatus(DraftStatus.GENERATING);
        draft.setGenerationErrorCode(null);
        draft.setGenerationErrorMessage(null);
        parentMessageDraftRepository.save(draft);

        ParentMessageGenerationInput input = parentMessageContextService.buildInput(draft);
        draft.setGenerationInputRedactedJson(input.redactedPayloadJson());
        draft.setGenerationPromptVersion(input.promptVersion());
        parentMessageDraftRepository.save(draft);

        aiAuditService.record(
                AiAuditEventType.MESSAGE_DRAFT_GENERATION_STARTED,
                AiEntityType.PARENT_MESSAGE_DRAFT,
                draft.getId(),
                actor,
                draft.getStudent(),
                draft.getParentUser(),
                AiAuditActionStatus.SUCCESS,
                null,
                input.redactedPayloadJson(),
                null,
                null,
                null,
                null,
                null,
                auditContext
        );

        try {
            ParentMessageGenerationResult result = parentMessageGenerationService.generate(input);
            draft.setDraftStatus(DraftStatus.READY_FOR_REVIEW);
            draft.setGeneratedSubject(result.subject());
            draft.setGeneratedMessageBody(result.messageBody());
            draft.setCurrentSubject(result.subject());
            draft.setCurrentMessageBody(result.messageBody());
            draft.setGenerationProvider(result.providerName());
            draft.setGenerationModel(result.providerModel());
            draft.setProviderRequestId(result.providerRequestId());
            draft.setGenerationOutputRedactedJson(result.outputRedactedJson());
            draft.setGeneratedAt(LocalDateTime.now());
            draft.setIsPlaceholder(result.placeholder());
            draft.setLastEditedAt(LocalDateTime.now());
            draft.setLastEditedByUser(actor);
            ParentMessageDraft saved = parentMessageDraftRepository.save(draft);

            aiAuditService.record(
                    AiAuditEventType.MESSAGE_DRAFT_GENERATED,
                    AiEntityType.PARENT_MESSAGE_DRAFT,
                    saved.getId(),
                    actor,
                    saved.getStudent(),
                    saved.getParentUser(),
                    AiAuditActionStatus.SUCCESS,
                    null,
                    input.redactedPayloadJson(),
                    null,
                    result.outputRedactedJson(),
                    result.providerName(),
                    result.providerModel(),
                    result.providerRequestId(),
                    auditContext
            );
            return saved;
        } catch (ParentMessageGenerationException exception) {
            draft.setDraftStatus(DraftStatus.GENERATION_FAILED);
            draft.setGenerationProvider(exception.getProviderName());
            draft.setGenerationErrorCode(exception.getErrorCode());
            draft.setGenerationErrorMessage(exception.getMessage());
            ParentMessageDraft saved = parentMessageDraftRepository.save(draft);

            aiAuditService.record(
                    AiAuditEventType.MESSAGE_DRAFT_GENERATION_FAILED,
                    AiEntityType.PARENT_MESSAGE_DRAFT,
                    saved.getId(),
                    actor,
                    saved.getStudent(),
                    saved.getParentUser(),
                    AiAuditActionStatus.FAILURE,
                    exception.getErrorCode(),
                    input.redactedPayloadJson(),
                    null,
                    safeJson(nullableMap(
                            "errorCode", exception.getErrorCode(),
                            "message", exception.getMessage()
                    )),
                    exception.getProviderName(),
                    draft.getGenerationModel(),
                    draft.getProviderRequestId(),
                    auditContext
            );
            return saved;
        }
    }

    private ParentMessageDraft requireDraft(Long id) {
        return parentMessageDraftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Draft not found"));
    }

    private User requireStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!student.isStudent()) {
            throw new IllegalArgumentException("Provided user is not a student");
        }
        return student;
    }

    private User requireParent(Long parentUserId) {
        User parent = userRepository.findById(parentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
        if (!parent.isParent()) {
            throw new IllegalArgumentException("Provided user is not a parent");
        }
        return parent;
    }

    private StudentRiskSnapshot requireRiskSnapshot(Long studentId, Long riskSnapshotId) {
        if (riskSnapshotId == null) {
            return null;
        }
        StudentRiskSnapshot riskSnapshot = studentRiskSnapshotRepository.findById(riskSnapshotId)
                .orElseThrow(() -> new ResourceNotFoundException("Risk snapshot not found"));
        if (!riskSnapshot.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Risk snapshot does not belong to the selected student");
        }
        return riskSnapshot;
    }

    private DraftChannel parseChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return DraftChannel.EMAIL;
        }
        return DraftChannel.valueOf(channel.toUpperCase());
    }

    private IssueType parseIssueType(String issueType) {
        return IssueType.valueOf(issueType.toUpperCase());
    }

    private String sanitizeTeacherNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }
        String normalized = note.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        return normalized.length() > 1000 ? normalized.substring(0, 1000) : normalized;
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private void ensureTeacherApprovalActor(User actor) {
        if (!actor.isTeacher() || actor.isAdmin()) {
            throw new AccessDeniedException("Only teachers can approve or reject parent message drafts");
        }
    }

    private String safeJson(Map<String, ?> source) {
        Map<String, Object> filtered = new LinkedHashMap<>();
        source.forEach((key, value) -> filtered.put(key, value == null ? "" : value));
        return aiAuditService.toJson(filtered);
    }

    private Map<String, Object> nullableMap(Object... values) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            payload.put(String.valueOf(values[index]), values[index + 1]);
        }
        return payload;
    }
}
