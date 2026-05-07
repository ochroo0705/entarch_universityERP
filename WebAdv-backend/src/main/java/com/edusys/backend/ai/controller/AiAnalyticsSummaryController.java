package com.edusys.backend.ai.controller;

import com.edusys.backend.ai.audit.AiAuditContext;
import com.edusys.backend.ai.dto.AiAuditLogQueryRequest;
import com.edusys.backend.ai.dto.AiAuditLogResponse;
import com.edusys.backend.ai.dto.summary.*;
import com.edusys.backend.ai.service.summary.AnalyticsSummaryWorkflowService;
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
@RequestMapping("/api/ai/analytics-summaries")
public class AiAnalyticsSummaryController {

    private final AnalyticsSummaryWorkflowService analyticsSummaryWorkflowService;

    public AiAnalyticsSummaryController(AnalyticsSummaryWorkflowService analyticsSummaryWorkflowService) {
        this.analyticsSummaryWorkflowService = analyticsSummaryWorkflowService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<AnalyticsSummaryListItemResponse>> listSummaries(
            @ModelAttribute AnalyticsSummaryQueryRequest query,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(analyticsSummaryWorkflowService.listSummaries(query, auditContext(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<AnalyticsSummaryResponse> getSummary(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(analyticsSummaryWorkflowService.getSummary(id, auditContext(request)));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<AnalyticsSummaryResponse> generateSummary(
            @Valid @RequestBody AnalyticsSummaryGenerateRequest request,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(analyticsSummaryWorkflowService.generateSummary(request, auditContext(servletRequest)));
    }

    @PostMapping("/{id}/refresh")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<AnalyticsSummaryResponse> refreshSummary(
            @PathVariable Long id,
            @RequestParam(required = false) String languageCode,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(analyticsSummaryWorkflowService.refreshSummary(id, languageCode, auditContext(request)));
    }

    @GetMapping("/teacher-overview")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AnalyticsSummaryBundleResponse> getTeacherOverview(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String languageCode,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(analyticsSummaryWorkflowService.getTeacherOverview(classId, languageCode, auditContext(request)));
    }

    @GetMapping("/admin-overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsSummaryBundleResponse> getAdminOverview(
            @RequestParam(required = false) Integer gradeLevel,
            @RequestParam(required = false) String languageCode,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(analyticsSummaryWorkflowService.getAdminOverview(gradeLevel, languageCode, auditContext(request)));
    }

    @GetMapping("/{id}/audit-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<PaginatedResponseDTO<AiAuditLogResponse>> getSummaryAuditLogs(
            @PathVariable Long id,
            @ModelAttribute AiAuditLogQueryRequest query,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(analyticsSummaryWorkflowService.getSummaryAuditLogs(id, query, auditContext(request)));
    }

    private AiAuditContext auditContext(HttpServletRequest request) {
        return new AiAuditContext(
                request.getHeader("X-Request-Id"),
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );
    }
}
