package com.edusys.backend.ai.controller;

import com.edusys.backend.ai.audit.AiAuditContext;
import com.edusys.backend.ai.dto.*;
import com.edusys.backend.ai.service.RiskConfigurationService;
import com.edusys.backend.ai.service.RiskRecalculationDispatchService;
import com.edusys.backend.ai.service.RiskSnapshotService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/ai")
public class AiRiskController {

    private final RiskSnapshotService riskSnapshotService;
    private final RiskConfigurationService riskConfigurationService;
    private final RiskRecalculationDispatchService riskRecalculationDispatchService;

    public AiRiskController(
            RiskSnapshotService riskSnapshotService,
            RiskConfigurationService riskConfigurationService,
            RiskRecalculationDispatchService riskRecalculationDispatchService
    ) {
        this.riskSnapshotService = riskSnapshotService;
        this.riskConfigurationService = riskConfigurationService;
        this.riskRecalculationDispatchService = riskRecalculationDispatchService;
    }

    @GetMapping("/risk-snapshots")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<RiskDashboardItemResponse>> listRiskSnapshots(
            @RequestParam(required = false) Long studentId,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(riskSnapshotService.listSnapshots(studentId, auditContext(request)));
    }

    @PostMapping("/risk-snapshots")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<RiskSnapshotResponse> createRiskSnapshot(
            @Valid @RequestBody RiskSnapshotCreateRequest request,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(riskSnapshotService.createSnapshot(request, auditContext(servletRequest)));
    }

    @GetMapping("/risk-snapshots/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<RiskSnapshotResponse> getRiskSnapshot(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(riskSnapshotService.getSnapshot(id, auditContext(request)));
    }

    @GetMapping("/students/{studentId}/risk-snapshots")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<RiskDashboardItemResponse>> listStudentRiskSnapshots(@PathVariable Long studentId, HttpServletRequest request) {
        return ResponseEntity.ok(riskSnapshotService.listSnapshots(studentId, auditContext(request)));
    }

    @GetMapping("/risk-dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<RiskDashboardListItemResponse>> getRiskDashboard(
            @ModelAttribute RiskDashboardQueryRequest query,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(riskSnapshotService.getDashboard(query, auditContext(request)));
    }

    @GetMapping("/risk-dashboard/admin-bundle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RiskDashboardBundleResponse> getAdminRiskDashboardBundle(
            @ModelAttribute RiskDashboardQueryRequest query,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(riskSnapshotService.getAdminDashboardBundle(query, auditContext(request)));
    }

    @GetMapping("/risk-dashboard/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<RiskDetailResponse> getRiskDashboardDetail(@PathVariable Long studentId, HttpServletRequest request) {
        return ResponseEntity.ok(riskSnapshotService.getDashboardDetail(studentId, auditContext(request)));
    }

    @GetMapping("/risk-dashboard/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RiskSummaryResponse> getRiskSummary(
            @ModelAttribute RiskDashboardQueryRequest query,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(riskSnapshotService.getSummary(query, auditContext(request)));
    }

    @PostMapping("/risk-dashboard/{studentId}/recalculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<RiskSnapshotResponse> recalculateStudentRisk(@PathVariable Long studentId, HttpServletRequest request) {
        return ResponseEntity.ok(riskSnapshotService.recalculateStudent(studentId, auditContext(request)));
    }

    @PostMapping("/risk-dashboard/recalculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RiskRecalculationJobResponse> recalculateRiskScope(
            @RequestBody(required = false) RiskRecalculationRequest request,
            HttpServletRequest servletRequest
    ) {
        RiskRecalculationRequest effective = request == null ? new RiskRecalculationRequest(null, null) : request;
        var actor = riskSnapshotService.requireAdminForScopeRecalculation();
        var studentIds = riskSnapshotService.resolveScopeStudentIds(effective);
        String scope = effective.studentId() != null ? "STUDENT" : effective.classId() != null ? "CLASS" : "SCHOOL";
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(riskRecalculationDispatchService.dispatchScopeRecalculation(actor, studentIds, scope));
    }

    @GetMapping("/risk-dashboard/filters/access-scope")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<RiskAccessScopeResponse> getRiskAccessScope() {
        return ResponseEntity.ok(riskSnapshotService.getAccessScope());
    }

    @GetMapping("/risk-config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RiskConfigResponse> getRiskConfig() {
        return ResponseEntity.ok(riskConfigurationService.getActiveConfigResponse());
    }

    @PutMapping("/risk-config/{configKey}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RiskConfigResponse> updateRiskConfig(
            @PathVariable String configKey,
            @Valid @RequestBody RiskConfigUpdateRequest request
    ) {
        return ResponseEntity.ok(riskConfigurationService.updateConfig(configKey, request));
    }

    private AiAuditContext auditContext(HttpServletRequest request) {
        return new AiAuditContext(
                request.getHeader("X-Request-Id"),
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );
    }
}
