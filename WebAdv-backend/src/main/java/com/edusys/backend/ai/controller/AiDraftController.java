package com.edusys.backend.ai.controller;

import com.edusys.backend.ai.audit.AiAuditContext;
import com.edusys.backend.ai.dto.*;
import com.edusys.backend.ai.service.draft.ParentMessageDraftWorkflowService;
import com.edusys.backend.dto.PaginatedResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/ai")
public class AiDraftController {

    private final ParentMessageDraftWorkflowService parentMessageDraftWorkflowService;

    public AiDraftController(ParentMessageDraftWorkflowService parentMessageDraftWorkflowService) {
        this.parentMessageDraftWorkflowService = parentMessageDraftWorkflowService;
    }

    @GetMapping("/message-drafts")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ParentMessageDraftResponse>> listDrafts(
            @ModelAttribute ParentMessageDraftQueryRequest query,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(parentMessageDraftWorkflowService.listDrafts(query, auditContext(request)));
    }

    @GetMapping("/message-drafts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ParentMessageDraftResponse> getDraft(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(parentMessageDraftWorkflowService.getDraft(id, auditContext(request)));
    }

    @PostMapping("/message-drafts")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ParentMessageDraftResponse> createDraft(
            @Valid @RequestBody ParentMessageDraftCreateRequest request,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(parentMessageDraftWorkflowService.createDraft(request, auditContext(servletRequest)));
    }

    @PutMapping("/message-drafts/{id}/content")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ParentMessageDraftResponse> updateDraft(
            @PathVariable Long id,
            @Valid @RequestBody ParentMessageDraftUpdateRequest request,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(parentMessageDraftWorkflowService.updateDraft(id, request, auditContext(servletRequest)));
    }

    @PostMapping("/message-drafts/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ParentMessageDraftResponse> approveDraft(
            @PathVariable Long id,
            @RequestBody(required = false) DraftDecisionRequest request,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(parentMessageDraftWorkflowService.approveDraft(
                id,
                request == null ? new DraftDecisionRequest(null) : request,
                auditContext(servletRequest)
        ));
    }

    @PostMapping("/message-drafts/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ParentMessageDraftResponse> rejectDraft(
            @PathVariable Long id,
            @RequestBody(required = false) DraftDecisionRequest request,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(parentMessageDraftWorkflowService.rejectDraft(
                id,
                request == null ? new DraftDecisionRequest(null) : request,
                auditContext(servletRequest)
        ));
    }

    @PostMapping("/message-drafts/{id}/retry-generation")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ParentMessageDraftResponse> retryGeneration(
            @PathVariable Long id,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(parentMessageDraftWorkflowService.retryGeneration(id, auditContext(servletRequest)));
    }

    @GetMapping("/message-drafts/{id}/audit-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<PaginatedResponseDTO<AiAuditLogResponse>> getDraftAuditLogs(
            @PathVariable Long id,
            @ModelAttribute AiAuditLogQueryRequest query,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(parentMessageDraftWorkflowService.getDraftAuditLogs(id, query, auditContext(servletRequest)));
    }

    private AiAuditContext auditContext(HttpServletRequest request) {
        return new AiAuditContext(
                request.getHeader("X-Request-Id"),
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );
    }
}
