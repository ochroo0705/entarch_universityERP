package com.edusys.backend.ai.controller;

import com.edusys.backend.ai.audit.AiAuditContext;
import com.edusys.backend.ai.dto.AiAuditLogQueryRequest;
import com.edusys.backend.ai.dto.AiAuditLogResponse;
import com.edusys.backend.ai.model.AiAuditActionStatus;
import com.edusys.backend.ai.model.AiAuditEventType;
import com.edusys.backend.ai.model.AiEntityType;
import com.edusys.backend.ai.service.AiAuditService;
import com.edusys.backend.ai.service.AiContextAssemblerService;
import com.edusys.backend.ai.validation.AiAccessService;
import com.edusys.backend.dto.PaginatedResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiAuditController {

    private final AiAuditService aiAuditService;
    private final AiAccessService aiAccessService;
    private final AiContextAssemblerService aiContextAssemblerService;

    public AiAuditController(
            AiAuditService aiAuditService,
            AiAccessService aiAccessService,
            AiContextAssemblerService aiContextAssemblerService
    ) {
        this.aiAuditService = aiAuditService;
        this.aiAccessService = aiAccessService;
        this.aiContextAssemblerService = aiContextAssemblerService;
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponseDTO<AiAuditLogResponse>> getAuditLogs(
            @ModelAttribute AiAuditLogQueryRequest query,
            HttpServletRequest request
    ) {
        var actor = aiAccessService.requireCurrentUser();
        aiAuditService.record(
                AiAuditEventType.AUDIT_LOG_VIEWED,
                AiEntityType.AI_AUDIT_LOG,
                null,
                actor,
                null,
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                aiContextAssemblerService.toJson(Map.of(
                        "entityType", query.getEntityType() == null ? "" : query.getEntityType(),
                        "eventType", query.getEventType() == null ? "" : query.getEventType(),
                        "studentId", query.getStudentId() == null ? "" : query.getStudentId()
                )),
                null,
                null,
                new AiAuditContext(request.getHeader("X-Request-Id"), request.getRemoteAddr(), request.getHeader("User-Agent"))
        );
        return ResponseEntity.ok(aiAuditService.query(query));
    }
}
