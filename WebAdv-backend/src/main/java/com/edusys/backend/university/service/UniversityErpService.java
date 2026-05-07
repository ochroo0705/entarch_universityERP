package com.edusys.backend.university.service;

import com.edusys.backend.model.FeeInvoice;
import com.edusys.backend.model.FeeInvoiceLine;
import com.edusys.backend.model.FeePayment;
import com.edusys.backend.model.Subject;
import com.edusys.backend.model.TeachingAssignment;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.FeeInvoiceRepository;
import com.edusys.backend.repository.FeePaymentRepository;
import com.edusys.backend.repository.SubjectRepository;
import com.edusys.backend.repository.TeachingAssignmentRepository;
import com.edusys.backend.repository.UserRepository;
import com.edusys.backend.service.FileStorageService;
import com.edusys.backend.university.dto.AcademicRecordRequest;
import com.edusys.backend.university.dto.AcademicRecordResponse;
import com.edusys.backend.university.dto.AcademicPolicyRequest;
import com.edusys.backend.university.dto.AcademicPolicyResponse;
import com.edusys.backend.university.dto.ApplicantCreateRequest;
import com.edusys.backend.university.dto.ApplicantDecisionRequest;
import com.edusys.backend.university.dto.ApplicantResponse;
import com.edusys.backend.university.dto.BankPaymentCallbackRequest;
import com.edusys.backend.university.dto.BankPaymentCallbackResponse;
import com.edusys.backend.university.dto.CourseCorequisiteRequest;
import com.edusys.backend.university.dto.CourseCorequisiteResponse;
import com.edusys.backend.university.dto.CourseOptionResponse;
import com.edusys.backend.university.dto.CoursePrerequisiteRequest;
import com.edusys.backend.university.dto.CoursePrerequisiteResponse;
import com.edusys.backend.university.dto.CourseSelectionBatchResponse;
import com.edusys.backend.university.dto.CourseSelectionRequest;
import com.edusys.backend.university.dto.CourseSelectionResponse;
import com.edusys.backend.university.dto.DegreeAuditRequirementResponse;
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
import com.edusys.backend.university.dto.ServiceRequestHistoryResponse;
import com.edusys.backend.university.dto.ServiceRequestResponse;
import com.edusys.backend.university.dto.ServiceRequestStatusRequest;
import com.edusys.backend.university.dto.ServiceTypeRequest;
import com.edusys.backend.university.dto.ServiceTypeResponse;
import com.edusys.backend.university.dto.UniversityDemoSeedResponse;
import com.edusys.backend.university.dto.UniversityErpEventLogResponse;
import com.edusys.backend.university.dto.UniversityIntegrationRunResponse;
import com.edusys.backend.university.dto.UniversityIntegrationStatusResponse;
import com.edusys.backend.university.dto.UniversityReportBreakdownResponse;
import com.edusys.backend.university.dto.UniversityReportDefinitionResponse;
import com.edusys.backend.university.dto.UniversityReportDetailRowResponse;
import com.edusys.backend.university.dto.UniversityReportResponse;
import com.edusys.backend.university.dto.UniversityReportRunResponse;
import com.edusys.backend.university.model.AcademicRecordStatus;
import com.edusys.backend.university.model.ApplicantStatus;
import com.edusys.backend.university.model.CourseSelectionStatus;
import com.edusys.backend.university.model.ServiceRequestStatus;
import com.edusys.backend.university.model.UniversityAcademicRecord;
import com.edusys.backend.university.model.UniversityAcademicPolicy;
import com.edusys.backend.university.model.UniversityApplicant;
import com.edusys.backend.university.model.UniversityCourseCorequisite;
import com.edusys.backend.university.model.UniversityCoursePrerequisite;
import com.edusys.backend.university.model.UniversityCourseSelection;
import com.edusys.backend.university.model.UniversityDepartment;
import com.edusys.backend.university.model.UniversityErpEventLog;
import com.edusys.backend.university.model.UniversityFacultyProfile;
import com.edusys.backend.university.model.UniversityFacultyLeaveRequest;
import com.edusys.backend.university.model.UniversityFacultyWorkload;
import com.edusys.backend.university.model.UniversityIntegrationConnection;
import com.edusys.backend.university.model.UniversityIntegrationRun;
import com.edusys.backend.university.model.UniversityProgramRequirement;
import com.edusys.backend.university.model.UniversityReportDefinition;
import com.edusys.backend.university.model.UniversityReportRun;
import com.edusys.backend.university.model.UniversityServiceRequest;
import com.edusys.backend.university.model.UniversityServiceRequestAttachment;
import com.edusys.backend.university.model.UniversityServiceRequestComment;
import com.edusys.backend.university.model.UniversityServiceRequestHistory;
import com.edusys.backend.university.model.UniversityServiceType;
import com.edusys.backend.university.repository.UniversityAcademicRecordRepository;
import com.edusys.backend.university.repository.UniversityAcademicPolicyRepository;
import com.edusys.backend.university.repository.UniversityApplicantRepository;
import com.edusys.backend.university.repository.UniversityCourseCorequisiteRepository;
import com.edusys.backend.university.repository.UniversityCoursePrerequisiteRepository;
import com.edusys.backend.university.repository.UniversityCourseSelectionRepository;
import com.edusys.backend.university.repository.UniversityDepartmentRepository;
import com.edusys.backend.university.repository.UniversityErpEventLogRepository;
import com.edusys.backend.university.repository.UniversityFacultyProfileRepository;
import com.edusys.backend.university.repository.UniversityFacultyLeaveRequestRepository;
import com.edusys.backend.university.repository.UniversityFacultyWorkloadRepository;
import com.edusys.backend.university.repository.UniversityIntegrationConnectionRepository;
import com.edusys.backend.university.repository.UniversityIntegrationRunRepository;
import com.edusys.backend.university.repository.UniversityProgramRequirementRepository;
import com.edusys.backend.university.repository.UniversityReportDefinitionRepository;
import com.edusys.backend.university.repository.UniversityReportRunRepository;
import com.edusys.backend.university.repository.UniversityServiceRequestRepository;
import com.edusys.backend.university.repository.UniversityServiceRequestAttachmentRepository;
import com.edusys.backend.university.repository.UniversityServiceRequestCommentRepository;
import com.edusys.backend.university.repository.UniversityServiceRequestHistoryRepository;
import com.edusys.backend.university.repository.UniversityServiceTypeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UniversityErpService {
    private static final BigDecimal PRICE_PER_CREDIT = new BigDecimal("100000");
    private static final DateTimeFormatter NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final List<ServiceRequestStatus> TERMINAL_SERVICE_STATUSES = List.of(
            ServiceRequestStatus.DELIVERED,
            ServiceRequestStatus.REJECTED);

    private final UniversityApplicantRepository applicantRepository;
    private final UniversityCourseSelectionRepository selectionRepository;
    private final UniversityServiceRequestRepository serviceRequestRepository;
    private final UniversityCourseCorequisiteRepository corequisiteRepository;
    private final UniversityCoursePrerequisiteRepository prerequisiteRepository;
    private final UniversityAcademicRecordRepository academicRecordRepository;
    private final UniversityAcademicPolicyRepository academicPolicyRepository;
    private final UniversityProgramRequirementRepository programRequirementRepository;
    private final UniversityDepartmentRepository departmentRepository;
    private final UniversityFacultyProfileRepository facultyProfileRepository;
    private final UniversityFacultyLeaveRequestRepository facultyLeaveRequestRepository;
    private final UniversityFacultyWorkloadRepository facultyWorkloadRepository;
    private final UniversityIntegrationConnectionRepository integrationConnectionRepository;
    private final UniversityIntegrationRunRepository integrationRunRepository;
    private final UniversityReportDefinitionRepository reportDefinitionRepository;
    private final UniversityReportRunRepository reportRunRepository;
    private final UniversityErpEventLogRepository eventLogRepository;
    private final UniversityServiceTypeRepository serviceTypeRepository;
    private final UniversityServiceRequestCommentRepository serviceRequestCommentRepository;
    private final UniversityServiceRequestHistoryRepository serviceRequestHistoryRepository;
    private final UniversityServiceRequestAttachmentRepository serviceRequestAttachmentRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final FeeInvoiceRepository invoiceRepository;
    private final FeePaymentRepository paymentRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final UniversityHttpIntegrationAdapter httpIntegrationAdapter;

    public UniversityErpService(
            UniversityApplicantRepository applicantRepository,
            UniversityCourseSelectionRepository selectionRepository,
            UniversityServiceRequestRepository serviceRequestRepository,
            UniversityCourseCorequisiteRepository corequisiteRepository,
            UniversityCoursePrerequisiteRepository prerequisiteRepository,
            UniversityAcademicRecordRepository academicRecordRepository,
            UniversityAcademicPolicyRepository academicPolicyRepository,
            UniversityProgramRequirementRepository programRequirementRepository,
            UniversityDepartmentRepository departmentRepository,
            UniversityFacultyProfileRepository facultyProfileRepository,
            UniversityFacultyLeaveRequestRepository facultyLeaveRequestRepository,
            UniversityFacultyWorkloadRepository facultyWorkloadRepository,
            UniversityIntegrationConnectionRepository integrationConnectionRepository,
            UniversityIntegrationRunRepository integrationRunRepository,
            UniversityReportDefinitionRepository reportDefinitionRepository,
            UniversityReportRunRepository reportRunRepository,
            UniversityErpEventLogRepository eventLogRepository,
            UniversityServiceTypeRepository serviceTypeRepository,
            UniversityServiceRequestCommentRepository serviceRequestCommentRepository,
            UniversityServiceRequestHistoryRepository serviceRequestHistoryRepository,
            UniversityServiceRequestAttachmentRepository serviceRequestAttachmentRepository,
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            TeachingAssignmentRepository teachingAssignmentRepository,
            FeeInvoiceRepository invoiceRepository,
            FeePaymentRepository paymentRepository,
            FileStorageService fileStorageService,
            PasswordEncoder passwordEncoder,
            UniversityHttpIntegrationAdapter httpIntegrationAdapter) {
        this.applicantRepository = applicantRepository;
        this.selectionRepository = selectionRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.corequisiteRepository = corequisiteRepository;
        this.prerequisiteRepository = prerequisiteRepository;
        this.academicRecordRepository = academicRecordRepository;
        this.academicPolicyRepository = academicPolicyRepository;
        this.programRequirementRepository = programRequirementRepository;
        this.departmentRepository = departmentRepository;
        this.facultyProfileRepository = facultyProfileRepository;
        this.facultyLeaveRequestRepository = facultyLeaveRequestRepository;
        this.facultyWorkloadRepository = facultyWorkloadRepository;
        this.integrationConnectionRepository = integrationConnectionRepository;
        this.integrationRunRepository = integrationRunRepository;
        this.reportDefinitionRepository = reportDefinitionRepository;
        this.reportRunRepository = reportRunRepository;
        this.eventLogRepository = eventLogRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.serviceRequestCommentRepository = serviceRequestCommentRepository;
        this.serviceRequestHistoryRepository = serviceRequestHistoryRepository;
        this.serviceRequestAttachmentRepository = serviceRequestAttachmentRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
        this.httpIntegrationAdapter = httpIntegrationAdapter;
    }

    @Transactional(readOnly = true)
    public List<ApplicantResponse> listApplicants(ApplicantStatus status) {
        List<UniversityApplicant> applicants = status == null
                ? applicantRepository.findAllByOrderBySubmittedAtDescIdDesc()
                : applicantRepository.findByStatusOrderBySubmittedAtDescIdDesc(status);
        return applicants.stream().map(this::toApplicantResponse).toList();
    }

    @Transactional
    public ApplicantResponse createApplicant(ApplicantCreateRequest request) {
        applicantRepository.findByEmail(request.email().trim().toLowerCase(Locale.ROOT)).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Applicant email already exists");
        });

        LocalDateTime now = LocalDateTime.now();
        UniversityApplicant applicant = new UniversityApplicant();
        applicant.setApplicationNumber(nextApplicationNumber());
        applicant.setFirstName(request.firstName().trim());
        applicant.setLastName(request.lastName().trim());
        applicant.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        applicant.setPhone(blankToNull(request.phone()));
        applicant.setProgram(request.program().trim());
        applicant.setStatus(ApplicantStatus.SUBMITTED);
        applicant.setSubmittedAt(now);
        applicant.setUpdatedAt(now);
        return toApplicantResponse(applicantRepository.save(applicant));
    }

    @Transactional
    public ApplicantResponse screenApplicant(Long id, ApplicantDecisionRequest request) {
        UniversityApplicant applicant = getApplicant(id);
        ensureMutableDecision(applicant);
        applicant.setStatus(ApplicantStatus.SCREENING);
        applicant.setDecisionNotes(blankToNull(request.notes()));
        applicant.setUpdatedAt(LocalDateTime.now());
        return toApplicantResponse(applicantRepository.save(applicant));
    }

    @Transactional
    public ApplicantResponse rejectApplicant(Long id, ApplicantDecisionRequest request) {
        UniversityApplicant applicant = getApplicant(id);
        ensureMutableDecision(applicant);
        applicant.setStatus(ApplicantStatus.REJECTED);
        applicant.setDecisionNotes(blankToNull(request.notes()));
        applicant.setUpdatedAt(LocalDateTime.now());
        return toApplicantResponse(applicantRepository.save(applicant));
    }

    @Transactional
    public ApplicantResponse acceptAndRegisterApplicant(Long id, ApplicantDecisionRequest request) {
        UniversityApplicant applicant = getApplicant(id);
        if (applicant.getStatus() == ApplicantStatus.CONVERTED && applicant.getConvertedStudent() != null) {
            return toApplicantResponse(applicant);
        }
        if (applicant.getStatus() == ApplicantStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Rejected applicants cannot be converted");
        }

        User student = userRepository.findByEmail(applicant.getEmail())
                .map(existing -> ensureStudentRole(existing, applicant))
                .orElseGet(() -> createStudentUser(applicant));

        applicant.setStatus(ApplicantStatus.CONVERTED);
        applicant.setDecisionNotes(blankToNull(request.notes()));
        applicant.setConvertedStudent(student);
        applicant.setUpdatedAt(LocalDateTime.now());
        UniversityApplicant saved = applicantRepository.save(applicant);
        recordEvent("Admissions", "Applicant converted", "UniversityApplicant", saved.getId(), student, saved.getApplicationNumber());
        return toApplicantResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseOptionResponse> listCourseOptions() {
        return subjectRepository.findAll().stream()
                .sorted((left, right) -> {
                    String leftCode = left.getSubjectCode() == null ? "" : left.getSubjectCode();
                    String rightCode = right.getSubjectCode() == null ? "" : right.getSubjectCode();
                    int byCode = leftCode.compareToIgnoreCase(rightCode);
                    return byCode != 0 ? byCode : safeName(left).compareToIgnoreCase(safeName(right));
                })
                .map(this::toCourseOptionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseSelectionResponse> listSelections(Long studentId, String academicYear, Integer semester) {
        List<UniversityCourseSelection> selections;
        if (studentId != null && academicYear != null && semester != null) {
            selections = selectionRepository.findByStudent_IdAndAcademicYearAndSemesterOrderBySelectedAtDescIdDesc(studentId, academicYear, semester);
        } else if (studentId != null) {
            selections = selectionRepository.findByStudent_IdOrderBySelectedAtDescIdDesc(studentId);
        } else if (academicYear != null && semester != null) {
            selections = selectionRepository.findByAcademicYearAndSemesterOrderBySelectedAtDescIdDesc(academicYear, semester);
        } else {
            selections = selectionRepository.findAll();
        }
        return selections.stream().map(this::toCourseSelectionResponse).toList();
    }

    @Transactional
    public CourseSelectionBatchResponse selectCourses(CourseSelectionRequest request) {
        String academicYear = normalizeAcademicYear(request.academicYear());
        int semester = request.semester() == null ? 1 : request.semester();
        if (semester < 1 || semester > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Semester must be between 1 and 3");
        }

        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (!student.isStudent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course selection requires a student user");
        }
        if (outstandingBalanceFor(student).compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Finance hold: outstanding balance must be cleared before new course selections");
        }
        UniversityAcademicPolicy policy = activeAcademicPolicy();

        Set<Long> subjectIds = new LinkedHashSet<>(request.subjectIds());
        if (subjectIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at least one course");
        }

        List<UniversityCourseSelection> existing = selectionRepository
                .findByStudent_IdAndAcademicYearAndSemesterOrderBySelectedAtDescIdDesc(student.getId(), academicYear, semester);
        int existingCredits = existing.stream()
                .filter(selection -> selection.getStatus() != CourseSelectionStatus.DROPPED)
                .mapToInt(UniversityCourseSelection::getCredits)
                .sum();

        List<UniversityCourseSelection> newSelections = new ArrayList<>();
        int newCredits = 0;
        LocalDateTime now = LocalDateTime.now();
        for (Long subjectId : subjectIds) {
            if (selectionRepository.findByStudent_IdAndSubject_IdAndAcademicYearAndSemester(student.getId(), subjectId, academicYear, semester).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Course is already selected for this term");
            }
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found: " + subjectId));
            ensurePrerequisitesCompleted(student, subject);
            ensureCorequisitesSatisfied(student, subject, subjectIds, academicYear, semester);
            ensureRepeatAllowed(student, subject, policy);
            int credits = creditsFor(subject);
            newCredits += credits;

            UniversityCourseSelection selection = new UniversityCourseSelection();
            selection.setStudent(student);
            selection.setSubject(subject);
            selection.setAcademicYear(academicYear);
            selection.setSemester(semester);
            selection.setCredits(credits);
            selection.setStatus(CourseSelectionStatus.SELECTED);
            selection.setSelectedAt(now);
            selection.setUpdatedAt(now);
            newSelections.add(selection);
        }

        int allowedCredits = allowedTermCredits(student, policy);
        if (existingCredits + newCredits > allowedCredits) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Term credit limit exceeded: allowed " + allowedCredits + " credits by active academic policy");
        }

        FeeInvoice invoice = createCourseSelectionInvoice(student, academicYear, semester, newSelections);
        List<UniversityCourseSelection> savedSelections = new ArrayList<>();
        for (UniversityCourseSelection selection : newSelections) {
            selection.setStatus(CourseSelectionStatus.BILLED);
            selection.setInvoice(invoice);
            selection.setUpdatedAt(LocalDateTime.now());
            savedSelections.add(selectionRepository.save(selection));
        }

        BigDecimal invoiceAmount = totalInvoiceAmount(invoice);
        recordEvent("Course selection", "Courses selected and billed", "FeeInvoice", invoice.getId(), student, invoice.getInvoiceNumber());
        return new CourseSelectionBatchResponse(
                student.getId(),
                student.getFullName().trim(),
                academicYear,
                semester,
                newCredits,
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoiceAmount,
                savedSelections.stream().map(this::toCourseSelectionResponse).toList());
    }

    @Transactional(readOnly = true)
    public List<CoursePrerequisiteResponse> listPrerequisites(Long subjectId) {
        List<UniversityCoursePrerequisite> prerequisites = subjectId == null
                ? prerequisiteRepository.findAll()
                : prerequisiteRepository.findBySubject_IdOrderByPrerequisiteSubject_SubjectCodeAsc(subjectId);
        return prerequisites.stream().map(this::toCoursePrerequisiteResponse).toList();
    }

    @Transactional
    public CoursePrerequisiteResponse createPrerequisite(CoursePrerequisiteRequest request) {
        if (request.subjectId().equals(request.prerequisiteSubjectId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A course cannot require itself");
        }
        prerequisiteRepository.findBySubject_IdAndPrerequisiteSubject_Id(request.subjectId(), request.prerequisiteSubjectId()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Prerequisite already exists");
        });
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        Subject prerequisite = subjectRepository.findById(request.prerequisiteSubjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prerequisite course not found"));

        UniversityCoursePrerequisite coursePrerequisite = new UniversityCoursePrerequisite();
        coursePrerequisite.setSubject(subject);
        coursePrerequisite.setPrerequisiteSubject(prerequisite);
        coursePrerequisite.setGroupCode(blankToNull(request.groupCode()));
        coursePrerequisite.setCreatedAt(LocalDateTime.now());
        UniversityCoursePrerequisite saved = prerequisiteRepository.save(coursePrerequisite);
        recordEvent("Academic management", "Prerequisite rule created", "UniversityCoursePrerequisite", saved.getId(), null, courseLabel(subject) + " requires " + courseLabel(prerequisite));
        return toCoursePrerequisiteResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseCorequisiteResponse> listCorequisites(Long subjectId) {
        List<UniversityCourseCorequisite> corequisites = subjectId == null
                ? corequisiteRepository.findAll()
                : corequisiteRepository.findBySubject_IdOrderByCorequisiteSubject_SubjectCodeAsc(subjectId);
        return corequisites.stream().map(this::toCourseCorequisiteResponse).toList();
    }

    @Transactional
    public CourseCorequisiteResponse createCorequisite(CourseCorequisiteRequest request) {
        if (request.subjectId().equals(request.corequisiteSubjectId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A course cannot require itself as a co-requisite");
        }
        corequisiteRepository.findBySubject_IdAndCorequisiteSubject_Id(request.subjectId(), request.corequisiteSubjectId()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Co-requisite already exists");
        });
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        Subject corequisite = subjectRepository.findById(request.corequisiteSubjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Co-requisite course not found"));

        UniversityCourseCorequisite courseCorequisite = new UniversityCourseCorequisite();
        courseCorequisite.setSubject(subject);
        courseCorequisite.setCorequisiteSubject(corequisite);
        courseCorequisite.setCreatedAt(LocalDateTime.now());
        UniversityCourseCorequisite saved = corequisiteRepository.save(courseCorequisite);
        recordEvent("Academic management", "Co-requisite rule created", "UniversityCourseCorequisite", saved.getId(), null, courseLabel(subject) + " requires co-selection with " + courseLabel(corequisite));
        return toCourseCorequisiteResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AcademicRecordResponse> listAcademicRecords(Long studentId) {
        List<UniversityAcademicRecord> records = studentId == null
                ? academicRecordRepository.findAll()
                : academicRecordRepository.findByStudent_IdOrderByCompletedAtDescIdDesc(studentId);
        return records.stream().map(this::toAcademicRecordResponse).toList();
    }

    @Transactional
    public AcademicRecordResponse createAcademicRecord(AcademicRecordRequest request) {
        academicRecordRepository.findByStudent_IdAndSubject_Id(request.studentId(), request.subjectId()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Academic record already exists for this student and course");
        });
        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (!student.isStudent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Academic records require a student user");
        }
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        LocalDateTime now = LocalDateTime.now();
        UniversityAcademicRecord record = new UniversityAcademicRecord();
        record.setStudent(student);
        record.setSubject(subject);
        record.setAcademicYear(normalizeAcademicYear(request.academicYear()));
        record.setSemester(request.semester() == null ? 1 : request.semester());
        record.setFinalGrade(request.finalGrade());
        record.setStatus(request.status() == null ? AcademicRecordStatus.COMPLETED : request.status());
        record.setCompletedAt(now);
        record.setCreatedAt(now);
        UniversityAcademicRecord saved = academicRecordRepository.save(record);
        recordEvent("Academic records", "Course completion recorded", "UniversityAcademicRecord", saved.getId(), student, courseLabel(subject));
        return toAcademicRecordResponse(saved);
    }

    @Transactional(readOnly = true)
    public AcademicPolicyResponse getActiveAcademicPolicy() {
        return toAcademicPolicyResponse(activeAcademicPolicy());
    }

    @Transactional
    public AcademicPolicyResponse updateActiveAcademicPolicy(AcademicPolicyRequest request) {
        if (request.minTermCredits() > request.maxTermCredits()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum credits cannot exceed maximum credits");
        }
        if (request.probationMaxTermCredits() > request.maxTermCredits()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Probation maximum credits cannot exceed maximum credits");
        }
        UniversityAcademicPolicy policy = activeAcademicPolicy();
        policy.setPolicyName(request.policyName().trim());
        policy.setMinTermCredits(request.minTermCredits());
        policy.setMaxTermCredits(request.maxTermCredits());
        policy.setProbationMaxTermCredits(request.probationMaxTermCredits());
        policy.setMinAverageGradeGoodStanding(request.minAverageGradeGoodStanding());
        policy.setBlockRegistrationWhenProbation(Boolean.TRUE.equals(request.blockRegistrationWhenProbation()));
        policy.setAllowRepeatCompletedCourses(Boolean.TRUE.equals(request.allowRepeatCompletedCourses()));
        policy.setActive(true);
        policy.setUpdatedAt(LocalDateTime.now());
        UniversityAcademicPolicy saved = academicPolicyRepository.save(policy);
        recordEvent("Academic policy", "Academic policy updated", "UniversityAcademicPolicy", saved.getId(), null, saved.getPolicyName());
        return toAcademicPolicyResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProgramRequirementResponse> listProgramRequirements(String programName) {
        List<UniversityProgramRequirement> requirements = programName == null || programName.isBlank()
                ? programRequirementRepository.findAllByOrderByProgramNameAscIdAsc()
                : programRequirementRepository.findByProgramNameIgnoreCaseAndActiveTrueOrderByIdAsc(programName.trim());
        return requirements.stream().map(this::toProgramRequirementResponse).toList();
    }

    @Transactional
    public ProgramRequirementResponse createProgramRequirement(ProgramRequirementRequest request) {
        LocalDateTime now = LocalDateTime.now();
        UniversityProgramRequirement requirement = new UniversityProgramRequirement();
        requirement.setProgramName(request.programName().trim());
        requirement.setRequirementName(request.requirementName().trim());
        requirement.setRequiredCredits(request.requiredCredits());
        requirement.setSubject(request.subjectId() == null ? null : subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found")));
        requirement.setActive(request.active() == null || Boolean.TRUE.equals(request.active()));
        requirement.setCreatedAt(now);
        requirement.setUpdatedAt(now);
        UniversityProgramRequirement saved = programRequirementRepository.save(requirement);
        recordEvent("Academic policy", "Program requirement created", "UniversityProgramRequirement", saved.getId(), null, saved.getProgramName());
        return toProgramRequirementResponse(saved);
    }

    @Transactional(readOnly = true)
    public DegreeAuditResponse getDegreeAudit(Long studentId, String programName) {
        if (programName == null || programName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Program name is required");
        }
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (!student.isStudent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Degree audit requires a student user");
        }
        List<UniversityProgramRequirement> requirements = programRequirementRepository
                .findByProgramNameIgnoreCaseAndActiveTrueOrderByIdAsc(programName.trim());
        List<UniversityAcademicRecord> completedRecords = academicRecordRepository.findByStudent_IdOrderByCompletedAtDescIdDesc(studentId).stream()
                .filter(record -> record.getStatus() == AcademicRecordStatus.COMPLETED)
                .toList();
        Set<Long> completedSubjectIds = completedRecords.stream()
                .map(UniversityAcademicRecord::getSubject)
                .filter(subject -> subject != null)
                .map(Subject::getId)
                .collect(java.util.stream.Collectors.toSet());
        int totalCompletedCredits = completedRecords.stream()
                .map(UniversityAcademicRecord::getSubject)
                .filter(subject -> subject != null)
                .mapToInt(this::creditsFor)
                .sum();
        List<DegreeAuditRequirementResponse> requirementResponses = requirements.stream()
                .map(requirement -> toDegreeAuditRequirementResponse(requirement, completedSubjectIds, totalCompletedCredits))
                .toList();
        int totalRequiredCredits = requirements.stream()
                .mapToInt(requirement -> safePositive(requirement.getRequiredCredits(), 0))
                .sum();
        int matchedRequiredCredits = requirementResponses.stream()
                .mapToInt(DegreeAuditRequirementResponse::completedCredits)
                .sum();
        int remainingCredits = Math.max(0, totalRequiredCredits - matchedRequiredCredits);
        double progressPercent = totalRequiredCredits == 0 ? 0.0 : Math.min(100.0, (matchedRequiredCredits * 100.0) / totalRequiredCredits);
        return new DegreeAuditResponse(
                student.getId(),
                student.getFullName().trim(),
                programName.trim(),
                totalRequiredCredits,
                totalCompletedCredits,
                matchedRequiredCredits,
                remainingCredits,
                Math.round(progressPercent * 10.0) / 10.0,
                totalRequiredCredits > 0 && remainingCredits == 0,
                requirementResponses);
    }

    @Transactional(readOnly = true)
    public List<FacultyProfileResponse> listFacultyProfiles() {
        return facultyProfileRepository.findAllByOrderByDepartmentAscFacultyUser_LastNameAscFacultyUser_FirstNameAscIdAsc().stream()
                .map(this::toFacultyProfileResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listDepartments() {
        return departmentRepository.findAllByOrderByNameAscIdAsc().stream()
                .map(this::toDepartmentResponse)
                .toList();
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        String name = request.name().trim();
        departmentRepository.findByCodeIgnoreCase(code).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department code already exists");
        });
        departmentRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department name already exists");
        });
        LocalDateTime now = LocalDateTime.now();
        UniversityDepartment department = new UniversityDepartment();
        department.setCode(code);
        department.setName(name);
        department.setActive(request.active() == null || Boolean.TRUE.equals(request.active()));
        department.setCreatedAt(now);
        department.setUpdatedAt(now);
        UniversityDepartment saved = departmentRepository.save(department);
        recordEvent("HR and faculty", "Department created", "UniversityDepartment", saved.getId(), null, saved.getName());
        return toDepartmentResponse(saved);
    }

    @Transactional
    public FacultyProfileResponse upsertFacultyProfile(FacultyProfileRequest request) {
        User faculty = userRepository.findById(request.facultyUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty user not found"));
        if (!faculty.isTeacher()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculty profile requires a faculty user");
        }
        LocalDateTime now = LocalDateTime.now();
        UniversityFacultyProfile profile = facultyProfileRepository.findByFacultyUser_Id(faculty.getId())
                .orElseGet(() -> {
                    UniversityFacultyProfile created = new UniversityFacultyProfile();
                    created.setFacultyUser(faculty);
                    created.setCreatedAt(now);
                    return created;
                });
        profile.setEmployeeNumber(blankToNull(request.employeeNumber()));
        profile.setDepartment(request.department().trim());
        profile.setAcademicRank(blankToNull(request.academicRank()));
        profile.setEmploymentStatus(request.employmentStatus() == null || request.employmentStatus().isBlank()
                ? "ACTIVE"
                : request.employmentStatus().trim().toUpperCase(Locale.ROOT));
        profile.setHireDate(request.hireDate());
        profile.setOfficeLocation(blankToNull(request.officeLocation()));
        profile.setWorkloadTargetCredits(request.workloadTargetCredits());
        profile.setUpdatedAt(now);
        UniversityFacultyProfile saved = facultyProfileRepository.save(profile);
        recordEvent("HR and faculty", "Faculty profile saved", "UniversityFacultyProfile", saved.getId(), faculty, saved.getDepartment());
        return toFacultyProfileResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FacultyWorkloadResponse> listFacultyWorkloads(Long facultyProfileId) {
        List<UniversityFacultyWorkload> workloads = facultyProfileId == null
                ? facultyWorkloadRepository.findAllByOrderByAcademicYearDescSemesterDescIdDesc()
                : facultyWorkloadRepository.findByFacultyProfile_IdOrderByAcademicYearDescSemesterDescIdDesc(facultyProfileId);
        return workloads.stream().map(this::toFacultyWorkloadResponse).toList();
    }

    @Transactional
    public FacultyWorkloadResponse createFacultyWorkload(FacultyWorkloadRequest request) {
        UniversityFacultyProfile profile = facultyProfileRepository.findById(request.facultyProfileId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty profile not found"));
        LocalDateTime now = LocalDateTime.now();
        UniversityFacultyWorkload workload = new UniversityFacultyWorkload();
        workload.setFacultyProfile(profile);
        workload.setAcademicYear(normalizeAcademicYear(request.academicYear()));
        workload.setSemester(request.semester());
        workload.setTeachingCredits(safePositive(request.teachingCredits(), 0));
        workload.setAdvisingCredits(safePositive(request.advisingCredits(), 0));
        workload.setResearchCredits(safePositive(request.researchCredits(), 0));
        workload.setCommitteeCredits(safePositive(request.committeeCredits(), 0));
        workload.setNotes(blankToNull(request.notes()));
        workload.setCreatedAt(now);
        workload.setUpdatedAt(now);
        UniversityFacultyWorkload saved = facultyWorkloadRepository.save(workload);
        recordEvent("HR and faculty", "Faculty workload recorded", "UniversityFacultyWorkload", saved.getId(), profile.getFacultyUser(), saved.getAcademicYear());
        return toFacultyWorkloadResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FacultyLeaveResponse> listFacultyLeaveRequests(Long facultyProfileId, String status) {
        List<UniversityFacultyLeaveRequest> requests;
        if (facultyProfileId != null) {
            requests = facultyLeaveRequestRepository.findByFacultyProfile_IdOrderByRequestedAtDescIdDesc(facultyProfileId);
        } else if (status != null && !status.isBlank()) {
            requests = facultyLeaveRequestRepository.findByStatusIgnoreCaseOrderByRequestedAtDescIdDesc(status.trim());
        } else {
            requests = facultyLeaveRequestRepository.findAllByOrderByRequestedAtDescIdDesc();
        }
        String normalizedStatus = status == null ? "" : status.trim();
        return requests.stream()
                .filter(request -> normalizedStatus.isBlank() || request.getStatus().equalsIgnoreCase(normalizedStatus))
                .map(this::toFacultyLeaveResponse)
                .toList();
    }

    @Transactional
    public FacultyLeaveResponse createFacultyLeaveRequest(FacultyLeaveRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave end date cannot be before start date");
        }
        UniversityFacultyProfile profile = facultyProfileRepository.findById(request.facultyProfileId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty profile not found"));
        UniversityFacultyLeaveRequest leave = new UniversityFacultyLeaveRequest();
        leave.setFacultyProfile(profile);
        leave.setLeaveType(request.leaveType().trim());
        leave.setStartDate(request.startDate());
        leave.setEndDate(request.endDate());
        leave.setStatus("REQUESTED");
        leave.setReason(blankToNull(request.reason()));
        leave.setRequestedAt(LocalDateTime.now());
        UniversityFacultyLeaveRequest saved = facultyLeaveRequestRepository.save(leave);
        recordEvent("HR and faculty", "Faculty leave requested", "UniversityFacultyLeaveRequest", saved.getId(), profile.getFacultyUser(), saved.getLeaveType());
        return toFacultyLeaveResponse(saved);
    }

    @Transactional
    public FacultyLeaveResponse decideFacultyLeaveRequest(Long id, FacultyLeaveDecisionRequest request) {
        UniversityFacultyLeaveRequest leave = facultyLeaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty leave request not found"));
        String status = request.status().trim().toUpperCase(Locale.ROOT);
        if (!List.of("APPROVED", "REJECTED", "CANCELLED").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave status must be APPROVED, REJECTED, or CANCELLED");
        }
        leave.setStatus(status);
        leave.setDecisionNotes(blankToNull(request.decisionNotes()));
        leave.setDecidedAt(LocalDateTime.now());
        UniversityFacultyLeaveRequest saved = facultyLeaveRequestRepository.save(leave);
        User faculty = saved.getFacultyProfile() == null ? null : saved.getFacultyProfile().getFacultyUser();
        recordEvent("HR and faculty", "Faculty leave " + status.toLowerCase(Locale.ROOT), "UniversityFacultyLeaveRequest", saved.getId(), faculty, saved.getDecisionNotes());
        return toFacultyLeaveResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> listServiceRequests(Long studentId, ServiceRequestStatus status, String assignedOffice, Long assignedUserId, String slaStatus) {
        Specification<UniversityServiceRequest> spec = Specification.where(null);
        if (studentId != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("student").get("id"), studentId));
        }
        if (status != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("status"), status));
        }
        if (assignedOffice != null && !assignedOffice.isBlank()) {
            spec = spec.and((root, query, builder) -> builder.equal(builder.lower(root.get("assignedOffice")), assignedOffice.trim().toLowerCase(Locale.ROOT)));
        }
        if (assignedUserId != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("assignedUser").get("id"), assignedUserId));
        }
        List<UniversityServiceRequest> requests = serviceRequestRepository.findAll(spec).stream()
                .sorted((left, right) -> {
                    LocalDateTime leftDate = left.getRequestedAt() == null ? LocalDateTime.MIN : left.getRequestedAt();
                    LocalDateTime rightDate = right.getRequestedAt() == null ? LocalDateTime.MIN : right.getRequestedAt();
                    int byRequestedAt = rightDate.compareTo(leftDate);
                    return byRequestedAt != 0 ? byRequestedAt : Long.compare(right.getId(), left.getId());
                })
                .toList();
        if (slaStatus != null && !slaStatus.isBlank()) {
            String normalizedSla = slaStatus.trim().toUpperCase(Locale.ROOT);
            requests = requests.stream()
                    .filter(request -> normalizedSla.equals(slaStatusFor(request)))
                    .toList();
        }
        return requests.stream().map(this::toServiceRequestResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceQueueResponse> listServiceQueues() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime soon = now.plusDays(1);
        return serviceRequestRepository.findAll().stream()
                .map(UniversityServiceRequest::getAssignedOffice)
                .filter(office -> office != null && !office.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .map(office -> new ServiceQueueResponse(
                        office,
                        serviceRequestRepository.countByAssignedOfficeAndStatusNotIn(office, TERMINAL_SERVICE_STATUSES),
                        serviceRequestRepository.countByAssignedOfficeAndAssignedUserIsNullAndStatusNotIn(office, TERMINAL_SERVICE_STATUSES),
                        serviceRequestRepository.countByAssignedOfficeAndDueAtBetweenAndStatusNotIn(office, now, soon, TERMINAL_SERVICE_STATUSES),
                        serviceRequestRepository.countByAssignedOfficeAndDueAtBeforeAndStatusNotIn(office, now, TERMINAL_SERVICE_STATUSES)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceTypeResponse> listServiceTypes(boolean activeOnly) {
        List<UniversityServiceType> serviceTypes = activeOnly
                ? serviceTypeRepository.findByActiveTrueOrderByNameAscIdAsc()
                : serviceTypeRepository.findAllByOrderByNameAscIdAsc();
        return serviceTypes.stream().map(this::toServiceTypeResponse).toList();
    }

    @Transactional
    public ServiceTypeResponse createServiceType(ServiceTypeRequest request) {
        String code = normalizeServiceTypeCode(request.code());
        serviceTypeRepository.findByCodeIgnoreCase(code).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Service type code already exists");
        });
        UniversityServiceType serviceType = new UniversityServiceType();
        applyServiceTypeRequest(serviceType, request, code);
        serviceType.setCreatedAt(LocalDateTime.now());
        serviceType.setUpdatedAt(LocalDateTime.now());
        UniversityServiceType saved = serviceTypeRepository.save(serviceType);
        recordEvent("Student services", "Service type created", "UniversityServiceType", saved.getId(), null, saved.getName());
        return toServiceTypeResponse(saved);
    }

    @Transactional
    public ServiceTypeResponse updateServiceType(Long id, ServiceTypeRequest request) {
        UniversityServiceType serviceType = serviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service type not found"));
        String code = normalizeServiceTypeCode(request.code());
        serviceTypeRepository.findByCodeIgnoreCase(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Service type code already exists");
                });
        applyServiceTypeRequest(serviceType, request, code);
        serviceType.setUpdatedAt(LocalDateTime.now());
        UniversityServiceType saved = serviceTypeRepository.save(serviceType);
        recordEvent("Student services", "Service type updated", "UniversityServiceType", saved.getId(), null, saved.getName());
        return toServiceTypeResponse(saved);
    }

    @Transactional
    public ServiceRequestResponse createServiceRequest(ServiceRequestCreateRequest request) {
        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (!student.isStudent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service requests require a student user");
        }

        LocalDateTime now = LocalDateTime.now();
        UniversityServiceType serviceType = resolveServiceType(request.requestType());
        UniversityServiceRequest serviceRequest = new UniversityServiceRequest();
        serviceRequest.setRequestNumber(nextServiceRequestNumber());
        serviceRequest.setStudent(student);
        serviceRequest.setRequestType(serviceType.getName());
        serviceRequest.setDescription(blankToNull(request.description()));
        serviceRequest.setAssignedOffice(serviceType.getDefaultOffice());
        serviceRequest.setDueAt(now.plusDays(serviceType.getSlaDays() == null ? 5 : Math.max(1, serviceType.getSlaDays())));
        serviceRequest.setRequestedAt(now);
        serviceRequest.setUpdatedAt(now);

        BigDecimal balance = outstandingBalanceFor(student);
        if (Boolean.TRUE.equals(serviceType.getRequiresFinanceClearance()) && balance.compareTo(BigDecimal.ZERO) > 0) {
            serviceRequest.setStatus(ServiceRequestStatus.ON_HOLD);
            serviceRequest.setHoldReason("Outstanding finance balance: " + balance);
        } else {
            serviceRequest.setStatus(ServiceRequestStatus.REQUESTED);
        }

        UniversityServiceRequest saved = serviceRequestRepository.save(serviceRequest);
        recordServiceRequestHistory(saved, "CREATED", null, saved.getStatus(), "Request created");
        recordEvent("Student services", "Service request created", "UniversityServiceRequest", saved.getId(), student, saved.getRequestType());
        return toServiceRequestResponse(saved);
    }

    @Transactional
    public ServiceRequestResponse updateServiceRequestStatus(Long id, ServiceRequestStatusRequest request) {
        UniversityServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service request not found"));
        if (serviceRequest.getStatus() == ServiceRequestStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Delivered requests cannot be changed");
        }

        ServiceRequestStatus nextStatus = request.status();
        if (nextStatus == ServiceRequestStatus.APPROVED || nextStatus == ServiceRequestStatus.REVIEW || nextStatus == ServiceRequestStatus.DELIVERED) {
            BigDecimal balance = outstandingBalanceFor(serviceRequest.getStudent());
            UniversityServiceType serviceType = resolveServiceType(serviceRequest.getRequestType());
            if (Boolean.TRUE.equals(serviceType.getRequiresFinanceClearance()) && balance.compareTo(BigDecimal.ZERO) > 0) {
                ServiceRequestStatus fromStatus = serviceRequest.getStatus();
                serviceRequest.setStatus(ServiceRequestStatus.ON_HOLD);
                serviceRequest.setHoldReason("Outstanding finance balance: " + balance);
                serviceRequest.setUpdatedAt(LocalDateTime.now());
                UniversityServiceRequest saved = serviceRequestRepository.save(serviceRequest);
                recordServiceRequestHistory(saved, "HELD", fromStatus, saved.getStatus(), saved.getHoldReason());
                recordEvent("Student services", "Service request held", "UniversityServiceRequest", saved.getId(), saved.getStudent(), saved.getHoldReason());
                return toServiceRequestResponse(saved);
            }
        }
        if ((nextStatus == ServiceRequestStatus.APPROVED || nextStatus == ServiceRequestStatus.DELIVERED)
                && attachmentRequiredFor(serviceRequest)
                && !attachmentSatisfiedFor(serviceRequest)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Required attachment must be uploaded before approval or delivery");
        }

        ServiceRequestStatus fromStatus = serviceRequest.getStatus();
        serviceRequest.setStatus(nextStatus);
        serviceRequest.setAssignedOffice(firstNonBlank(request.assignedOffice(), serviceRequest.getAssignedOffice()));
        serviceRequest.setAssignedUser(resolveAssignedUser(request.assignedUserId(), serviceRequest.getAssignedUser()));
        serviceRequest.setHoldReason(nextStatus == ServiceRequestStatus.ON_HOLD ? blankToNull(request.notes()) : null);
        serviceRequest.setUpdatedAt(LocalDateTime.now());
        serviceRequest.setCompletedAt(nextStatus == ServiceRequestStatus.DELIVERED ? LocalDateTime.now() : null);
        UniversityServiceRequest saved = serviceRequestRepository.save(serviceRequest);
        recordServiceRequestHistory(saved, "STATUS_CHANGED", fromStatus, nextStatus, blankToNull(request.notes()));
        recordEvent("Student services", "Service request status changed", "UniversityServiceRequest", saved.getId(), saved.getStudent(), nextStatus.name());
        return toServiceRequestResponse(saved);
    }

    @Transactional
    public GraduationClearanceResponse evaluateGraduationClearance(Long id, GraduationClearanceRequest request) {
        UniversityServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service request not found"));
        String normalizedType = normalizeServiceTypeCode(serviceRequest.getRequestType());
        if (!normalizedType.contains("GRADUATION")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Graduation clearance evaluation requires a graduation request");
        }

        DegreeAuditResponse degreeAudit = getDegreeAudit(serviceRequest.getStudent().getId(), request.programName());
        BigDecimal balance = outstandingBalanceFor(serviceRequest.getStudent());
        boolean attachmentRequired = attachmentRequiredFor(serviceRequest);
        boolean attachmentSatisfied = !attachmentRequired || attachmentSatisfiedFor(serviceRequest);
        List<String> missingRequirements = degreeAudit.requirements().stream()
                .filter(requirement -> !requirement.satisfied())
                .map(DegreeAuditRequirementResponse::requirementName)
                .toList();
        boolean eligible = Boolean.TRUE.equals(degreeAudit.graduationEligible())
                && balance.compareTo(BigDecimal.ZERO) <= 0
                && attachmentSatisfied;

        ServiceRequestStatus fromStatus = serviceRequest.getStatus();
        if (eligible) {
            serviceRequest.setStatus(ServiceRequestStatus.APPROVED);
            serviceRequest.setHoldReason(null);
        } else {
            serviceRequest.setStatus(ServiceRequestStatus.ON_HOLD);
            serviceRequest.setHoldReason(graduationClearanceHoldReason(degreeAudit, balance, attachmentRequired, attachmentSatisfied));
        }
        serviceRequest.setAssignedOffice(firstNonBlank(serviceRequest.getAssignedOffice(), "Registrar"));
        serviceRequest.setUpdatedAt(LocalDateTime.now());
        UniversityServiceRequest saved = serviceRequestRepository.save(serviceRequest);
        recordServiceRequestHistory(saved, "GRADUATION_CLEARANCE_EVALUATED", fromStatus, saved.getStatus(), saved.getHoldReason());
        recordEvent("Student services", "Graduation clearance evaluated", "UniversityServiceRequest", saved.getId(), saved.getStudent(), eligible ? "Eligible" : saved.getHoldReason());

        return new GraduationClearanceResponse(
                saved.getId(),
                saved.getRequestNumber(),
                saved.getStudent().getId(),
                saved.getStudent().getFullName().trim(),
                degreeAudit.programName(),
                saved.getStatus(),
                eligible,
                balance,
                attachmentRequired,
                attachmentSatisfied,
                degreeAudit.remainingCredits(),
                missingRequirements,
                degreeAudit);
    }

    @Transactional
    public ServiceRequestResponse assignServiceRequest(Long id, ServiceRequestAssignmentRequest request) {
        UniversityServiceRequest serviceRequest = getServiceRequest(id);
        if (TERMINAL_SERVICE_STATUSES.contains(serviceRequest.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Completed requests cannot be reassigned");
        }
        String previousOffice = serviceRequest.getAssignedOffice();
        User previousUser = serviceRequest.getAssignedUser();
        serviceRequest.setAssignedOffice(firstNonBlank(request.assignedOffice(), serviceRequest.getAssignedOffice()));
        serviceRequest.setAssignedUser(resolveAssignedUser(request.assignedUserId(), serviceRequest.getAssignedUser()));
        serviceRequest.setUpdatedAt(LocalDateTime.now());
        UniversityServiceRequest saved = serviceRequestRepository.save(serviceRequest);
        String previousAssignee = previousUser == null ? "unassigned" : previousUser.getFullName().trim();
        String nextAssignee = saved.getAssignedUser() == null ? "unassigned" : saved.getAssignedUser().getFullName().trim();
        recordServiceRequestHistory(
                saved,
                "ASSIGNED",
                saved.getStatus(),
                saved.getStatus(),
                "Office: " + firstNonBlank(previousOffice, "-") + " -> " + firstNonBlank(saved.getAssignedOffice(), "-")
                        + ", assignee: " + previousAssignee + " -> " + nextAssignee
                        + (request.notes() == null || request.notes().isBlank() ? "" : ". " + request.notes().trim()));
        recordEvent("Student services", "Service request assigned", "UniversityServiceRequest", saved.getId(), saved.getStudent(), nextAssignee);
        return toServiceRequestResponse(saved);
    }

    @Transactional(readOnly = true)
    public ServiceRequestDetailResponse getServiceRequestDetail(Long id) {
        UniversityServiceRequest serviceRequest = getServiceRequest(id);
        return toServiceRequestDetailResponse(serviceRequest);
    }

    @Transactional(readOnly = true)
    public Long getServiceRequestStudentId(Long id) {
        UniversityServiceRequest serviceRequest = getServiceRequest(id);
        return serviceRequest.getStudent() == null ? null : serviceRequest.getStudent().getId();
    }

    @Transactional
    public ServiceRequestCommentResponse addServiceRequestComment(Long id, ServiceRequestCommentRequest request) {
        UniversityServiceRequest serviceRequest = getServiceRequest(id);
        User actor = currentActor();
        UniversityServiceRequestComment comment = new UniversityServiceRequestComment();
        comment.setRequest(serviceRequest);
        comment.setAuthor(actor);
        comment.setCommentText(request.commentText().trim());
        comment.setInternal(Boolean.TRUE.equals(request.internal()));
        comment.setCreatedAt(LocalDateTime.now());
        UniversityServiceRequestComment saved = serviceRequestCommentRepository.save(comment);
        recordServiceRequestHistory(serviceRequest, "COMMENT_ADDED", serviceRequest.getStatus(), serviceRequest.getStatus(), saved.getCommentText());
        recordEvent("Student services", "Service request comment added", "UniversityServiceRequest", serviceRequest.getId(), serviceRequest.getStudent(), saved.getCommentText());
        return toServiceRequestCommentResponse(saved);
    }

    @Transactional
    public List<ServiceRequestAttachmentResponse> uploadServiceRequestAttachments(Long id, List<MultipartFile> files) {
        UniversityServiceRequest serviceRequest = getServiceRequest(id);
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        User actor = currentActor();
        List<ServiceRequestAttachmentResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String storedPath = fileStorageService.store(file, "student_services");
            UniversityServiceRequestAttachment attachment = new UniversityServiceRequestAttachment();
            attachment.setRequest(serviceRequest);
            attachment.setUploadedBy(actor);
            attachment.setOriginalFilename(safeOriginalFilename(file));
            attachment.setStoredPath(storedPath);
            attachment.setMimeType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
            attachment.setSizeBytes(file.getSize());
            attachment.setUploadedAt(LocalDateTime.now());
            responses.add(toServiceRequestAttachmentResponse(serviceRequestAttachmentRepository.save(attachment)));
        }
        if (!responses.isEmpty()) {
            recordServiceRequestHistory(serviceRequest, "ATTACHMENT_UPLOADED", serviceRequest.getStatus(), serviceRequest.getStatus(), responses.size() + " attachment(s)");
            recordEvent("Student services", "Service request attachment uploaded", "UniversityServiceRequest", serviceRequest.getId(), serviceRequest.getStudent(), responses.size() + " attachment(s)");
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<UniversityErpEventLogResponse> listRecentEvents() {
        return eventLogRepository.findTop20ByOrderByCreatedAtDescIdDesc().stream()
                .map(this::toEventLogResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UniversityIntegrationStatusResponse> listIntegrations() {
        return List.of(
                integrationStatus("lms", "Learning Management System", "Outbound roster and course catalog", "READY", "Course roster sync payload"),
                integrationStatus("bank", "Bank payment gateway", "Inbound payment confirmation", "READY", "Invoice payment callback payload"),
                integrationStatus("notification", "Notification service", "Outbound email/SMS notices", "READY", "Applicant and service request notification payload"),
                integrationStatus("government", "Government reporting", "Outbound statutory report", "READY", "Enrollment and finance summary payload")
        );
    }

    @Transactional(readOnly = true)
    public List<UniversityIntegrationRunResponse> listIntegrationRuns() {
        return integrationRunRepository.findTop20ByOrderByExchangedAtDescIdDesc().stream()
                .map(this::toIntegrationRunResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IntegrationConnectionResponse> listIntegrationConnections() {
        return integrationConnectionRepository.findAllByOrderByIntegrationKeyAsc().stream()
                .map(this::toIntegrationConnectionResponse)
                .toList();
    }

    @Transactional
    public IntegrationConnectionResponse saveIntegrationConnection(IntegrationConnectionRequest request) {
        String key = request.integrationKey().trim().toLowerCase(Locale.ROOT);
        LocalDateTime now = LocalDateTime.now();
        UniversityIntegrationConnection connection = integrationConnectionRepository.findByIntegrationKeyIgnoreCase(key)
                .orElseGet(() -> {
                    UniversityIntegrationConnection created = new UniversityIntegrationConnection();
                    created.setIntegrationKey(key);
                    created.setCreatedAt(now);
                    return created;
        });
        connection.setDisplayName(request.displayName().trim());
        connection.setEndpointUrl(blankToNull(request.endpointUrl()));
        connection.setAdapterMode(normalizeAdapterMode(request.adapterMode()));
        connection.setAuthType(normalizeAuthType(request.authType()));
        connection.setSecretRef(blankToNull(request.secretRef()));
        validateAdapterConfiguration(connection);
        connection.setEnabled(request.enabled() == null || Boolean.TRUE.equals(request.enabled()));
        connection.setLastStatus(Boolean.TRUE.equals(connection.getEnabled()) ? "READY" : "DISABLED");
        connection.setUpdatedAt(now);
        UniversityIntegrationConnection saved = integrationConnectionRepository.save(connection);
        recordEvent("Integration", "Integration connection saved", "UniversityIntegrationConnection", saved.getId(), null, saved.getIntegrationKey());
        return toIntegrationConnectionResponse(saved);
    }

    @Transactional
    public UniversityIntegrationRunResponse runIntegration(String key) {
        UniversityIntegrationStatusResponse integration = listIntegrations().stream()
                .filter(candidate -> candidate.key().equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Integration not found"));
        UniversityIntegrationConnection connection = integrationConnectionRepository.findByIntegrationKeyIgnoreCase(integration.key())
                .orElse(null);
        if (connection != null && !Boolean.TRUE.equals(connection.getEnabled())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Integration connection is disabled");
        }
        UniversityIntegrationRun run = new UniversityIntegrationRun();
        run.setIntegrationKey(integration.key());
        run.setIntegrationName(integration.name());
        run.setDirection(integration.direction());
        String payload = adapterPayload(integration.key(), connection);
        run.setPayload(payload);
        if (connection != null && "HTTP".equalsIgnoreCase(connection.getAdapterMode())) {
            validateAdapterConfiguration(connection);
            UniversityHttpIntegrationAdapter.AdapterResult result = httpIntegrationAdapter.post(connection, payload);
            run.setStatus(result.success() ? "HTTP_SUCCESS" : "FAILED");
            run.setResultMessage(result.success()
                    ? "HTTP adapter posted ERP payload successfully with status " + result.statusCode()
                    : "HTTP adapter exchange failed");
            run.setErrorMessage(result.success() ? null : firstNonBlank(result.errorMessage(), "HTTP status " + result.statusCode()));
            updateConnectionStatus(integration.key(), run.getStatus());
        } else {
            run.setStatus("SIMULATED_SUCCESS");
            run.setResultMessage("Demo adapter exchanged ERP data successfully");
        }
        run.setActorUser(currentActor());
        run.setExchangedAt(LocalDateTime.now());
        UniversityIntegrationRun saved = integrationRunRepository.save(run);
        recordEvent("Integration", "Integration exchange recorded", "UniversityIntegrationRun", saved.getId(), null, integration.name());
        return toIntegrationRunResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<IntegrationSmokeTestResponse> smokeTestIntegrations() {
        return integrationConnectionRepository.findAllByOrderByIntegrationKeyAsc().stream()
                .map(this::smokeTestConnection)
                .toList();
    }

    @Transactional
    public UniversityIntegrationRunResponse simulateIntegrationFailure(String key) {
        UniversityIntegrationStatusResponse integration = listIntegrations().stream()
                .filter(candidate -> candidate.key().equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Integration not found"));
        UniversityIntegrationRun run = new UniversityIntegrationRun();
        run.setIntegrationKey(integration.key());
        run.setIntegrationName(integration.name());
        run.setDirection(integration.direction());
        run.setStatus("FAILED");
        run.setPayload(buildIntegrationPayload(integration.key()));
        run.setResultMessage("Demo adapter failure recorded");
        run.setErrorMessage("Simulated connection timeout");
        run.setRetryCount(0);
        run.setActorUser(currentActor());
        run.setExchangedAt(LocalDateTime.now());
        UniversityIntegrationRun saved = integrationRunRepository.save(run);
        updateConnectionStatus(integration.key(), "FAILED");
        recordEvent("Integration", "Integration failure recorded", "UniversityIntegrationRun", saved.getId(), null, integration.name());
        return toIntegrationRunResponse(saved);
    }

    @Transactional
    public UniversityIntegrationRunResponse retryIntegrationRun(Long runId) {
        UniversityIntegrationRun failed = integrationRunRepository.findById(runId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Integration run not found"));
        UniversityIntegrationRun retry = new UniversityIntegrationRun();
        retry.setIntegrationKey(failed.getIntegrationKey());
        retry.setIntegrationName(failed.getIntegrationName());
        retry.setDirection(failed.getDirection());
        retry.setStatus("RETRIED_SUCCESS");
        retry.setPayload(failed.getPayload());
        retry.setResultMessage("Retry completed successfully");
        retry.setRetryCount(safePositive(failed.getRetryCount(), 0) + 1);
        retry.setActorUser(currentActor());
        retry.setExchangedAt(LocalDateTime.now());
        UniversityIntegrationRun saved = integrationRunRepository.save(retry);
        updateConnectionStatus(saved.getIntegrationKey(), saved.getStatus());
        recordEvent("Integration", "Integration run retried", "UniversityIntegrationRun", saved.getId(), null, saved.getIntegrationName());
        return toIntegrationRunResponse(saved);
    }

    @Transactional
    public BankPaymentCallbackResponse simulateBankPaymentCallback(BankPaymentCallbackRequest request) {
        FeeInvoice invoice = resolveBankCallbackInvoice(request == null ? null : request.invoiceId());
        if (invoice.getStatus() == FeeInvoice.Status.CANCELLED || invoice.getStatus() == FeeInvoice.Status.WAIVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payments cannot be recorded against cancelled or waived invoices");
        }
        BigDecimal balance = totalInvoiceAmount(invoice).subtract(totalPaid(invoice)).max(BigDecimal.ZERO);
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice has no outstanding balance");
        }
        BigDecimal amount = request == null || request.amount() == null ? balance : request.amount().min(balance);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
        String reference = blankToNull(request == null ? null : request.referenceNumber());
        if (reference == null) {
            reference = "BANK-" + NUMBER_DATE.format(LocalDate.now()) + "-" + invoice.getId();
        }

        FeePayment payment = new FeePayment();
        payment.setInvoice(invoice);
        payment.setStudent(invoice.getStudent());
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDate.now());
        payment.setMethod(FeePayment.Method.ONLINE);
        payment.setStatus(FeePayment.Status.COMPLETED);
        payment.setReferenceNumber(reference);
        payment.setNotes("University ERP bank payment callback");
        payment.setRecordedBy(currentActor());
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        BigDecimal remaining = totalInvoiceAmount(invoice).subtract(totalPaid(invoice)).max(BigDecimal.ZERO);
        invoice.setStatus(remaining.compareTo(BigDecimal.ZERO) <= 0 ? FeeInvoice.Status.PAID : FeeInvoice.Status.PARTIALLY_PAID);
        invoice.setUpdatedAt(LocalDateTime.now());
        FeeInvoice savedInvoice = invoiceRepository.save(invoice);

        UniversityIntegrationRun run = new UniversityIntegrationRun();
        run.setIntegrationKey("bank");
        run.setIntegrationName("Bank payment gateway");
        run.setDirection("Inbound payment confirmation");
        run.setStatus("COMPLETED");
        run.setPayload(jsonPayload(
                "\"invoiceId\":" + savedInvoice.getId(),
                "\"invoiceNumber\":\"" + savedInvoice.getInvoiceNumber() + "\"",
                "\"paidAmount\":\"" + amount + "\"",
                "\"remainingBalance\":\"" + remaining + "\"",
                "\"referenceNumber\":\"" + reference + "\""));
        run.setResultMessage("Bank callback recorded a completed finance payment");
        run.setActorUser(currentActor());
        run.setExchangedAt(LocalDateTime.now());
        UniversityIntegrationRun savedRun = integrationRunRepository.save(run);
        recordEvent("Integration", "Bank payment callback recorded", "UniversityIntegrationRun", savedRun.getId(), savedInvoice.getStudent(), savedInvoice.getInvoiceNumber());

        return new BankPaymentCallbackResponse(
                savedInvoice.getId(),
                savedInvoice.getInvoiceNumber(),
                savedInvoice.getStatus().name(),
                amount,
                reference,
                toIntegrationRunResponse(savedRun));
    }

    @Transactional
    public LmsRosterExportResponse exportLmsRoster() {
        List<UniversityCourseSelection> rosterSelections = selectionRepository.findAll().stream()
                .filter(selection -> selection.getStatus() != CourseSelectionStatus.DROPPED)
                .toList();
        String payload = buildLmsRosterExportPayload(rosterSelections);

        UniversityIntegrationRun run = new UniversityIntegrationRun();
        run.setIntegrationKey("lms");
        run.setIntegrationName("Learning Management System");
        run.setDirection("Outbound roster and course catalog");
        run.setStatus("COMPLETED");
        run.setPayload(payload);
        run.setResultMessage("LMS roster export generated from course selections");
        run.setActorUser(currentActor());
        run.setExchangedAt(LocalDateTime.now());
        UniversityIntegrationRun savedRun = integrationRunRepository.save(run);
        recordEvent("Integration", "LMS roster exported", "UniversityIntegrationRun", savedRun.getId(), null, rosterSelections.size() + " roster row(s)");

        return new LmsRosterExportResponse(rosterSelections.size(), toIntegrationRunResponse(savedRun));
    }

    @Transactional
    public NotificationDispatchResponse dispatchNotifications() {
        List<String> rows = new ArrayList<>();
        applicantRepository.findAllByOrderBySubmittedAtDescIdDesc().stream()
                .filter(applicant -> applicant.getStatus() == ApplicantStatus.SUBMITTED
                        || applicant.getStatus() == ApplicantStatus.SCREENING
                        || applicant.getStatus() == ApplicantStatus.ACCEPTED)
                .limit(50)
                .map(this::applicantNotificationJson)
                .forEach(rows::add);
        serviceRequestRepository.findAllByOrderByRequestedAtDescIdDesc().stream()
                .filter(request -> request.getStatus() == ServiceRequestStatus.REQUESTED
                        || request.getStatus() == ServiceRequestStatus.REVIEW
                        || request.getStatus() == ServiceRequestStatus.APPROVED
                        || request.getStatus() == ServiceRequestStatus.ON_HOLD)
                .limit(50)
                .map(this::serviceRequestNotificationJson)
                .forEach(rows::add);

        UniversityIntegrationRun run = new UniversityIntegrationRun();
        run.setIntegrationKey("notification");
        run.setIntegrationName("Notification service");
        run.setDirection("Outbound email/SMS notices");
        run.setStatus("COMPLETED");
        run.setPayload("{"
                + "\"dispatchType\":\"demo_notification_batch\","
                + "\"notificationCount\":" + rows.size() + ","
                + "\"generatedAt\":\"" + LocalDateTime.now() + "\","
                + "\"notifications\":[" + String.join(",", rows) + "]"
                + "}");
        run.setResultMessage("Notification dispatch records generated from admissions and student services");
        run.setActorUser(currentActor());
        run.setExchangedAt(LocalDateTime.now());
        UniversityIntegrationRun savedRun = integrationRunRepository.save(run);
        recordEvent("Integration", "Notification dispatch recorded", "UniversityIntegrationRun", savedRun.getId(), null, rows.size() + " notification(s)");
        return new NotificationDispatchResponse(rows.size(), toIntegrationRunResponse(savedRun));
    }

    @Transactional
    public GovernmentReportExportResponse exportGovernmentReport() {
        UniversityReportResponse summary = getReportSummary();
        String reportPeriod = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String payload = "{"
                + "\"reportType\":\"university_statutory_summary\","
                + "\"reportPeriod\":\"" + reportPeriod + "\","
                + "\"generatedAt\":\"" + LocalDateTime.now() + "\","
                + "\"enrollment\":{\"applicants\":" + summary.applicants()
                + ",\"acceptedApplicants\":" + summary.acceptedApplicants()
                + ",\"convertedStudents\":" + summary.convertedStudents() + "},"
                + "\"academic\":{\"courseSelections\":" + summary.selectedCourses()
                + ",\"billedSelections\":" + summary.billedSelections()
                + ",\"academicRecords\":" + summary.academicRecords()
                + ",\"prerequisiteRules\":" + summary.prerequisiteRules() + "},"
                + "\"finance\":{\"invoices\":" + summary.financeInvoices()
                + ",\"billedAmount\":\"" + summary.billedAmount()
                + "\",\"outstandingBalance\":\"" + summary.outstandingBalance() + "\"},"
                + "\"studentServices\":{\"requests\":" + summary.serviceRequests()
                + ",\"openRequests\":" + summary.openServiceRequests()
                + ",\"heldRequests\":" + summary.heldServiceRequests() + "},"
                + "\"governance\":{\"auditEvents\":" + summary.auditEvents()
                + ",\"integrationRuns\":" + integrationRunRepository.count() + "}"
                + "}";

        UniversityIntegrationRun run = new UniversityIntegrationRun();
        run.setIntegrationKey("government");
        run.setIntegrationName("Government reporting");
        run.setDirection("Outbound statutory report");
        run.setStatus("COMPLETED");
        run.setPayload(payload);
        run.setResultMessage("Government statutory summary export generated");
        run.setActorUser(currentActor());
        run.setExchangedAt(LocalDateTime.now());
        UniversityIntegrationRun savedRun = integrationRunRepository.save(run);
        recordEvent("Integration", "Government report exported", "UniversityIntegrationRun", savedRun.getId(), null, reportPeriod);
        return new GovernmentReportExportResponse(reportPeriod, 5, toIntegrationRunResponse(savedRun));
    }

    @Transactional(readOnly = true)
    public UniversityReportResponse getReportSummary() {
        long converted = applicantRepository.countByStatus(ApplicantStatus.CONVERTED);
        long accepted = applicantRepository.countByStatus(ApplicantStatus.ACCEPTED) + converted;
        long billedSelections = selectionRepository.countByStatus(CourseSelectionStatus.BILLED);

        BigDecimal billedAmount = BigDecimal.ZERO;
        BigDecimal outstanding = BigDecimal.ZERO;
        for (FeeInvoice invoice : invoiceRepository.findAll()) {
            BigDecimal total = totalInvoiceAmount(invoice);
            billedAmount = billedAmount.add(total);
            if (invoice.getStatus() != FeeInvoice.Status.CANCELLED && invoice.getStatus() != FeeInvoice.Status.WAIVED) {
                outstanding = outstanding.add(total.subtract(totalPaid(invoice)));
            }
        }

        return new UniversityReportResponse(
                applicantRepository.count(),
                accepted,
                converted,
                selectionRepository.count(),
                billedSelections,
                serviceRequestRepository.count(),
                serviceRequestRepository.countByStatus(ServiceRequestStatus.REQUESTED)
                        + serviceRequestRepository.countByStatus(ServiceRequestStatus.REVIEW)
                        + serviceRequestRepository.countByStatus(ServiceRequestStatus.APPROVED)
                        + serviceRequestRepository.countByStatus(ServiceRequestStatus.ON_HOLD),
                serviceRequestRepository.countByStatus(ServiceRequestStatus.ON_HOLD),
                prerequisiteRepository.count(),
                academicRecordRepository.count(),
                eventLogRepository.count(),
                invoiceRepository.count(),
                billedAmount,
                outstanding.max(BigDecimal.ZERO),
                admissionsBreakdown(),
                serviceRequestBreakdown(),
                financeBreakdown(),
                serviceQueueBreakdown(),
                academicPolicyBreakdown(),
                programRequirementBreakdown());
    }

    @Transactional(readOnly = true)
    public List<UniversityReportDefinitionResponse> listReportDefinitions() {
        return reportDefinitionRepository.findByActiveTrueOrderByCategoryAscNameAsc().stream()
                .filter(definition -> canCurrentActorViewReport(definition.getReportKey()))
                .map(this::toReportDefinitionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UniversityReportRunResponse> listReportRuns() {
        return reportRunRepository.findTop20ByOrderByGeneratedAtDescIdDesc().stream()
                .map(this::toReportRunResponse)
                .toList();
    }

    @Transactional
    public UniversityReportRunResponse runReport(String reportKey) {
        UniversityReportDefinition definition = reportDefinitionRepository.findByReportKeyAndActiveTrue(reportKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report definition not found"));
        if (!canCurrentActorViewReport(definition.getReportKey())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Report is not visible for the current role");
        }
        String snapshot = buildReportSnapshot(definition.getReportKey());
        UniversityReportRun run = new UniversityReportRun();
        run.setReportDefinition(definition);
        run.setStatus("GENERATED");
        run.setFilters("Current live ERP demo scope");
        run.setSnapshotPayload(snapshot);
        run.setRowCount(reportRowCount(definition.getReportKey()));
        run.setActorUser(currentActor());
        run.setGeneratedAt(LocalDateTime.now());
        UniversityReportRun saved = reportRunRepository.save(run);
        recordEvent("Reporting", "Report generated", "UniversityReportRun", saved.getId(), null, definition.getName());
        return toReportRunResponse(saved);
    }

    @Transactional(readOnly = true)
    public String exportReportCsv(String reportKey, String academicYear, Integer semester, String status) {
        UniversityReportDefinition definition = reportDefinitionRepository.findByReportKeyAndActiveTrue(reportKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report definition not found"));
        if (!canCurrentActorViewReport(definition.getReportKey())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Report is not visible for the current role");
        }
        String filters = "academicYear=" + firstNonBlank(academicYear, "ALL")
                + ";semester=" + (semester == null ? "ALL" : semester)
                + ";status=" + firstNonBlank(status, "ALL");
        return switch (definition.getReportKey()) {
            case "enrollment_funnel" -> csvRows(
                    "report,filter,label,count,amount",
                    admissionsBreakdown().stream()
                            .map(row -> csvLine(definition.getName(), filters, row.label(), row.count(), row.amount()))
                            .toList());
            case "finance_balance" -> csvRows(
                    "report,filter,label,count,amount",
                    financeBreakdown().stream()
                            .filter(row -> status == null || status.isBlank() || row.label().equalsIgnoreCase(status))
                            .map(row -> csvLine(definition.getName(), filters, row.label(), row.count(), row.amount()))
                            .toList());
            case "student_services_sla" -> csvRows(
                    "report,filter,label,count,amount",
                    serviceRequestBreakdown().stream()
                            .filter(row -> status == null || status.isBlank() || row.label().equalsIgnoreCase(status))
                            .map(row -> csvLine(definition.getName(), filters, row.label(), row.count(), row.amount()))
                            .toList());
            case "faculty_workload" -> csvRows(
                    "report,filter,label,count,amount",
                    List.of(csvLine(definition.getName(), filters, "Faculty profiles", facultyProfileRepository.count(), null),
                            csvLine(definition.getName(), filters, "Teaching assignments", teachingAssignmentRepository.count(), null)));
            case "integration_health" -> csvRows(
                    "report,filter,label,count,amount",
                    listIntegrations().stream()
                            .map(row -> csvLine(definition.getName(), filters, row.name() + " " + row.status(), 1, null))
                            .toList());
            default -> csvRows("report,filter,label,count,amount", List.of(csvLine(definition.getName(), filters, "Rows", 1, null)));
        };
    }

    @Transactional(readOnly = true)
    public List<UniversityReportDetailRowResponse> listReportDetails(String reportKey, String academicYear, Integer semester, String status) {
        UniversityReportDefinition definition = reportDefinitionRepository.findByReportKeyAndActiveTrue(reportKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report definition not found"));
        if (!canCurrentActorViewReport(definition.getReportKey())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Report is not visible for the current role");
        }
        String normalizedStatus = status == null ? "" : status.trim();
        return switch (definition.getReportKey()) {
            case "enrollment_funnel" -> applicantRepository.findAllByOrderBySubmittedAtDescIdDesc().stream()
                    .filter(applicant -> normalizedStatus.isBlank() || applicant.getStatus().name().equalsIgnoreCase(normalizedStatus))
                    .limit(25)
                    .map(applicant -> new UniversityReportDetailRowResponse(
                            definition.getReportKey(),
                            "UniversityApplicant",
                            applicant.getId(),
                            applicant.getFirstName() + " " + applicant.getLastName(),
                            applicant.getProgram(),
                            applicant.getStatus().name(),
                            null,
                            firstNonBlank(applicant.getDecisionNotes(), applicant.getEmail())))
                    .toList();
            case "finance_balance" -> invoiceRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                    .filter(invoice -> normalizedStatus.isBlank() || invoice.getStatus().name().equalsIgnoreCase(normalizedStatus))
                    .limit(25)
                    .map(invoice -> new UniversityReportDetailRowResponse(
                            definition.getReportKey(),
                            "FeeInvoice",
                            invoice.getId(),
                            invoice.getInvoiceNumber(),
                            invoice.getStudent() == null ? "-" : invoice.getStudent().getFullName().trim(),
                            invoice.getStatus().name(),
                            totalInvoiceAmount(invoice).subtract(totalPaid(invoice)),
                            invoice.getDueDate() == null ? null : "Due " + invoice.getDueDate()))
                    .toList();
            case "student_services_sla" -> serviceRequestRepository.findAll().stream()
                    .filter(request -> normalizedStatus.isBlank() || request.getStatus().name().equalsIgnoreCase(normalizedStatus) || slaStatusFor(request).equalsIgnoreCase(normalizedStatus))
                    .sorted((left, right) -> safeDateTime(right.getRequestedAt()).compareTo(safeDateTime(left.getRequestedAt())))
                    .limit(25)
                    .map(request -> new UniversityReportDetailRowResponse(
                            definition.getReportKey(),
                            "UniversityServiceRequest",
                            request.getId(),
                            request.getRequestType(),
                            request.getStudent() == null ? "-" : request.getStudent().getFullName().trim(),
                            request.getStatus().name(),
                            null,
                            firstNonBlank(request.getAssignedOffice(), "-") + " | " + slaStatusFor(request)))
                    .toList();
            case "faculty_workload" -> facultyProfileRepository.findAll().stream()
                    .limit(25)
                    .map(profile -> {
                        FacultyProfileResponse profileResponse = toFacultyProfileResponse(profile);
                        return new UniversityReportDetailRowResponse(
                                definition.getReportKey(),
                                "UniversityFacultyProfile",
                                profile.getId(),
                                profileResponse.facultyName(),
                                profileResponse.department(),
                                profileResponse.employmentStatus(),
                                BigDecimal.valueOf(profileResponse.workloadVariance()),
                                "Assigned credits: " + profileResponse.assignedCredits() + ", target: " + profileResponse.workloadTargetCredits());
                    })
                    .toList();
            case "integration_health" -> integrationRunRepository.findTop20ByOrderByExchangedAtDescIdDesc().stream()
                    .filter(run -> normalizedStatus.isBlank() || run.getStatus().equalsIgnoreCase(normalizedStatus))
                    .limit(25)
                    .map(run -> new UniversityReportDetailRowResponse(
                            definition.getReportKey(),
                            "UniversityIntegrationRun",
                            run.getId(),
                            run.getIntegrationName(),
                            run.getIntegrationKey(),
                            run.getStatus(),
                            null,
                            firstNonBlank(run.getResultMessage(), run.getErrorMessage())))
                    .toList();
            default -> List.of();
        };
    }

    @Transactional
    public UniversityDemoSeedResponse seedDemoData() {
        Subject foundation = upsertDemoSubject("ERP101", "University Systems Foundations", 3, true);
        Subject advanced = upsertDemoSubject("ERP220", "Enterprise Architecture Studio", 3, false);
        Subject services = upsertDemoSubject("SVC110", "Student Success Seminar", 2, false);

        UniversityCoursePrerequisite prerequisite = prerequisiteRepository
                .findBySubject_IdAndPrerequisiteSubject_Id(advanced.getId(), foundation.getId())
                .orElseGet(() -> {
                    UniversityCoursePrerequisite created = new UniversityCoursePrerequisite();
                    created.setSubject(advanced);
                    created.setPrerequisiteSubject(foundation);
                    created.setCreatedAt(LocalDateTime.now());
                    return prerequisiteRepository.save(created);
                });

        User student = userRepository.findByEmail("erp.demo.student@edusys.local")
                .map(existing -> ensureStudentRole(existing, demoApplicantShape()))
                .orElseGet(() -> createStudentUser(demoApplicantShape()));

        UniversityAcademicRecord record = academicRecordRepository
                .findByStudent_IdAndSubject_Id(student.getId(), foundation.getId())
                .orElseGet(() -> {
                    UniversityAcademicRecord created = new UniversityAcademicRecord();
                    created.setStudent(student);
                    created.setSubject(foundation);
                    created.setAcademicYear(normalizeAcademicYear(null));
                    created.setSemester(1);
                    created.setFinalGrade(new BigDecimal("88"));
                    created.setStatus(AcademicRecordStatus.COMPLETED);
                    created.setCompletedAt(LocalDateTime.now());
                    created.setCreatedAt(LocalDateTime.now());
                    return academicRecordRepository.save(created);
                });

        UniversityServiceRequest serviceRequest = serviceRequestRepository.findByStudent_IdOrderByRequestedAtDescIdDesc(student.getId()).stream()
                .filter(request -> "Enrollment certificate".equalsIgnoreCase(request.getRequestType()))
                .findFirst()
                .orElseGet(() -> {
                    ServiceRequestResponse response = createServiceRequest(new ServiceRequestCreateRequest(
                            student.getId(),
                            "Enrollment certificate",
                            "Seeded request for the university ERP classroom demo"));
                    return serviceRequestRepository.findById(response.id()).orElseThrow();
                });

        UniversityDemoSeedResponse response = new UniversityDemoSeedResponse(
                student.getId(),
                student.getFullName().trim(),
                List.of(foundation.getId(), advanced.getId(), services.getId()),
                prerequisite.getId(),
                record.getId(),
                serviceRequest.getId(),
                "University ERP demo data is ready");
        recordEvent("Governance", "Demo data seeded", "UniversityDemoSeed", student.getId(), student, response.message());
        return response;
    }

    private UniversityApplicant getApplicant(Long id) {
        return applicantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Applicant not found"));
    }

    private UniversityServiceRequest getServiceRequest(Long id) {
        return serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service request not found"));
    }

    private FeeInvoice resolveBankCallbackInvoice(Long invoiceId) {
        if (invoiceId != null) {
            return invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        }
        return invoiceRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .filter(invoice -> invoice.getStatus() != FeeInvoice.Status.CANCELLED)
                .filter(invoice -> invoice.getStatus() != FeeInvoice.Status.WAIVED)
                .filter(invoice -> invoice.getStatus() != FeeInvoice.Status.PAID)
                .filter(invoice -> totalInvoiceAmount(invoice).subtract(totalPaid(invoice)).compareTo(BigDecimal.ZERO) > 0)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No outstanding invoice is available for bank callback simulation"));
    }

    private void ensureMutableDecision(UniversityApplicant applicant) {
        if (applicant.getStatus() == ApplicantStatus.CONVERTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Converted applicants cannot be changed");
        }
    }

    private User ensureStudentRole(User user, UniversityApplicant applicant) {
        int flags = user.getRoleFlags() == null ? 0 : user.getRoleFlags();
        user.setRoleFlags(flags | User.ROLE_STUDENT);
        user.setFirstName(firstNonBlank(user.getFirstName(), applicant.getFirstName()));
        user.setLastName(firstNonBlank(user.getLastName(), applicant.getLastName()));
        user.setPhone(firstNonBlank(user.getPhone(), applicant.getPhone()));
        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private User createStudentUser(UniversityApplicant applicant) {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(nextUsername(applicant));
        user.setEmail(applicant.getEmail());
        user.setPasswordHash(passwordEncoder.encode("student123"));
        user.setFirstName(applicant.getFirstName());
        user.setLastName(applicant.getLastName());
        user.setPhone(applicant.getPhone());
        user.setRoleFlags(User.ROLE_STUDENT);
        user.setIsActive(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return userRepository.save(user);
    }

    private UniversityApplicant demoApplicantShape() {
        UniversityApplicant applicant = new UniversityApplicant();
        applicant.setFirstName("Demo");
        applicant.setLastName("Student");
        applicant.setEmail("erp.demo.student@edusys.local");
        applicant.setPhone("000-000");
        applicant.setProgram("Enterprise Architecture");
        return applicant;
    }

    private Subject upsertDemoSubject(String code, String name, int credits, boolean mandatory) {
        return subjectRepository.findAll().stream()
                .filter(subject -> code.equalsIgnoreCase(subject.getSubjectCode()))
                .findFirst()
                .orElseGet(() -> {
                    Subject subject = new Subject();
                    subject.setSubjectCode(code);
                    subject.setName(name);
                    subject.setGradeLevel(1);
                    subject.setHoursPerWeek(credits);
                    subject.setIsMandatory(mandatory);
                    subject.setCreatedAt(LocalDateTime.now());
                    return subjectRepository.save(subject);
                });
    }

    private FeeInvoice createCourseSelectionInvoice(User student, String academicYear, int semester, List<UniversityCourseSelection> selections) {
        LocalDateTime now = LocalDateTime.now();
        FeeInvoice invoice = new FeeInvoice();
        invoice.setStudent(student);
        invoice.setInvoiceNumber(nextInvoiceNumber());
        invoice.setDueDate(LocalDate.now().plusDays(14));
        invoice.setStatus(FeeInvoice.Status.ISSUED);
        invoice.setNotes("Generated from university course selection for " + academicYear + " semester " + semester);
        invoice.setCreatedAt(now);
        invoice.setUpdatedAt(now);

        for (UniversityCourseSelection selection : selections) {
            FeeInvoiceLine line = new FeeInvoiceLine();
            line.setInvoice(invoice);
            line.setDescription(courseLabel(selection.getSubject()) + " (" + selection.getCredits() + " credits)");
            line.setAmount(PRICE_PER_CREDIT.multiply(BigDecimal.valueOf(selection.getCredits())));
            invoice.getLines().add(line);
        }

        return invoiceRepository.save(invoice);
    }

    private String nextApplicationNumber() {
        String prefix = "APP-" + LocalDate.now().format(NUMBER_DATE) + "-";
        long next = applicantRepository.countByApplicationNumberStartingWith(prefix) + 1;
        return prefix + String.format("%04d", next);
    }

    private String nextInvoiceNumber() {
        String prefix = "ERP-" + LocalDate.now().format(NUMBER_DATE) + "-";
        long next = invoiceRepository.countByInvoiceNumberStartingWith(prefix) + 1;
        return prefix + String.format("%04d", next);
    }

    private String nextServiceRequestNumber() {
        String prefix = "SR-" + LocalDate.now().format(NUMBER_DATE) + "-";
        long next = serviceRequestRepository.countByRequestNumberStartingWith(prefix) + 1;
        return prefix + String.format("%04d", next);
    }

    private String nextUsername(UniversityApplicant applicant) {
        String base = (applicant.getFirstName() + "." + applicant.getLastName())
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", ".")
                .replaceAll("^\\.|\\.$", "");
        if (base.isBlank()) {
            base = "student";
        }
        long next = userRepository.countByUsernameStartingWith(base);
        String candidate = next == 0 ? base : base + next;
        while (userRepository.findByUsername(candidate).isPresent()) {
            next += 1;
            candidate = base + next;
        }
        return candidate;
    }

    private String normalizeAcademicYear(String academicYear) {
        if (academicYear != null && !academicYear.isBlank()) {
            return academicYear.trim();
        }
        int year = LocalDate.now().getYear();
        return year + "-" + (year + 1);
    }

    private int creditsFor(Subject subject) {
        Integer hours = subject.getHoursPerWeek();
        return hours == null || hours < 1 ? 3 : hours;
    }

    private BigDecimal totalInvoiceAmount(FeeInvoice invoice) {
        return invoice.getLines().stream()
                .map(FeeInvoiceLine::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalPaid(FeeInvoice invoice) {
        if (invoice.getId() == null) {
            return BigDecimal.ZERO;
        }
        return paymentRepository.findByInvoice_IdOrderByPaymentDateDescIdDesc(invoice.getId()).stream()
                .filter(payment -> payment.getStatus() == FeePayment.Status.COMPLETED)
                .map(FeePayment::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal outstandingBalanceFor(User student) {
        return invoiceRepository.findByStudent_IdOrderByCreatedAtDescIdDesc(student.getId()).stream()
                .filter(invoice -> invoice.getStatus() != FeeInvoice.Status.CANCELLED && invoice.getStatus() != FeeInvoice.Status.WAIVED)
                .map(invoice -> totalInvoiceAmount(invoice).subtract(totalPaid(invoice)))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .max(BigDecimal.ZERO);
    }

    private List<UniversityReportBreakdownResponse> admissionsBreakdown() {
        return List.of(ApplicantStatus.values()).stream()
                .map(status -> new UniversityReportBreakdownResponse(status.name(), applicantRepository.countByStatus(status), null))
                .toList();
    }

    private List<UniversityReportBreakdownResponse> serviceRequestBreakdown() {
        return List.of(ServiceRequestStatus.values()).stream()
                .map(status -> new UniversityReportBreakdownResponse(status.name(), serviceRequestRepository.countByStatus(status), null))
                .toList();
    }

    private List<UniversityReportBreakdownResponse> financeBreakdown() {
        return List.of(FeeInvoice.Status.values()).stream()
                .map(status -> {
                    List<FeeInvoice> invoices = invoiceRepository.findByStatusOrderByCreatedAtDescIdDesc(status);
                    BigDecimal amount = invoices.stream()
                            .map(this::totalInvoiceAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new UniversityReportBreakdownResponse(status.name(), invoices.size(), amount);
                })
                .toList();
    }

    private List<UniversityReportBreakdownResponse> serviceQueueBreakdown() {
        return listServiceQueues().stream()
                .map(queue -> new UniversityReportBreakdownResponse(
                        queue.office(),
                        queue.openRequests(),
                        BigDecimal.valueOf(queue.overdueRequests())))
                .toList();
    }

    private List<UniversityReportBreakdownResponse> academicPolicyBreakdown() {
        UniversityAcademicPolicy policy = activeAcademicPolicy();
        return List.of(
                new UniversityReportBreakdownResponse("Max term credits", safePositive(policy.getMaxTermCredits(), 18), null),
                new UniversityReportBreakdownResponse("Probation max credits", safePositive(policy.getProbationMaxTermCredits(), 12), null),
                new UniversityReportBreakdownResponse("Prerequisite rules", prerequisiteRepository.count(), null),
                new UniversityReportBreakdownResponse("Program requirements", programRequirementRepository.count(), null),
                new UniversityReportBreakdownResponse("Academic records", academicRecordRepository.count(), null));
    }

    private List<UniversityReportBreakdownResponse> programRequirementBreakdown() {
        return programRequirementRepository.findAllByOrderByProgramNameAscIdAsc().stream()
                .filter(requirement -> Boolean.TRUE.equals(requirement.getActive()))
                .collect(java.util.stream.Collectors.groupingBy(
                        UniversityProgramRequirement::getProgramName,
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    int credits = entry.getValue().stream()
                            .mapToInt(requirement -> safePositive(requirement.getRequiredCredits(), 0))
                            .sum();
                    return new UniversityReportBreakdownResponse(entry.getKey(), entry.getValue().size(), BigDecimal.valueOf(credits));
                })
                .toList();
    }

    private ApplicantResponse toApplicantResponse(UniversityApplicant applicant) {
        User student = applicant.getConvertedStudent();
        return new ApplicantResponse(
                applicant.getId(),
                applicant.getApplicationNumber(),
                applicant.getFirstName(),
                applicant.getLastName(),
                applicant.getEmail(),
                applicant.getPhone(),
                applicant.getProgram(),
                applicant.getStatus(),
                applicant.getDecisionNotes(),
                student == null ? null : student.getId(),
                student == null ? null : student.getFullName().trim(),
                applicant.getSubmittedAt(),
                applicant.getUpdatedAt());
    }

    private CourseOptionResponse toCourseOptionResponse(Subject subject) {
        return new CourseOptionResponse(
                subject.getId(),
                safeName(subject),
                subject.getSubjectCode(),
                subject.getGradeLevel(),
                creditsFor(subject),
                subject.getIsMandatory(),
                prerequisiteRepository.findBySubject_IdOrderByPrerequisiteSubject_SubjectCodeAsc(subject.getId()).stream()
                        .map(prerequisite -> courseLabel(prerequisite.getPrerequisiteSubject()))
                        .toList());
    }

    private CourseSelectionResponse toCourseSelectionResponse(UniversityCourseSelection selection) {
        FeeInvoice invoice = selection.getInvoice();
        User student = selection.getStudent();
        Subject subject = selection.getSubject();
        return new CourseSelectionResponse(
                selection.getId(),
                student == null ? null : student.getId(),
                student == null ? null : student.getFullName().trim(),
                subject == null ? null : subject.getId(),
                subject == null ? null : safeName(subject),
                subject == null ? null : subject.getSubjectCode(),
                selection.getAcademicYear(),
                selection.getSemester(),
                selection.getCredits(),
                selection.getStatus(),
                invoice == null ? null : invoice.getId(),
                invoice == null ? null : invoice.getInvoiceNumber(),
                selection.getSelectedAt());
    }

    private ServiceRequestResponse toServiceRequestResponse(UniversityServiceRequest serviceRequest) {
        User student = serviceRequest.getStudent();
        User assignedUser = serviceRequest.getAssignedUser();
        return new ServiceRequestResponse(
                serviceRequest.getId(),
                serviceRequest.getRequestNumber(),
                student == null ? null : student.getId(),
                student == null ? null : student.getFullName().trim(),
                serviceRequest.getRequestType(),
                serviceRequest.getDescription(),
                serviceRequest.getStatus(),
                serviceRequest.getAssignedOffice(),
                assignedUser == null ? null : assignedUser.getId(),
                assignedUser == null ? null : assignedUser.getFullName().trim(),
                serviceRequest.getHoldReason(),
                serviceRequest.getDueAt(),
                slaStatusFor(serviceRequest),
                attachmentRequiredFor(serviceRequest),
                attachmentSatisfiedFor(serviceRequest),
                serviceRequest.getRequestedAt(),
                serviceRequest.getUpdatedAt(),
                serviceRequest.getCompletedAt());
    }

    private ServiceRequestDetailResponse toServiceRequestDetailResponse(UniversityServiceRequest serviceRequest) {
        Long requestId = serviceRequest.getId();
        return new ServiceRequestDetailResponse(
                toServiceRequestResponse(serviceRequest),
                serviceRequestCommentRepository.findByRequest_IdOrderByCreatedAtAscIdAsc(requestId).stream()
                        .map(this::toServiceRequestCommentResponse)
                        .toList(),
                serviceRequestHistoryRepository.findByRequest_IdOrderByCreatedAtAscIdAsc(requestId).stream()
                        .map(this::toServiceRequestHistoryResponse)
                        .toList(),
                serviceRequestAttachmentRepository.findByRequest_IdOrderByUploadedAtAscIdAsc(requestId).stream()
                        .map(this::toServiceRequestAttachmentResponse)
                        .toList());
    }

    private ServiceRequestCommentResponse toServiceRequestCommentResponse(UniversityServiceRequestComment comment) {
        User author = comment.getAuthor();
        return new ServiceRequestCommentResponse(
                comment.getId(),
                author == null ? null : author.getId(),
                author == null ? null : author.getFullName().trim(),
                comment.getCommentText(),
                comment.getInternal(),
                comment.getCreatedAt());
    }

    private ServiceRequestHistoryResponse toServiceRequestHistoryResponse(UniversityServiceRequestHistory history) {
        User actor = history.getActor();
        return new ServiceRequestHistoryResponse(
                history.getId(),
                actor == null ? null : actor.getId(),
                actor == null ? null : actor.getFullName().trim(),
                history.getEventType(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getDetails(),
                history.getCreatedAt());
    }

    private ServiceRequestAttachmentResponse toServiceRequestAttachmentResponse(UniversityServiceRequestAttachment attachment) {
        User uploader = attachment.getUploadedBy();
        return new ServiceRequestAttachmentResponse(
                attachment.getId(),
                uploader == null ? null : uploader.getId(),
                uploader == null ? null : uploader.getFullName().trim(),
                attachment.getOriginalFilename(),
                attachment.getStoredPath(),
                "/api/files/download/" + attachment.getStoredPath(),
                attachment.getMimeType(),
                attachment.getSizeBytes(),
                attachment.getUploadedAt());
    }

    private void recordServiceRequestHistory(
            UniversityServiceRequest serviceRequest,
            String eventType,
            ServiceRequestStatus fromStatus,
            ServiceRequestStatus toStatus,
            String details) {
        UniversityServiceRequestHistory history = new UniversityServiceRequestHistory();
        history.setRequest(serviceRequest);
        history.setActor(currentActor());
        history.setEventType(eventType);
        history.setFromStatus(fromStatus == null ? null : fromStatus.name());
        history.setToStatus(toStatus == null ? null : toStatus.name());
        history.setDetails(details);
        history.setCreatedAt(LocalDateTime.now());
        serviceRequestHistoryRepository.save(history);
    }

    private ServiceTypeResponse toServiceTypeResponse(UniversityServiceType serviceType) {
        return new ServiceTypeResponse(
                serviceType.getId(),
                serviceType.getCode(),
                serviceType.getName(),
                serviceType.getDefaultOffice(),
                serviceType.getSlaDays(),
                serviceType.getRequiresFinanceClearance(),
                serviceType.getRequiresAttachment(),
                serviceType.getActive(),
                serviceType.getCreatedAt(),
                serviceType.getUpdatedAt());
    }

    private void applyServiceTypeRequest(UniversityServiceType serviceType, ServiceTypeRequest request, String code) {
        serviceType.setCode(code);
        serviceType.setName(request.name().trim());
        serviceType.setDefaultOffice(request.defaultOffice().trim());
        serviceType.setSlaDays(request.slaDays());
        serviceType.setRequiresFinanceClearance(Boolean.TRUE.equals(request.requiresFinanceClearance()));
        serviceType.setRequiresAttachment(Boolean.TRUE.equals(request.requiresAttachment()));
        serviceType.setActive(request.active() == null || Boolean.TRUE.equals(request.active()));
    }

    private UniversityServiceType resolveServiceType(String requestType) {
        String value = requestType == null ? "" : requestType.trim();
        return serviceTypeRepository.findByCodeIgnoreCase(normalizeServiceTypeCode(value))
                .or(() -> serviceTypeRepository.findByNameIgnoreCase(value))
                .orElseGet(() -> fallbackServiceType(value));
    }

    private User resolveAssignedUser(Long assignedUserId, User currentUser) {
        if (assignedUserId == null) {
            return currentUser;
        }
        return userRepository.findById(assignedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assigned user not found"));
    }

    private boolean attachmentRequiredFor(UniversityServiceRequest serviceRequest) {
        return Boolean.TRUE.equals(resolveServiceType(serviceRequest.getRequestType()).getRequiresAttachment());
    }

    private boolean attachmentSatisfiedFor(UniversityServiceRequest serviceRequest) {
        return serviceRequest.getId() != null
                && serviceRequestAttachmentRepository.existsByRequest_Id(serviceRequest.getId());
    }

    private String graduationClearanceHoldReason(
            DegreeAuditResponse degreeAudit,
            BigDecimal balance,
            boolean attachmentRequired,
            boolean attachmentSatisfied) {
        List<String> reasons = new ArrayList<>();
        if (!Boolean.TRUE.equals(degreeAudit.graduationEligible())) {
            reasons.add("Academic requirements incomplete: " + degreeAudit.remainingCredits() + " credits remaining");
        }
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            reasons.add("Outstanding finance balance: " + balance);
        }
        if (attachmentRequired && !attachmentSatisfied) {
            reasons.add("Required graduation documents are missing");
        }
        return String.join("; ", reasons);
    }

    private String slaStatusFor(UniversityServiceRequest serviceRequest) {
        if (TERMINAL_SERVICE_STATUSES.contains(serviceRequest.getStatus())) {
            return "CLOSED";
        }
        LocalDateTime dueAt = serviceRequest.getDueAt();
        if (dueAt == null) {
            return "UNSCHEDULED";
        }
        LocalDateTime now = LocalDateTime.now();
        if (dueAt.isBefore(now)) {
            return "OVERDUE";
        }
        if (!dueAt.isAfter(now.plusDays(1))) {
            return "DUE_SOON";
        }
        return "ON_TRACK";
    }

    private UniversityServiceType fallbackServiceType(String requestType) {
        UniversityServiceType fallback = new UniversityServiceType();
        fallback.setCode(normalizeServiceTypeCode(requestType));
        fallback.setName(requestType == null || requestType.isBlank() ? "General service request" : requestType.trim());
        fallback.setDefaultOffice(defaultOfficeFor(requestType));
        fallback.setSlaDays(5);
        fallback.setRequiresFinanceClearance(requiresFinanceClearance(requestType));
        fallback.setRequiresAttachment(false);
        fallback.setActive(true);
        return fallback;
    }

    private String normalizeServiceTypeCode(String code) {
        String normalized = code == null ? "" : code.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        return normalized.isBlank() ? "GENERAL_REQUEST" : normalized;
    }

    private String safeOriginalFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return "attachment";
        }
        return Paths.get(originalFilename).getFileName().toString();
    }

    private UniversityErpEventLogResponse toEventLogResponse(UniversityErpEventLog event) {
        User actor = event.getActorUser();
        User student = event.getStudent();
        return new UniversityErpEventLogResponse(
                event.getId(),
                event.getModule(),
                event.getAction(),
                event.getEntityType(),
                event.getEntityId(),
                actor == null ? null : actor.getId(),
                actor == null ? null : actor.getFullName().trim(),
                student == null ? null : student.getId(),
                student == null ? null : student.getFullName().trim(),
                event.getDetails(),
                event.getCreatedAt());
    }

    private void recordEvent(String module, String action, String entityType, Long entityId, User student, String details) {
        UniversityErpEventLog event = new UniversityErpEventLog();
        event.setModule(module);
        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setActorUser(currentActor());
        event.setStudent(student);
        event.setDetails(details);
        event.setCreatedAt(LocalDateTime.now());
        eventLogRepository.save(event);
    }

    private UniversityIntegrationStatusResponse integrationStatus(String key, String name, String direction, String status, String payload) {
        return integrationRunRepository.findTop1ByIntegrationKeyIgnoreCaseOrderByExchangedAtDescIdDesc(key)
                .map(run -> new UniversityIntegrationStatusResponse(
                        key,
                        name,
                        direction,
                        run.getStatus(),
                        run.getExchangedAt() == null ? "Not yet exchanged" : run.getExchangedAt().toString(),
                        run.getPayload()))
                .orElseGet(() -> new UniversityIntegrationStatusResponse(key, name, direction, status, "Not yet exchanged", payload));
    }

    private String buildIntegrationPayload(String key) {
        String normalizedKey = key == null ? "" : key.toLowerCase(Locale.ROOT);
        return switch (normalizedKey) {
            case "lms" -> lmsPayload();
            case "bank" -> bankPayload();
            case "notification" -> notificationPayload();
            case "government" -> governmentPayload();
            default -> "{\"message\":\"Unknown integration payload\"}";
        };
    }

    private String adapterPayload(String key, UniversityIntegrationConnection connection) {
        String exchangePayload = buildIntegrationPayload(key);
        if (connection == null) {
            return exchangePayload;
        }
        return jsonPayload(
                "\"adapterMode\":\"" + jsonEscape(firstNonBlank(connection.getAdapterMode(), "MOCK")) + "\"",
                "\"authType\":\"" + jsonEscape(firstNonBlank(connection.getAuthType(), "NONE")) + "\"",
                "\"secretRef\":\"" + jsonEscape(firstNonBlank(connection.getSecretRef(), "NONE")) + "\"",
                "\"endpointUrl\":\"" + jsonEscape(firstNonBlank(connection.getEndpointUrl(), "mock://local")) + "\"",
                "\"payload\":" + exchangePayload);
    }

    private String normalizeAdapterMode(String adapterMode) {
        String normalized = adapterMode == null || adapterMode.isBlank()
                ? "MOCK"
                : adapterMode.trim().toUpperCase(Locale.ROOT);
        if (!List.of("MOCK", "HTTP").contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adapter mode must be MOCK or HTTP");
        }
        return normalized;
    }

    private String normalizeAuthType(String authType) {
        String normalized = authType == null || authType.isBlank()
                ? "NONE"
                : authType.trim().toUpperCase(Locale.ROOT);
        if (!List.of("NONE", "API_KEY", "BEARER_TOKEN", "BASIC").contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Auth type must be NONE, API_KEY, BEARER_TOKEN, or BASIC");
        }
        return normalized;
    }

    private void validateAdapterConfiguration(UniversityIntegrationConnection connection) {
        if ("HTTP".equalsIgnoreCase(connection.getAdapterMode())
                && (connection.getEndpointUrl() == null || !connection.getEndpointUrl().toLowerCase(Locale.ROOT).startsWith("http"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "HTTP adapter mode requires an http or https endpoint URL");
        }
        if (!"NONE".equalsIgnoreCase(connection.getAuthType()) && blankToNull(connection.getSecretRef()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authenticated adapters require a secret reference");
        }
    }

    private IntegrationSmokeTestResponse smokeTestConnection(UniversityIntegrationConnection connection) {
        try {
            validateAdapterConfiguration(connection);
            boolean secretResolved = httpIntegrationAdapter.canResolveSecret(connection);
            if (!secretResolved) {
                return new IntegrationSmokeTestResponse(
                        connection.getIntegrationKey(),
                        firstNonBlank(connection.getAdapterMode(), "MOCK"),
                        firstNonBlank(connection.getAuthType(), "NONE"),
                        "FAILED",
                        "Secret reference is configured but could not be resolved from environment or properties",
                        false);
            }
            String message = "HTTP".equalsIgnoreCase(connection.getAdapterMode())
                    ? "HTTP endpoint and secret reference are ready for outbound exchange"
                    : "Mock adapter is ready";
            return new IntegrationSmokeTestResponse(
                    connection.getIntegrationKey(),
                    firstNonBlank(connection.getAdapterMode(), "MOCK"),
                    firstNonBlank(connection.getAuthType(), "NONE"),
                    "READY",
                    message,
                    true);
        } catch (ResponseStatusException ex) {
            return new IntegrationSmokeTestResponse(
                    connection.getIntegrationKey(),
                    firstNonBlank(connection.getAdapterMode(), "MOCK"),
                    firstNonBlank(connection.getAuthType(), "NONE"),
                    "FAILED",
                    ex.getReason(),
                    false);
        }
    }

    private String lmsPayload() {
        long selectedCourses = selectionRepository.count();
        long billedSelections = selectionRepository.countByStatus(CourseSelectionStatus.BILLED);
        long subjects = subjectRepository.count();
        long activeFacultyProfiles = facultyProfileRepository.count();
        return jsonPayload(
                "\"courses\":" + subjects,
                "\"courseSelections\":" + selectedCourses,
                "\"billedSelections\":" + billedSelections,
                "\"facultyProfiles\":" + activeFacultyProfiles);
    }

    private String buildLmsRosterExportPayload(List<UniversityCourseSelection> selections) {
        String rows = selections.stream()
                .map(this::lmsRosterRowJson)
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        return "{"
                + "\"exportType\":\"lms_roster\","
                + "\"rowCount\":" + selections.size() + ","
                + "\"generatedAt\":\"" + LocalDateTime.now() + "\","
                + "\"rows\":[" + rows + "]"
                + "}";
    }

    private String lmsRosterRowJson(UniversityCourseSelection selection) {
        User student = selection.getStudent();
        Subject subject = selection.getSubject();
        return "{"
                + "\"selectionId\":" + selection.getId() + ","
                + "\"studentId\":" + (student == null ? null : student.getId()) + ","
                + "\"studentName\":\"" + jsonEscape(student == null ? "" : student.getFullName().trim()) + "\","
                + "\"studentEmail\":\"" + jsonEscape(student == null ? "" : student.getEmail()) + "\","
                + "\"courseId\":" + (subject == null ? null : subject.getId()) + ","
                + "\"courseCode\":\"" + jsonEscape(subject == null ? "" : subject.getSubjectCode()) + "\","
                + "\"courseName\":\"" + jsonEscape(subject == null ? "" : safeName(subject)) + "\","
                + "\"academicYear\":\"" + jsonEscape(selection.getAcademicYear()) + "\","
                + "\"semester\":" + selection.getSemester() + ","
                + "\"credits\":" + selection.getCredits() + ","
                + "\"status\":\"" + selection.getStatus() + "\""
                + "}";
    }

    private String applicantNotificationJson(UniversityApplicant applicant) {
        return "{"
                + "\"source\":\"admissions\","
                + "\"recipient\":\"" + jsonEscape(applicant.getEmail()) + "\","
                + "\"recipientName\":\"" + jsonEscape((applicant.getFirstName() + " " + applicant.getLastName()).trim()) + "\","
                + "\"subject\":\"Application status update\","
                + "\"reference\":\"" + jsonEscape(applicant.getApplicationNumber()) + "\","
                + "\"status\":\"" + applicant.getStatus() + "\","
                + "\"program\":\"" + jsonEscape(applicant.getProgram()) + "\""
                + "}";
    }

    private String serviceRequestNotificationJson(UniversityServiceRequest request) {
        User student = request.getStudent();
        return "{"
                + "\"source\":\"student_services\","
                + "\"recipient\":\"" + jsonEscape(student == null ? "" : student.getEmail()) + "\","
                + "\"recipientName\":\"" + jsonEscape(student == null ? "" : student.getFullName().trim()) + "\","
                + "\"subject\":\"Service request update\","
                + "\"reference\":\"" + jsonEscape(request.getRequestNumber()) + "\","
                + "\"status\":\"" + request.getStatus() + "\","
                + "\"requestType\":\"" + jsonEscape(request.getRequestType()) + "\""
                + "}";
    }

    private String bankPayload() {
        BigDecimal billed = BigDecimal.ZERO;
        BigDecimal outstanding = BigDecimal.ZERO;
        long openInvoices = 0;
        for (FeeInvoice invoice : invoiceRepository.findAll()) {
            BigDecimal total = totalInvoiceAmount(invoice);
            billed = billed.add(total);
            if (invoice.getStatus() != FeeInvoice.Status.CANCELLED && invoice.getStatus() != FeeInvoice.Status.WAIVED) {
                BigDecimal balance = total.subtract(totalPaid(invoice));
                if (balance.compareTo(BigDecimal.ZERO) > 0) {
                    openInvoices++;
                    outstanding = outstanding.add(balance);
                }
            }
        }
        return jsonPayload(
                "\"invoices\":" + invoiceRepository.count(),
                "\"openInvoices\":" + openInvoices,
                "\"payments\":" + paymentRepository.count(),
                "\"billedAmount\":\"" + billed + "\"",
                "\"outstandingAmount\":\"" + outstanding + "\"");
    }

    private String notificationPayload() {
        long pendingApplicants = applicantRepository.countByStatus(ApplicantStatus.SUBMITTED)
                + applicantRepository.countByStatus(ApplicantStatus.SCREENING)
                + applicantRepository.countByStatus(ApplicantStatus.ACCEPTED);
        long openRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.REQUESTED)
                + serviceRequestRepository.countByStatus(ServiceRequestStatus.REVIEW)
                + serviceRequestRepository.countByStatus(ServiceRequestStatus.APPROVED);
        return jsonPayload(
                "\"pendingApplicantNotices\":" + pendingApplicants,
                "\"openServiceRequestNotices\":" + openRequests,
                "\"heldServiceRequests\":" + serviceRequestRepository.countByStatus(ServiceRequestStatus.ON_HOLD));
    }

    private String governmentPayload() {
        long convertedStudents = applicantRepository.countByStatus(ApplicantStatus.CONVERTED);
        long acceptedApplicants = applicantRepository.countByStatus(ApplicantStatus.ACCEPTED) + convertedStudents;
        BigDecimal billed = BigDecimal.ZERO;
        for (FeeInvoice invoice : invoiceRepository.findAll()) {
            billed = billed.add(totalInvoiceAmount(invoice));
        }
        return jsonPayload(
                "\"acceptedApplicants\":" + acceptedApplicants,
                "\"convertedStudents\":" + convertedStudents,
                "\"courseSelections\":" + selectionRepository.count(),
                "\"serviceRequests\":" + serviceRequestRepository.count(),
                "\"billedAmount\":\"" + billed + "\"");
    }

    private String jsonPayload(String... fields) {
        return "{" + String.join(",", fields) + "}";
    }

    private void updateConnectionStatus(String integrationKey, String status) {
        integrationConnectionRepository.findByIntegrationKeyIgnoreCase(integrationKey).ifPresent(connection -> {
            connection.setLastStatus(status);
            connection.setUpdatedAt(LocalDateTime.now());
            integrationConnectionRepository.save(connection);
        });
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String csvRows(String header, List<String> rows) {
        List<String> lines = new ArrayList<>();
        lines.add(header);
        lines.addAll(rows);
        return String.join("\n", lines) + "\n";
    }

    private String csvLine(String report, String filters, String label, long count, BigDecimal amount) {
        return csv(report) + ","
                + csv(filters) + ","
                + csv(label) + ","
                + count + ","
                + csv(amount == null ? "" : amount.toPlainString());
    }

    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private UniversityIntegrationRunResponse toIntegrationRunResponse(UniversityIntegrationRun run) {
        return new UniversityIntegrationRunResponse(
                run.getId(),
                run.getIntegrationKey(),
                run.getIntegrationName(),
                run.getDirection(),
                run.getStatus(),
                run.getPayload(),
                run.getResultMessage(),
                run.getRetryCount(),
                run.getErrorMessage(),
                run.getExchangedAt());
    }

    private IntegrationConnectionResponse toIntegrationConnectionResponse(UniversityIntegrationConnection connection) {
        return new IntegrationConnectionResponse(
                connection.getId(),
                connection.getIntegrationKey(),
                connection.getDisplayName(),
                connection.getEndpointUrl(),
                firstNonBlank(connection.getAdapterMode(), "MOCK"),
                firstNonBlank(connection.getAuthType(), "NONE"),
                connection.getSecretRef(),
                connection.getEnabled(),
                connection.getLastStatus());
    }

    private UniversityReportDefinitionResponse toReportDefinitionResponse(UniversityReportDefinition definition) {
        return new UniversityReportDefinitionResponse(
                definition.getId(),
                definition.getReportKey(),
                definition.getName(),
                definition.getCategory(),
                definition.getDescription(),
                visibleReportRoles(definition.getReportKey()));
    }

    private UniversityReportRunResponse toReportRunResponse(UniversityReportRun run) {
        UniversityReportDefinition definition = run.getReportDefinition();
        return new UniversityReportRunResponse(
                run.getId(),
                definition == null ? null : definition.getReportKey(),
                definition == null ? null : definition.getName(),
                definition == null ? null : definition.getCategory(),
                run.getStatus(),
                run.getFilters(),
                run.getSnapshotPayload(),
                run.getRowCount() == null ? 0 : run.getRowCount(),
                run.getGeneratedAt());
    }

    private boolean canCurrentActorViewReport(String reportKey) {
        User actor = currentActor();
        if (actor == null || actor.isAdmin()) {
            return true;
        }
        List<String> roles = visibleReportRoles(reportKey);
        return (actor.isAdmissionsStaff() && roles.contains("ADMISSIONS_STAFF"))
                || (actor.isFinanceStaff() && roles.contains("FINANCE_STAFF"))
                || (actor.isTeacher() && roles.contains("TEACHER"));
    }

    private List<String> visibleReportRoles(String reportKey) {
        String normalizedKey = reportKey == null ? "" : reportKey.toLowerCase(Locale.ROOT);
        return switch (normalizedKey) {
            case "enrollment_funnel" -> List.of("ADMIN", "ADMISSIONS_STAFF");
            case "finance_balance" -> List.of("ADMIN", "FINANCE_STAFF");
            case "student_services_sla" -> List.of("ADMIN", "ADMISSIONS_STAFF");
            case "faculty_workload" -> List.of("ADMIN", "ADMISSIONS_STAFF", "TEACHER");
            case "integration_health" -> List.of("ADMIN");
            default -> List.of("ADMIN");
        };
    }

    private DepartmentResponse toDepartmentResponse(UniversityDepartment department) {
        return new DepartmentResponse(
                department.getId(),
                department.getCode(),
                department.getName(),
                department.getActive());
    }

    private FacultyWorkloadResponse toFacultyWorkloadResponse(UniversityFacultyWorkload workload) {
        UniversityFacultyProfile profile = workload.getFacultyProfile();
        User faculty = profile == null ? null : profile.getFacultyUser();
        int teaching = safePositive(workload.getTeachingCredits(), 0);
        int advising = safePositive(workload.getAdvisingCredits(), 0);
        int research = safePositive(workload.getResearchCredits(), 0);
        int committee = safePositive(workload.getCommitteeCredits(), 0);
        return new FacultyWorkloadResponse(
                workload.getId(),
                profile == null ? null : profile.getId(),
                faculty == null ? null : faculty.getFullName().trim(),
                workload.getAcademicYear(),
                workload.getSemester(),
                teaching,
                advising,
                research,
                committee,
                teaching + advising + research + committee,
                workload.getNotes());
    }

    private FacultyLeaveResponse toFacultyLeaveResponse(UniversityFacultyLeaveRequest leave) {
        UniversityFacultyProfile profile = leave.getFacultyProfile();
        User faculty = profile == null ? null : profile.getFacultyUser();
        return new FacultyLeaveResponse(
                leave.getId(),
                profile == null ? null : profile.getId(),
                faculty == null ? null : faculty.getFullName().trim(),
                profile == null ? null : profile.getDepartment(),
                leave.getLeaveType(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getStatus(),
                leave.getReason(),
                leave.getDecisionNotes(),
                leave.getRequestedAt(),
                leave.getDecidedAt());
    }

    private String buildReportSnapshot(String reportKey) {
        String normalizedKey = reportKey == null ? "" : reportKey.toLowerCase(Locale.ROOT);
        UniversityReportResponse summary = getReportSummary();
        return switch (normalizedKey) {
            case "enrollment_funnel" -> jsonPayload(
                    "\"applicants\":" + summary.applicants(),
                    "\"acceptedApplicants\":" + summary.acceptedApplicants(),
                    "\"convertedStudents\":" + summary.convertedStudents(),
                    "\"admissionsStatuses\":" + summary.admissionsByStatus().size());
            case "finance_balance" -> jsonPayload(
                    "\"financeInvoices\":" + summary.financeInvoices(),
                    "\"billedAmount\":\"" + summary.billedAmount() + "\"",
                    "\"outstandingBalance\":\"" + summary.outstandingBalance() + "\"",
                    "\"invoiceStatuses\":" + summary.financeByStatus().size());
            case "student_services_sla" -> jsonPayload(
                    "\"serviceRequests\":" + summary.serviceRequests(),
                    "\"openServiceRequests\":" + summary.openServiceRequests(),
                    "\"heldServiceRequests\":" + summary.heldServiceRequests(),
                    "\"queueRows\":" + summary.serviceQueues().size());
            case "faculty_workload" -> jsonPayload(
                    "\"facultyProfiles\":" + facultyProfileRepository.count(),
                    "\"activeTeachingAssignments\":" + teachingAssignmentRepository.count(),
                    "\"programRequirements\":" + summary.programRequirementProgress().size());
            case "integration_health" -> jsonPayload(
                    "\"integrationRuns\":" + integrationRunRepository.count(),
                    "\"latestIntegrations\":" + listIntegrations().size());
            default -> jsonPayload("\"message\":\"Report generated from live ERP summary\"");
        };
    }

    private long reportRowCount(String reportKey) {
        String normalizedKey = reportKey == null ? "" : reportKey.toLowerCase(Locale.ROOT);
        UniversityReportResponse summary = getReportSummary();
        return switch (normalizedKey) {
            case "enrollment_funnel" -> summary.admissionsByStatus().size();
            case "finance_balance" -> summary.financeByStatus().size();
            case "student_services_sla" -> summary.serviceRequestsByStatus().size() + summary.serviceQueues().size();
            case "faculty_workload" -> facultyProfileRepository.count();
            case "integration_health" -> integrationRunRepository.count();
            default -> 1;
        };
    }

    private User currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }

    private CoursePrerequisiteResponse toCoursePrerequisiteResponse(UniversityCoursePrerequisite prerequisite) {
        Subject subject = prerequisite.getSubject();
        Subject prerequisiteSubject = prerequisite.getPrerequisiteSubject();
        return new CoursePrerequisiteResponse(
                prerequisite.getId(),
                subject == null ? null : subject.getId(),
                subject == null ? null : safeName(subject),
                subject == null ? null : subject.getSubjectCode(),
                prerequisiteSubject == null ? null : prerequisiteSubject.getId(),
                prerequisiteSubject == null ? null : safeName(prerequisiteSubject),
                prerequisiteSubject == null ? null : prerequisiteSubject.getSubjectCode(),
                prerequisite.getGroupCode());
    }

    private CourseCorequisiteResponse toCourseCorequisiteResponse(UniversityCourseCorequisite corequisite) {
        Subject subject = corequisite.getSubject();
        Subject corequisiteSubject = corequisite.getCorequisiteSubject();
        return new CourseCorequisiteResponse(
                corequisite.getId(),
                subject == null ? null : subject.getId(),
                subject == null ? null : safeName(subject),
                subject == null ? null : subject.getSubjectCode(),
                corequisiteSubject == null ? null : corequisiteSubject.getId(),
                corequisiteSubject == null ? null : safeName(corequisiteSubject),
                corequisiteSubject == null ? null : corequisiteSubject.getSubjectCode());
    }

    private AcademicRecordResponse toAcademicRecordResponse(UniversityAcademicRecord record) {
        User student = record.getStudent();
        Subject subject = record.getSubject();
        return new AcademicRecordResponse(
                record.getId(),
                student == null ? null : student.getId(),
                student == null ? null : student.getFullName().trim(),
                subject == null ? null : subject.getId(),
                subject == null ? null : safeName(subject),
                subject == null ? null : subject.getSubjectCode(),
                record.getAcademicYear(),
                record.getSemester(),
                record.getFinalGrade(),
                record.getStatus(),
                record.getCompletedAt());
    }

    private AcademicPolicyResponse toAcademicPolicyResponse(UniversityAcademicPolicy policy) {
        return new AcademicPolicyResponse(
                policy.getId(),
                policy.getPolicyName(),
                policy.getMinTermCredits(),
                policy.getMaxTermCredits(),
                policy.getProbationMaxTermCredits(),
                policy.getMinAverageGradeGoodStanding(),
                policy.getBlockRegistrationWhenProbation(),
                policy.getAllowRepeatCompletedCourses(),
                policy.getActive(),
                policy.getUpdatedAt());
    }

    private ProgramRequirementResponse toProgramRequirementResponse(UniversityProgramRequirement requirement) {
        Subject subject = requirement.getSubject();
        return new ProgramRequirementResponse(
                requirement.getId(),
                requirement.getProgramName(),
                requirement.getRequirementName(),
                subject == null ? null : subject.getId(),
                subject == null ? null : safeName(subject),
                subject == null ? null : subject.getSubjectCode(),
                requirement.getRequiredCredits(),
                requirement.getActive());
    }

    private DegreeAuditRequirementResponse toDegreeAuditRequirementResponse(
            UniversityProgramRequirement requirement,
            Set<Long> completedSubjectIds,
            int totalCompletedCredits) {
        Subject subject = requirement.getSubject();
        int requiredCredits = safePositive(requirement.getRequiredCredits(), 0);
        int completedCredits;
        if (subject == null) {
            completedCredits = Math.min(totalCompletedCredits, requiredCredits);
        } else {
            completedCredits = completedSubjectIds.contains(subject.getId())
                    ? Math.min(creditsFor(subject), requiredCredits)
                    : 0;
        }
        return new DegreeAuditRequirementResponse(
                requirement.getId(),
                requirement.getRequirementName(),
                subject == null ? null : subject.getId(),
                subject == null ? null : safeName(subject),
                subject == null ? null : subject.getSubjectCode(),
                requiredCredits,
                completedCredits,
                completedCredits >= requiredCredits);
    }

    private FacultyProfileResponse toFacultyProfileResponse(UniversityFacultyProfile profile) {
        User faculty = profile.getFacultyUser();
        List<TeachingAssignment> assignments = faculty == null
                ? List.of()
                : teachingAssignmentRepository.findAllDetailedByTeacher_IdAndIsActiveTrue(faculty.getId());
        int assignedCredits = assignments.stream()
                .map(TeachingAssignment::getSubject)
                .filter(subject -> subject != null)
                .mapToInt(this::creditsFor)
                .sum();
        int targetCredits = safePositive(profile.getWorkloadTargetCredits(), 0);
        return new FacultyProfileResponse(
                profile.getId(),
                faculty == null ? null : faculty.getId(),
                faculty == null ? null : faculty.getFullName().trim(),
                faculty == null ? null : faculty.getEmail(),
                profile.getEmployeeNumber(),
                profile.getDepartment(),
                profile.getAcademicRank(),
                profile.getEmploymentStatus(),
                profile.getHireDate(),
                profile.getOfficeLocation(),
                profile.getWorkloadTargetCredits(),
                assignments.size(),
                assignedCredits,
                assignedCredits - targetCredits);
    }

    private void ensurePrerequisitesCompleted(User student, Subject subject) {
        List<UniversityCoursePrerequisite> prerequisites = prerequisiteRepository.findBySubject_IdOrderByPrerequisiteSubject_SubjectCodeAsc(subject.getId());
        for (UniversityCoursePrerequisite prerequisite : prerequisites) {
            if (blankToNull(prerequisite.getGroupCode()) != null) {
                continue;
            }
            Subject required = prerequisite.getPrerequisiteSubject();
            if (required != null && !academicRecordRepository.existsByStudent_IdAndSubject_IdAndStatus(student.getId(), required.getId(), AcademicRecordStatus.COMPLETED)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Prerequisite not completed: " + courseLabel(required));
            }
        }
        java.util.Map<String, List<UniversityCoursePrerequisite>> prerequisiteGroups = prerequisites.stream()
                .filter(prerequisite -> blankToNull(prerequisite.getGroupCode()) != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        prerequisite -> blankToNull(prerequisite.getGroupCode()).toUpperCase(Locale.ROOT),
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()));
        for (java.util.Map.Entry<String, List<UniversityCoursePrerequisite>> entry : prerequisiteGroups.entrySet()) {
            boolean groupSatisfied = entry.getValue().stream()
                    .map(UniversityCoursePrerequisite::getPrerequisiteSubject)
                    .filter(required -> required != null)
                    .anyMatch(required -> academicRecordRepository.existsByStudent_IdAndSubject_IdAndStatus(
                            student.getId(), required.getId(), AcademicRecordStatus.COMPLETED));
            if (!groupSatisfied) {
                String options = entry.getValue().stream()
                        .map(UniversityCoursePrerequisite::getPrerequisiteSubject)
                        .filter(required -> required != null)
                        .map(this::courseLabel)
                        .collect(java.util.stream.Collectors.joining(" or "));
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Prerequisite group not completed: " + options);
            }
        }
    }

    private void ensureCorequisitesSatisfied(User student, Subject subject, Set<Long> requestedSubjectIds, String academicYear, int semester) {
        List<UniversityCourseCorequisite> corequisites = corequisiteRepository.findBySubject_IdOrderByCorequisiteSubject_SubjectCodeAsc(subject.getId());
        for (UniversityCourseCorequisite corequisite : corequisites) {
            Subject required = corequisite.getCorequisiteSubject();
            if (required == null) {
                continue;
            }
            boolean satisfiedByCurrentRequest = requestedSubjectIds.contains(required.getId());
            boolean satisfiedByExistingSelection = selectionRepository
                    .findByStudent_IdAndSubject_IdAndAcademicYearAndSemester(student.getId(), required.getId(), academicYear, semester)
                    .map(selection -> selection.getStatus() != CourseSelectionStatus.DROPPED)
                    .orElse(false);
            boolean satisfiedByCompletedRecord = academicRecordRepository.existsByStudent_IdAndSubject_IdAndStatus(
                    student.getId(), required.getId(), AcademicRecordStatus.COMPLETED);
            if (!satisfiedByCurrentRequest && !satisfiedByExistingSelection && !satisfiedByCompletedRecord) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Co-requisite required: " + courseLabel(required));
            }
        }
    }

    private UniversityAcademicPolicy activeAcademicPolicy() {
        return academicPolicyRepository.findFirstByActiveTrueOrderByUpdatedAtDescIdDesc()
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    UniversityAcademicPolicy policy = new UniversityAcademicPolicy();
                    policy.setPolicyName("Default undergraduate selection policy");
                    policy.setMinTermCredits(12);
                    policy.setMaxTermCredits(18);
                    policy.setProbationMaxTermCredits(12);
                    policy.setMinAverageGradeGoodStanding(new BigDecimal("60.00"));
                    policy.setBlockRegistrationWhenProbation(false);
                    policy.setAllowRepeatCompletedCourses(false);
                    policy.setActive(true);
                    policy.setCreatedAt(now);
                    policy.setUpdatedAt(now);
                    return academicPolicyRepository.save(policy);
                });
    }

    private void ensureRepeatAllowed(User student, Subject subject, UniversityAcademicPolicy policy) {
        if (Boolean.TRUE.equals(policy.getAllowRepeatCompletedCourses())) {
            return;
        }
        if (academicRecordRepository.existsByStudent_IdAndSubject_IdAndStatus(student.getId(), subject.getId(), AcademicRecordStatus.COMPLETED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Repeat not allowed by active academic policy: " + courseLabel(subject));
        }
    }

    private int allowedTermCredits(User student, UniversityAcademicPolicy policy) {
        if (!isOnAcademicProbation(student, policy)) {
            return safePositive(policy.getMaxTermCredits(), 18);
        }
        if (Boolean.TRUE.equals(policy.getBlockRegistrationWhenProbation())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Academic standing hold: student is below the good-standing grade threshold");
        }
        return safePositive(policy.getProbationMaxTermCredits(), safePositive(policy.getMaxTermCredits(), 18));
    }

    private boolean isOnAcademicProbation(User student, UniversityAcademicPolicy policy) {
        List<BigDecimal> grades = academicRecordRepository.findByStudent_IdOrderByCompletedAtDescIdDesc(student.getId()).stream()
                .filter(record -> record.getStatus() == AcademicRecordStatus.COMPLETED)
                .map(UniversityAcademicRecord::getFinalGrade)
                .filter(grade -> grade != null)
                .toList();
        if (grades.isEmpty()) {
            return false;
        }
        BigDecimal average = grades.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);
        return average.compareTo(policy.getMinAverageGradeGoodStanding()) < 0;
    }

    private int safePositive(Integer value, int fallback) {
        return value == null || value < 1 ? fallback : value;
    }

    private String courseLabel(Subject subject) {
        String code = subject.getSubjectCode();
        return (code == null || code.isBlank()) ? safeName(subject) : code + " - " + safeName(subject);
    }

    private String safeName(Subject subject) {
        return subject.getName() == null ? "Untitled course" : subject.getName();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String firstNonBlank(String current, String fallback) {
        return current == null || current.isBlank() ? fallback : current;
    }

    private LocalDateTime safeDateTime(LocalDateTime value) {
        return value == null ? LocalDateTime.MIN : value;
    }

    private boolean requiresFinanceClearance(String requestType) {
        String normalized = requestType == null ? "" : requestType.toLowerCase(Locale.ROOT);
        return normalized.contains("transcript")
                || normalized.contains("certificate")
                || normalized.contains("graduation")
                || normalized.contains("official");
    }

    private String defaultOfficeFor(String requestType) {
        String normalized = requestType == null ? "" : requestType.toLowerCase(Locale.ROOT);
        if (normalized.contains("finance") || normalized.contains("payment")) {
            return "Finance office";
        }
        if (normalized.contains("transcript") || normalized.contains("certificate") || normalized.contains("graduation")) {
            return "Registrar";
        }
        return "Student affairs";
    }
}
