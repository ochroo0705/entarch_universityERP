package com.edusys.backend.ai.service;

import com.edusys.backend.ai.audit.AiAuditContext;
import com.edusys.backend.ai.dto.AiAuditLogQueryRequest;
import com.edusys.backend.ai.mapper.AiMapper;
import com.edusys.backend.ai.model.*;
import com.edusys.backend.ai.repository.AiAuditLogRepository;
import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiAuditService {

    private final AiAuditLogRepository aiAuditLogRepository;
    private final ObjectMapper objectMapper;

    public AiAuditService(AiAuditLogRepository aiAuditLogRepository, ObjectMapper objectMapper) {
        this.aiAuditLogRepository = aiAuditLogRepository;
        this.objectMapper = objectMapper;
    }

    public void record(
            AiAuditEventType eventType,
            AiEntityType entityType,
            Long entityId,
            User actor,
            User targetStudent,
            User targetParentUser,
            AiAuditActionStatus actionStatus,
            String reasonCode,
            String detailsJson,
            String oldValueJson,
            String newValueJson,
            AiAuditContext context
    ) {
        record(
                eventType,
                entityType,
                entityId,
                actor,
                targetStudent,
                targetParentUser,
                actionStatus,
                reasonCode,
                detailsJson,
                oldValueJson,
                newValueJson,
                null,
                null,
                null,
                context
        );
    }

    public void record(
            AiAuditEventType eventType,
            AiEntityType entityType,
            Long entityId,
            User actor,
            User targetStudent,
            User targetParentUser,
            AiAuditActionStatus actionStatus,
            String reasonCode,
            String detailsJson,
            String oldValueJson,
            String newValueJson,
            String providerName,
            String providerModel,
            String correlationId,
            AiAuditContext context
    ) {
        AiAuditLog log = new AiAuditLog();
        log.setEventType(eventType);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setActorUser(actor);
        log.setTargetStudent(targetStudent);
        log.setTargetParentUser(targetParentUser);
        log.setActionStatus(actionStatus);
        log.setReasonCode(reasonCode);
        log.setDetailsJson(detailsJson);
        log.setOldValueJson(oldValueJson);
        log.setNewValueJson(newValueJson);
        log.setProviderName(providerName);
        log.setProviderModel(providerModel);
        log.setCorrelationId(correlationId);
        log.setRequestId(context != null ? context.requestId() : null);
        log.setIpAddress(context != null ? context.ipAddress() : null);
        log.setUserAgent(context != null ? context.userAgent() : null);
        aiAuditLogRepository.save(log);
    }

    public PaginatedResponseDTO<com.edusys.backend.ai.dto.AiAuditLogResponse> query(AiAuditLogQueryRequest query) {
        Specification<AiAuditLog> specification = Specification.where(null);

        if (query.getEntityType() != null && !query.getEntityType().isBlank()) {
            specification = specification.and((root, cq, cb) -> cb.equal(root.get("entityType"), AiEntityType.valueOf(query.getEntityType().toUpperCase())));
        }
        if (query.getEntityId() != null) {
            specification = specification.and((root, cq, cb) -> cb.equal(root.get("entityId"), query.getEntityId()));
        }
        if (query.getActorUserId() != null) {
            specification = specification.and((root, cq, cb) -> cb.equal(root.get("actorUser").get("id"), query.getActorUserId()));
        }
        if (query.getStudentId() != null) {
            specification = specification.and((root, cq, cb) -> cb.equal(root.get("targetStudent").get("id"), query.getStudentId()));
        }
        if (query.getEventType() != null && !query.getEventType().isBlank()) {
            specification = specification.and((root, cq, cb) -> cb.equal(root.get("eventType"), AiAuditEventType.valueOf(query.getEventType().toUpperCase())));
        }
        if (query.getFrom() != null) {
            specification = specification.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), query.getFrom()));
        }
        if (query.getTo() != null) {
            specification = specification.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), query.getTo()));
        }

        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();

        Page<AiAuditLog> auditPage = aiAuditLogRepository.findAll(
                specification,
                PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt", "id"))
        );

        List<com.edusys.backend.ai.dto.AiAuditLogResponse> items = auditPage.getContent().stream()
                .map(AiMapper::toAuditResponse)
                .toList();

        return new PaginatedResponseDTO<>(
                items,
                auditPage.getNumber() + 1,
                auditPage.getSize(),
                auditPage.getTotalElements(),
                auditPage.getTotalPages()
        );
    }

    public String toJson(Map<String, ?> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to serialize audit payload");
        }
    }
}
