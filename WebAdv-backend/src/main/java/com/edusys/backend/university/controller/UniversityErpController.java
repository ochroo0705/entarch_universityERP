package com.edusys.backend.university.controller;

import com.edusys.backend.university.dto.ApplicantCreateRequest;
import com.edusys.backend.university.dto.ApplicantDecisionRequest;
import com.edusys.backend.university.dto.ApplicantResponse;
import com.edusys.backend.university.dto.BankPaymentCallbackRequest;
import com.edusys.backend.university.dto.BankPaymentCallbackResponse;
import com.edusys.backend.university.dto.AcademicPolicyRequest;
import com.edusys.backend.university.dto.AcademicPolicyResponse;
import com.edusys.backend.university.dto.AcademicRecordRequest;
import com.edusys.backend.university.dto.AcademicRecordResponse;
import com.edusys.backend.university.dto.CourseCorequisiteRequest;
import com.edusys.backend.university.dto.CourseCorequisiteResponse;
import com.edusys.backend.university.dto.CourseOptionResponse;
import com.edusys.backend.university.dto.CoursePrerequisiteRequest;
import com.edusys.backend.university.dto.CoursePrerequisiteResponse;
import com.edusys.backend.university.dto.CourseSelectionBatchResponse;
import com.edusys.backend.university.dto.CourseSelectionRequest;
import com.edusys.backend.university.dto.CourseSelectionResponse;
import com.edusys.backend.university.dto.DegreeAuditResponse;
import com.edusys.backend.university.dto.DepartmentRequest;
import com.edusys.backend.university.dto.DepartmentResponse;
import com.edusys.backend.university.dto.FacultyProfileRequest;
import com.edusys.backend.university.dto.FacultyProfileResponse;
import com.edusys.backend.university.dto.FacultyLeaveDecisionRequest;
import com.edusys.backend.university.dto.FacultyLeaveRequest;
import com.edusys.backend.university.dto.FacultyLeaveResponse;
import com.edusys.backend.university.dto.FacultyWorkloadRequest;
import com.edusys.backend.university.dto.FacultyWorkloadResponse;
import com.edusys.backend.university.dto.GovernmentReportExportResponse;
import com.edusys.backend.university.dto.GraduationClearanceRequest;
import com.edusys.backend.university.dto.GraduationClearanceResponse;
import com.edusys.backend.university.dto.IntegrationConnectionRequest;
import com.edusys.backend.university.dto.IntegrationConnectionResponse;
import com.edusys.backend.university.dto.IntegrationSmokeTestResponse;
import com.edusys.backend.university.dto.LmsRosterExportResponse;
import com.edusys.backend.university.dto.NotificationDispatchResponse;
import com.edusys.backend.university.dto.ProgramRequirementRequest;
import com.edusys.backend.university.dto.ProgramRequirementResponse;
import com.edusys.backend.university.dto.ServiceRequestCreateRequest;
import com.edusys.backend.university.dto.ServiceQueueResponse;
import com.edusys.backend.university.dto.ServiceRequestAssignmentRequest;
import com.edusys.backend.university.dto.ServiceRequestAttachmentResponse;
import com.edusys.backend.university.dto.ServiceRequestCommentRequest;
import com.edusys.backend.university.dto.ServiceRequestCommentResponse;
import com.edusys.backend.university.dto.ServiceRequestDetailResponse;
import com.edusys.backend.university.dto.ServiceRequestResponse;
import com.edusys.backend.university.dto.ServiceRequestStatusRequest;
import com.edusys.backend.university.dto.ServiceTypeRequest;
import com.edusys.backend.university.dto.ServiceTypeResponse;
import com.edusys.backend.university.dto.UniversityDemoSeedResponse;
import com.edusys.backend.university.dto.UniversityErpEventLogResponse;
import com.edusys.backend.university.dto.UniversityIntegrationRunResponse;
import com.edusys.backend.university.dto.UniversityIntegrationStatusResponse;
import com.edusys.backend.university.dto.UniversityReportDefinitionResponse;
import com.edusys.backend.university.dto.UniversityReportDetailRowResponse;
import com.edusys.backend.university.dto.UniversityReportResponse;
import com.edusys.backend.university.dto.UniversityReportRunResponse;
import com.edusys.backend.university.model.ApplicantStatus;
import com.edusys.backend.university.model.ServiceRequestStatus;
import com.edusys.backend.university.service.UniversityErpService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/university-erp")
public class UniversityErpController {
    private final UniversityErpService universityErpService;

    public UniversityErpController(UniversityErpService universityErpService) {
        this.universityErpService = universityErpService;
    }

    @GetMapping("/admissions/applicants")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public List<ApplicantResponse> listApplicants(@RequestParam(required = false) ApplicantStatus status) {
        return universityErpService.listApplicants(status);
    }

    @PostMapping("/admissions/applicants")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public ApplicantResponse createApplicant(@Valid @RequestBody ApplicantCreateRequest request) {
        return universityErpService.createApplicant(request);
    }

    @PostMapping("/admissions/applicants/{id}/screen")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public ApplicantResponse screenApplicant(@PathVariable Long id, @RequestBody ApplicantDecisionRequest request) {
        return universityErpService.screenApplicant(id, request);
    }

    @PostMapping("/admissions/applicants/{id}/accept")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public ApplicantResponse acceptApplicant(@PathVariable Long id, @RequestBody ApplicantDecisionRequest request) {
        return universityErpService.acceptAndRegisterApplicant(id, request);
    }

    @PostMapping("/admissions/applicants/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public ApplicantResponse rejectApplicant(@PathVariable Long id, @RequestBody ApplicantDecisionRequest request) {
        return universityErpService.rejectApplicant(id, request);
    }

    @GetMapping("/course-selection/courses")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','STUDENT')")
    public List<CourseOptionResponse> listCourseOptions() {
        return universityErpService.listCourseOptions();
    }

    @GetMapping("/course-selection/selections")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF') or (hasRole('STUDENT') and @studentAccess.canAccessStudent(#studentId))")
    public List<CourseSelectionResponse> listSelections(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Integer semester) {
        return universityErpService.listSelections(studentId, academicYear, semester);
    }

    @PostMapping("/course-selection/selections")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF') or (hasRole('STUDENT') and @studentAccess.canAccessStudent(#request.studentId()))")
    public CourseSelectionBatchResponse selectCourses(@Valid @RequestBody CourseSelectionRequest request) {
        return universityErpService.selectCourses(request);
    }

    @GetMapping("/student-services/types")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF','STUDENT')")
    public List<ServiceTypeResponse> listServiceTypes(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return universityErpService.listServiceTypes(activeOnly);
    }

    @PostMapping("/student-services/types")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public ServiceTypeResponse createServiceType(@Valid @RequestBody ServiceTypeRequest request) {
        return universityErpService.createServiceType(request);
    }

    @PutMapping("/student-services/types/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public ServiceTypeResponse updateServiceType(@PathVariable Long id, @Valid @RequestBody ServiceTypeRequest request) {
        return universityErpService.updateServiceType(id, request);
    }

    @GetMapping("/course-selection/prerequisites")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','STUDENT')")
    public List<CoursePrerequisiteResponse> listPrerequisites(@RequestParam(required = false) Long subjectId) {
        return universityErpService.listPrerequisites(subjectId);
    }

    @PostMapping("/course-selection/prerequisites")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public CoursePrerequisiteResponse createPrerequisite(@Valid @RequestBody CoursePrerequisiteRequest request) {
        return universityErpService.createPrerequisite(request);
    }

    @GetMapping("/course-selection/corequisites")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','STUDENT')")
    public List<CourseCorequisiteResponse> listCorequisites(@RequestParam(required = false) Long subjectId) {
        return universityErpService.listCorequisites(subjectId);
    }

    @PostMapping("/course-selection/corequisites")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public CourseCorequisiteResponse createCorequisite(@Valid @RequestBody CourseCorequisiteRequest request) {
        return universityErpService.createCorequisite(request);
    }

    @GetMapping("/course-selection/academic-records")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF') or (hasRole('STUDENT') and @studentAccess.canAccessStudent(#studentId))")
    public List<AcademicRecordResponse> listAcademicRecords(@RequestParam(required = false) Long studentId) {
        return universityErpService.listAcademicRecords(studentId);
    }

    @PostMapping("/course-selection/academic-records")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public AcademicRecordResponse createAcademicRecord(@Valid @RequestBody AcademicRecordRequest request) {
        return universityErpService.createAcademicRecord(request);
    }

    @GetMapping("/course-selection/academic-policy")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','STUDENT')")
    public AcademicPolicyResponse getActiveAcademicPolicy() {
        return universityErpService.getActiveAcademicPolicy();
    }

    @PutMapping("/course-selection/academic-policy")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public AcademicPolicyResponse updateActiveAcademicPolicy(@Valid @RequestBody AcademicPolicyRequest request) {
        return universityErpService.updateActiveAcademicPolicy(request);
    }

    @GetMapping("/course-selection/program-requirements")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','STUDENT')")
    public List<ProgramRequirementResponse> listProgramRequirements(@RequestParam(required = false) String programName) {
        return universityErpService.listProgramRequirements(programName);
    }

    @PostMapping("/course-selection/program-requirements")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public ProgramRequirementResponse createProgramRequirement(@Valid @RequestBody ProgramRequirementRequest request) {
        return universityErpService.createProgramRequirement(request);
    }

    @GetMapping("/course-selection/degree-audit")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF') or (hasRole('STUDENT') and @studentAccess.canAccessStudent(#studentId))")
    public DegreeAuditResponse getDegreeAudit(
            @RequestParam Long studentId,
            @RequestParam String programName) {
        return universityErpService.getDegreeAudit(studentId, programName);
    }

    @GetMapping("/hr-faculty/profiles")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public List<FacultyProfileResponse> listFacultyProfiles() {
        return universityErpService.listFacultyProfiles();
    }

    @PostMapping("/hr-faculty/profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public FacultyProfileResponse upsertFacultyProfile(@Valid @RequestBody FacultyProfileRequest request) {
        return universityErpService.upsertFacultyProfile(request);
    }

    @GetMapping("/hr-faculty/departments")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public List<DepartmentResponse> listDepartments() {
        return universityErpService.listDepartments();
    }

    @PostMapping("/hr-faculty/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public DepartmentResponse createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return universityErpService.createDepartment(request);
    }

    @GetMapping("/hr-faculty/workloads")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public List<FacultyWorkloadResponse> listFacultyWorkloads(@RequestParam(required = false) Long facultyProfileId) {
        return universityErpService.listFacultyWorkloads(facultyProfileId);
    }

    @PostMapping("/hr-faculty/workloads")
    @PreAuthorize("hasRole('ADMIN')")
    public FacultyWorkloadResponse createFacultyWorkload(@Valid @RequestBody FacultyWorkloadRequest request) {
        return universityErpService.createFacultyWorkload(request);
    }

    @GetMapping("/hr-faculty/leave-requests")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public List<FacultyLeaveResponse> listFacultyLeaveRequests(
            @RequestParam(required = false) Long facultyProfileId,
            @RequestParam(required = false) String status) {
        return universityErpService.listFacultyLeaveRequests(facultyProfileId, status);
    }

    @PostMapping("/hr-faculty/leave-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public FacultyLeaveResponse createFacultyLeaveRequest(@Valid @RequestBody FacultyLeaveRequest request) {
        return universityErpService.createFacultyLeaveRequest(request);
    }

    @PostMapping("/hr-faculty/leave-requests/{id}/decision")
    @PreAuthorize("hasRole('ADMIN')")
    public FacultyLeaveResponse decideFacultyLeaveRequest(@PathVariable Long id, @Valid @RequestBody FacultyLeaveDecisionRequest request) {
        return universityErpService.decideFacultyLeaveRequest(id, request);
    }

    @GetMapping("/student-services/requests")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF') or (hasRole('STUDENT') and @studentAccess.canAccessStudent(#studentId))")
    public List<ServiceRequestResponse> listServiceRequests(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) ServiceRequestStatus status,
            @RequestParam(required = false) String assignedOffice,
            @RequestParam(required = false) Long assignedUserId,
            @RequestParam(required = false) String slaStatus) {
        return universityErpService.listServiceRequests(studentId, status, assignedOffice, assignedUserId, slaStatus);
    }

    @GetMapping("/student-services/queues")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF')")
    public List<ServiceQueueResponse> listServiceQueues() {
        return universityErpService.listServiceQueues();
    }

    @GetMapping("/student-services/requests/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF') or (hasRole('STUDENT') and @studentAccess.canAccessStudent(@universityErpService.getServiceRequestStudentId(#id)))")
    public ServiceRequestDetailResponse getServiceRequestDetail(@PathVariable Long id) {
        return universityErpService.getServiceRequestDetail(id);
    }

    @PostMapping("/student-services/requests")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF') or (hasRole('STUDENT') and @studentAccess.canAccessStudent(#request.studentId()))")
    public ServiceRequestResponse createServiceRequest(@Valid @RequestBody ServiceRequestCreateRequest request) {
        return universityErpService.createServiceRequest(request);
    }

    @PostMapping("/student-services/requests/{id}/comments")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF') or (hasRole('STUDENT') and @studentAccess.canAccessStudent(@universityErpService.getServiceRequestStudentId(#id)))")
    public ServiceRequestCommentResponse addServiceRequestComment(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequestCommentRequest request) {
        return universityErpService.addServiceRequestComment(id, request);
    }

    @PostMapping("/student-services/requests/{id}/attachments")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF') or (hasRole('STUDENT') and @studentAccess.canAccessStudent(@universityErpService.getServiceRequestStudentId(#id)))")
    public List<ServiceRequestAttachmentResponse> uploadServiceRequestAttachments(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        return universityErpService.uploadServiceRequestAttachments(id, files);
    }

    @PostMapping("/student-services/requests/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF')")
    public ServiceRequestResponse updateServiceRequestStatus(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequestStatusRequest request) {
        return universityErpService.updateServiceRequestStatus(id, request);
    }

    @PostMapping("/student-services/requests/{id}/assignment")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF')")
    public ServiceRequestResponse assignServiceRequest(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequestAssignmentRequest request) {
        return universityErpService.assignServiceRequest(id, request);
    }

    @PostMapping("/student-services/requests/{id}/graduation-clearance")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF')")
    public GraduationClearanceResponse evaluateGraduationClearance(
            @PathVariable Long id,
            @Valid @RequestBody GraduationClearanceRequest request) {
        return universityErpService.evaluateGraduationClearance(id, request);
    }

    @GetMapping("/reporting/summary")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF')")
    public UniversityReportResponse getReportSummary() {
        return universityErpService.getReportSummary();
    }

    @GetMapping("/reporting/audit-events")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF')")
    public List<UniversityErpEventLogResponse> listRecentEvents() {
        return universityErpService.listRecentEvents();
    }

    @GetMapping("/reporting/report-definitions")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF')")
    public List<UniversityReportDefinitionResponse> listReportDefinitions() {
        return universityErpService.listReportDefinitions();
    }

    @GetMapping("/reporting/report-runs")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF')")
    public List<UniversityReportRunResponse> listReportRuns() {
        return universityErpService.listReportRuns();
    }

    @PostMapping("/reporting/report-definitions/{reportKey}/run")
    @PreAuthorize("hasRole('ADMIN')")
    public UniversityReportRunResponse runReport(@PathVariable String reportKey) {
        return universityErpService.runReport(reportKey);
    }

    @GetMapping(value = "/reporting/report-definitions/{reportKey}/csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF')")
    public ResponseEntity<String> exportReportCsv(
            @PathVariable String reportKey,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String status) {
        String csv = universityErpService.exportReportCsv(reportKey, academicYear, semester, status);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + reportKey + ".csv\"")
                .body(csv);
    }

    @GetMapping("/reporting/report-definitions/{reportKey}/details")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF')")
    public List<UniversityReportDetailRowResponse> listReportDetails(
            @PathVariable String reportKey,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String status) {
        return universityErpService.listReportDetails(reportKey, academicYear, semester, status);
    }

    @GetMapping("/integrations")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF')")
    public List<UniversityIntegrationStatusResponse> listIntegrations() {
        return universityErpService.listIntegrations();
    }

    @GetMapping("/integrations/runs")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF')")
    public List<UniversityIntegrationRunResponse> listIntegrationRuns() {
        return universityErpService.listIntegrationRuns();
    }

    @GetMapping("/integrations/connections")
    @PreAuthorize("hasAnyRole('ADMIN','ADMISSIONS_STAFF','FINANCE_STAFF')")
    public List<IntegrationConnectionResponse> listIntegrationConnections() {
        return universityErpService.listIntegrationConnections();
    }

    @PostMapping("/integrations/connections")
    @PreAuthorize("hasRole('ADMIN')")
    public IntegrationConnectionResponse saveIntegrationConnection(@Valid @RequestBody IntegrationConnectionRequest request) {
        return universityErpService.saveIntegrationConnection(request);
    }

    @PostMapping("/integrations/smoke-test")
    @PreAuthorize("hasRole('ADMIN')")
    public List<IntegrationSmokeTestResponse> smokeTestIntegrations() {
        return universityErpService.smokeTestIntegrations();
    }

    @PostMapping("/integrations/{key}/run")
    @PreAuthorize("hasRole('ADMIN')")
    public UniversityIntegrationRunResponse runIntegration(@PathVariable String key) {
        return universityErpService.runIntegration(key);
    }

    @PostMapping("/integrations/{key}/fail")
    @PreAuthorize("hasRole('ADMIN')")
    public UniversityIntegrationRunResponse simulateIntegrationFailure(@PathVariable String key) {
        return universityErpService.simulateIntegrationFailure(key);
    }

    @PostMapping("/integrations/runs/{id}/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public UniversityIntegrationRunResponse retryIntegrationRun(@PathVariable Long id) {
        return universityErpService.retryIntegrationRun(id);
    }

    @PostMapping("/integrations/bank/payment-callback")
    @PreAuthorize("hasRole('ADMIN')")
    public BankPaymentCallbackResponse simulateBankPaymentCallback(@RequestBody(required = false) BankPaymentCallbackRequest request) {
        return universityErpService.simulateBankPaymentCallback(request);
    }

    @PostMapping("/integrations/lms/roster-export")
    @PreAuthorize("hasRole('ADMIN')")
    public LmsRosterExportResponse exportLmsRoster() {
        return universityErpService.exportLmsRoster();
    }

    @PostMapping("/integrations/notification/dispatch")
    @PreAuthorize("hasRole('ADMIN')")
    public NotificationDispatchResponse dispatchNotifications() {
        return universityErpService.dispatchNotifications();
    }

    @PostMapping("/integrations/government/report-export")
    @PreAuthorize("hasRole('ADMIN')")
    public GovernmentReportExportResponse exportGovernmentReport() {
        return universityErpService.exportGovernmentReport();
    }

    @PostMapping("/demo/seed")
    @PreAuthorize("hasRole('ADMIN')")
    public UniversityDemoSeedResponse seedDemoData() {
        return universityErpService.seedDemoData();
    }
}
