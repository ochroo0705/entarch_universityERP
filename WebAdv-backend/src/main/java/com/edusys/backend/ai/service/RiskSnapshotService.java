package com.edusys.backend.ai.service;

import com.edusys.backend.ai.audit.AiAuditContext;
import com.edusys.backend.ai.dto.*;
import com.edusys.backend.ai.mapper.AiMapper;
import com.edusys.backend.ai.model.*;
import com.edusys.backend.ai.repository.StudentRiskIndicatorSnapshotRepository;
import com.edusys.backend.ai.repository.StudentRiskSnapshotRepository;
import com.edusys.backend.ai.validation.AiAccessService;
import com.edusys.backend.dto.UserClassSummaryDTO;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.StudentEnrollment;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.StudentEnrollmentRepository;
import com.edusys.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RiskSnapshotService {

    private static final String MODEL_VERSION = "rule-engine-v1";

    private final StudentRiskSnapshotRepository studentRiskSnapshotRepository;
    private final StudentRiskIndicatorSnapshotRepository indicatorSnapshotRepository;
    private final UserRepository userRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final AiAccessService aiAccessService;
    private final AttendanceRiskCollector attendanceRiskCollector;
    private final LatenessRiskCollector latenessRiskCollector;
    private final HomeworkRiskCollector homeworkRiskCollector;
    private final GradeRiskCollector gradeRiskCollector;
    private final RiskScoringEngine riskScoringEngine;
    private final RiskConfigurationService riskConfigurationService;
    private final AiContextAssemblerService aiContextAssemblerService;
    private final AiAuditService aiAuditService;
    private final RiskRecalculationRunGuardService riskRecalculationRunGuardService;

    public RiskSnapshotService(
            StudentRiskSnapshotRepository studentRiskSnapshotRepository,
            StudentRiskIndicatorSnapshotRepository indicatorSnapshotRepository,
            UserRepository userRepository,
            StudentEnrollmentRepository studentEnrollmentRepository,
            AiAccessService aiAccessService,
            AttendanceRiskCollector attendanceRiskCollector,
            LatenessRiskCollector latenessRiskCollector,
            HomeworkRiskCollector homeworkRiskCollector,
            GradeRiskCollector gradeRiskCollector,
            RiskScoringEngine riskScoringEngine,
            RiskConfigurationService riskConfigurationService,
            AiContextAssemblerService aiContextAssemblerService,
            AiAuditService aiAuditService,
            RiskRecalculationRunGuardService riskRecalculationRunGuardService
    ) {
        this.studentRiskSnapshotRepository = studentRiskSnapshotRepository;
        this.indicatorSnapshotRepository = indicatorSnapshotRepository;
        this.userRepository = userRepository;
        this.studentEnrollmentRepository = studentEnrollmentRepository;
        this.aiAccessService = aiAccessService;
        this.attendanceRiskCollector = attendanceRiskCollector;
        this.latenessRiskCollector = latenessRiskCollector;
        this.homeworkRiskCollector = homeworkRiskCollector;
        this.gradeRiskCollector = gradeRiskCollector;
        this.riskScoringEngine = riskScoringEngine;
        this.riskConfigurationService = riskConfigurationService;
        this.aiContextAssemblerService = aiContextAssemblerService;
        this.aiAuditService = aiAuditService;
        this.riskRecalculationRunGuardService = riskRecalculationRunGuardService;
    }

    @Transactional
    public RiskSnapshotResponse createSnapshot(RiskSnapshotCreateRequest request, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        aiAccessService.ensureCanAccessStudent(actor, request.studentId());
        StudentRiskSnapshot snapshot = generateSnapshotForStudent(
                request.studentId(),
                actor,
                actor.isAdmin() ? CalculationTrigger.MANUAL_ADMIN : CalculationTrigger.MANUAL_TEACHER,
                auditContext,
                AiAuditEventType.RISK_SNAPSHOT_CREATED
        );
        return AiMapper.toRiskSnapshotResponse(snapshot);
    }

    public List<RiskDashboardItemResponse> listSnapshots(Long studentId, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        List<StudentRiskSnapshot> snapshots;
        if (studentId != null) {
            aiAccessService.ensureCanAccessStudent(actor, studentId);
            snapshots = studentRiskSnapshotRepository.findByStudent_IdOrderByCalculatedAtDesc(studentId);
        } else if (actor.isAdmin()) {
            snapshots = studentRiskSnapshotRepository.findAll().stream()
                    .sorted(Comparator.comparing(StudentRiskSnapshot::getCalculatedAt).reversed())
                    .toList();
        } else {
            List<Long> accessibleStudentIds = aiAccessService.getAccessibleStudentIds(actor);
            snapshots = accessibleStudentIds.isEmpty()
                    ? List.of()
                    : studentRiskSnapshotRepository.findByStudent_IdInOrderByCalculatedAtDesc(accessibleStudentIds);
        }

        aiAuditService.record(
                AiAuditEventType.RISK_SNAPSHOT_LIST_VIEWED,
                AiEntityType.STUDENT_RISK_SNAPSHOT,
                null,
                actor,
                studentId != null ? requireStudent(studentId) : null,
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                aiContextAssemblerService.toJson(mapOf("studentId", studentId)),
                null,
                null,
                auditContext
        );

        return snapshots.stream().map(AiMapper::toRiskDashboardItemResponse).toList();
    }

    public RiskSnapshotResponse getSnapshot(Long id, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        StudentRiskSnapshot snapshot = studentRiskSnapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Risk snapshot not found"));
        aiAccessService.ensureCanAccessStudent(actor, snapshot.getStudent().getId());

        aiAuditService.record(
                AiAuditEventType.RISK_SNAPSHOT_VIEWED,
                AiEntityType.STUDENT_RISK_SNAPSHOT,
                snapshot.getId(),
                actor,
                snapshot.getStudent(),
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                aiContextAssemblerService.toJson(mapOf("snapshotId", snapshot.getId())),
                null,
                null,
                auditContext
        );
        return AiMapper.toRiskSnapshotResponse(snapshot);
    }

    public List<RiskDashboardListItemResponse> getDashboard(RiskDashboardQueryRequest query, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        List<StudentRiskSnapshot> latest = latestSnapshotsForActor(actor);
        List<StudentRiskSnapshot> filtered = applyDashboardFilters(latest, query);
        List<RiskDashboardListItemResponse> response = filtered.stream()
                .map(this::toDashboardListItem)
                .toList();
        aiAuditService.record(
                AiAuditEventType.RISK_DASHBOARD_VIEWED,
                AiEntityType.STUDENT_RISK_SNAPSHOT,
                null,
                actor,
                null,
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                aiContextAssemblerService.toJson(mapOf(
                        "classId", query.classId(),
                        "gradeLevel", query.gradeLevel(),
                        "riskLevel", query.riskLevel() != null ? query.riskLevel().name() : null
                )),
                null,
                null,
                auditContext
        );
        return response;
    }

    public RiskDashboardBundleResponse getAdminDashboardBundle(RiskDashboardQueryRequest query, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        if (!actor.isAdmin()) {
            throw new AccessDeniedException("Only admins can view the admin dashboard bundle");
        }

        List<StudentRiskSnapshot> filtered = applyDashboardFilters(latestSnapshotsForActor(actor), query);
        List<RiskDashboardListItemResponse> items = filtered.stream()
                .map(this::toDashboardListItem)
                .toList();
        RiskSummaryResponse summary = buildSummaryResponse(filtered);

        aiAuditService.record(
                AiAuditEventType.RISK_DASHBOARD_VIEWED,
                AiEntityType.STUDENT_RISK_SNAPSHOT,
                null,
                actor,
                null,
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                aiContextAssemblerService.toJson(mapOf(
                        "classId", query.classId(),
                        "gradeLevel", query.gradeLevel(),
                        "riskLevel", query.riskLevel() != null ? query.riskLevel().name() : null,
                        "bundle", true
                )),
                null,
                null,
                auditContext
        );

        aiAuditService.record(
                AiAuditEventType.RISK_SUMMARY_VIEWED,
                AiEntityType.STUDENT_RISK_SNAPSHOT,
                null,
                actor,
                null,
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                aiContextAssemblerService.toJson(mapOf("totalStudents", filtered.size(), "bundle", true)),
                null,
                null,
                auditContext
        );

        return new RiskDashboardBundleResponse(items, summary);
    }

    public RiskDetailResponse getDashboardDetail(Long studentId, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        aiAccessService.ensureCanAccessStudent(actor, studentId);

        StudentRiskSnapshot latest = studentRiskSnapshotRepository.findFirstByStudent_IdOrderByCalculatedAtDesc(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Risk detail not found"));
        List<RiskDashboardItemResponse> history = studentRiskSnapshotRepository.findTop10ByStudent_IdOrderByCalculatedAtDesc(studentId).stream()
                .map(AiMapper::toRiskDashboardItemResponse)
                .toList();

        aiAuditService.record(
                AiAuditEventType.RISK_SNAPSHOT_VIEWED,
                AiEntityType.STUDENT_RISK_SNAPSHOT,
                latest.getId(),
                actor,
                latest.getStudent(),
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                aiContextAssemblerService.toJson(mapOf("studentId", studentId, "detail", true)),
                null,
                null,
                auditContext
        );

        return new RiskDetailResponse(
                latest.getId(),
                latest.getStudent().getId(),
                latest.getStudent().getFullName().trim(),
                latest.getClassEntity() != null ? latest.getClassEntity().getId() : null,
                latest.getClassEntity() != null ? latest.getClassEntity().getClassName() : null,
                latest.getGradeLevel(),
                latest.getRiskLevel(),
                latest.getRiskScore(),
                latest.getSnapshotStatus(),
                latest.getReasonSummary(),
                latest.getRecommendedAction(),
                latest.getModelVersionLabel(),
                latest.getScoringConfigVersion(),
                latest.getIsPlaceholder(),
                isStale(latest),
                latest.getCalculatedAt(),
                latest.getCalculationWindowStart(),
                latest.getCalculationWindowEnd(),
                AiMapper.toIndicatorResponses(latest.getIndicatorSnapshots()),
                history
        );
    }

    public RiskSummaryResponse getSummary(RiskDashboardQueryRequest query, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        if (!actor.isAdmin()) {
            throw new AccessDeniedException("Only admins can view risk summary");
        }
        List<StudentRiskSnapshot> latest = applyDashboardFilters(latestSnapshotsForActor(actor), query);

        aiAuditService.record(
                AiAuditEventType.RISK_SUMMARY_VIEWED,
                AiEntityType.STUDENT_RISK_SNAPSHOT,
                null,
                actor,
                null,
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                aiContextAssemblerService.toJson(mapOf("totalStudents", latest.size())),
                null,
                null,
                auditContext
        );

        return buildSummaryResponse(latest);
    }

    @Transactional
    public RiskSnapshotResponse recalculateStudent(Long studentId, AiAuditContext auditContext) {
        User actor = aiAccessService.requireCurrentUser();
        aiAccessService.ensureCanAccessStudent(actor, studentId);
        return AiMapper.toRiskSnapshotResponse(generateSnapshotForStudent(
                studentId,
                actor,
                actor.isAdmin() ? CalculationTrigger.MANUAL_ADMIN : CalculationTrigger.MANUAL_TEACHER,
                auditContext,
                AiAuditEventType.RISK_SNAPSHOT_RECALCULATED
        ));
    }

    @Transactional
    public List<RiskSnapshotResponse> recalculateScope(RiskRecalculationRequest request, AiAuditContext auditContext) {
        User actor = requireAdminForScopeRecalculation();
        List<Long> studentIds = resolveScopeStudentIds(request);
        return studentIds.stream()
                .map(studentId -> AiMapper.toRiskSnapshotResponse(generateSnapshotForStudent(
                        studentId,
                        actor,
                        CalculationTrigger.MANUAL_ADMIN,
                        auditContext,
                        AiAuditEventType.RISK_SNAPSHOT_RECALCULATED
                )))
                .toList();
    }

    public User requireAdminForScopeRecalculation() {
        User actor = aiAccessService.requireCurrentUser();
        if (!actor.isAdmin()) {
            throw new AccessDeniedException("Only admins can recalculate a broader scope");
        }
        return actor;
    }

    public List<Long> resolveScopeStudentIds(RiskRecalculationRequest request) {
        if (request.studentId() != null) {
            return List.of(request.studentId());
        }
        if (request.classId() != null) {
            return studentEnrollmentRepository.findActiveStudentIdsByClassId(request.classId());
        }
        return userRepository.findAllStudentsOrderByName().stream()
                .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                .map(User::getId)
                .toList();
    }

    public RiskAccessScopeResponse getAccessScope() {
        User actor = aiAccessService.requireCurrentUser();
        List<Long> studentIds = aiAccessService.getAccessibleStudentIds(actor);
        List<AiStudentOptionResponse> students = studentIds.isEmpty()
                ? List.of()
                : userRepository.findByIdInOrderByLastNameAscFirstNameAscIdAsc(studentIds).stream()
                .map(student -> new AiStudentOptionResponse(student.getId(), student.getFullName().trim(), List.of()))
                .toList();

        List<UserClassSummaryDTO> classSummaries = studentIds.isEmpty()
                ? List.of()
                : studentEnrollmentRepository.findActiveClassSummariesByStudentIds(studentIds);
        Map<Long, RiskAccessScopeResponse.AccessibleClassResponse> classes = new LinkedHashMap<>();
        classSummaries.forEach(summary -> classes.putIfAbsent(
                summary.classId(),
                new RiskAccessScopeResponse.AccessibleClassResponse(summary.classId(), summary.className(), summary.grade())
        ));
        return new RiskAccessScopeResponse(new ArrayList<>(classes.values()), students);
    }

    @Transactional
    public int runScheduledRecalculation() {
        User actor = userRepository.findFirstAdminUser()
                .orElseThrow(() -> new ResourceNotFoundException("Admin user required for scheduled recalculation"));
        List<Long> studentIds = resolveScopeStudentIds(new RiskRecalculationRequest(null, null));
        RiskRecalculationRunGuardService.ScopeRunStartResult runStart =
                riskRecalculationRunGuardService.tryStart("SCHOOL", studentIds.size(), actor.getId(), "SCHEDULED");
        if (!runStart.started()) {
            return 0;
        }

        try {
        studentIds.forEach(studentId -> generateSnapshotForStudent(
                studentId,
                actor,
                CalculationTrigger.SCHEDULED,
                new AiAuditContext("scheduled-risk-refresh", "127.0.0.1", "scheduler"),
                AiAuditEventType.RISK_SNAPSHOT_RECALCULATED
        ));
        return studentIds.size();
        } finally {
            riskRecalculationRunGuardService.finish();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateSnapshotInNewTransaction(
            Long studentId,
            User actor,
            CalculationTrigger trigger,
            AiAuditEventType eventType
    ) {
        generateSnapshotForStudent(
                studentId,
                actor,
                trigger,
                new AiAuditContext("background-risk-refresh", "127.0.0.1", "background-risk-refresh"),
                eventType
        );
    }

    private List<StudentRiskSnapshot> latestSnapshotsForActor(User actor) {
        return actor.isAdmin()
                ? studentRiskSnapshotRepository.findLatestSnapshots()
                : fetchAccessibleSnapshots(actor);
    }

    private List<StudentRiskSnapshot> fetchAccessibleSnapshots(User actor) {
        List<Long> accessibleStudentIds = aiAccessService.getAccessibleStudentIds(actor);
        if (accessibleStudentIds.isEmpty()) {
            return List.of();
        }
        return studentRiskSnapshotRepository.findLatestSnapshotsByStudentIds(accessibleStudentIds);
    }

    private List<StudentRiskSnapshot> applyDashboardFilters(List<StudentRiskSnapshot> snapshots, RiskDashboardQueryRequest query) {
        return snapshots.stream()
                .filter(snapshot -> query.classId() == null || Objects.equals(snapshot.getClassEntity() != null ? snapshot.getClassEntity().getId() : null, query.classId()))
                .filter(snapshot -> query.gradeLevel() == null || Objects.equals(snapshot.getGradeLevel(), query.gradeLevel()))
                .filter(snapshot -> query.riskLevel() == null || snapshot.getRiskLevel() == query.riskLevel())
                .filter(snapshot -> query.fromCalculatedAt() == null || !snapshot.getCalculatedAt().isBefore(query.fromCalculatedAt()))
                .filter(snapshot -> query.toCalculatedAt() == null || !snapshot.getCalculatedAt().isAfter(query.toCalculatedAt()))
                .filter(snapshot -> query.search() == null || query.search().isBlank()
                        || snapshot.getStudent().getFullName().toLowerCase().contains(query.search().trim().toLowerCase()))
                .toList();
    }

    private RiskSummaryResponse buildSummaryResponse(List<StudentRiskSnapshot> snapshots) {
        long low = snapshots.stream().filter(snapshot -> snapshot.getRiskLevel() == RiskLevel.LOW).count();
        long medium = snapshots.stream().filter(snapshot -> snapshot.getRiskLevel() == RiskLevel.MEDIUM).count();
        long high = snapshots.stream().filter(snapshot -> snapshot.getRiskLevel() == RiskLevel.HIGH).count();

        List<RiskSummaryResponse.RiskSummaryBucketResponse> breakdown = snapshots.stream()
                .collect(Collectors.groupingBy(snapshot -> snapshot.getClassEntity() != null ? snapshot.getClassEntity().getId() : -1L))
                .values().stream()
                .map(group -> {
                    StudentRiskSnapshot sample = group.getFirst();
                    long highRisk = group.stream().filter(item -> item.getRiskLevel() == RiskLevel.HIGH).count();
                    return new RiskSummaryResponse.RiskSummaryBucketResponse(
                            sample.getClassEntity() != null ? sample.getClassEntity().getId() : null,
                            sample.getClassEntity() != null ? sample.getClassEntity().getClassName() : "Unassigned",
                            sample.getGradeLevel(),
                            group.size(),
                            highRisk
                    );
                })
                .sorted(Comparator.comparing(RiskSummaryResponse.RiskSummaryBucketResponse::highRiskCount).reversed())
                .toList();

        LocalDateTime latestCalculatedAt = snapshots.stream()
                .map(StudentRiskSnapshot::getCalculatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new RiskSummaryResponse(snapshots.size(), low, medium, high, latestCalculatedAt, breakdown);
    }

    private RiskDashboardListItemResponse toDashboardListItem(StudentRiskSnapshot snapshot) {
        List<String> topIndicators = snapshot.getIndicatorSnapshots().stream()
                .sorted(Comparator.comparing(StudentRiskIndicatorSnapshot::getWeightedContribution).reversed())
                .limit(2)
                .map(indicator -> indicator.getIndicatorCode().name())
                .toList();
        return new RiskDashboardListItemResponse(
                snapshot.getId(),
                snapshot.getStudent().getId(),
                snapshot.getStudent().getFullName().trim(),
                snapshot.getClassEntity() != null ? snapshot.getClassEntity().getId() : null,
                snapshot.getClassEntity() != null ? snapshot.getClassEntity().getClassName() : null,
                snapshot.getGradeLevel(),
                snapshot.getRiskLevel(),
                snapshot.getRiskScore(),
                snapshot.getSnapshotStatus(),
                snapshot.getReasonSummary(),
                snapshot.getIsPlaceholder(),
                isStale(snapshot),
                snapshot.getCalculatedAt(),
                topIndicators
        );
    }

    private boolean isStale(StudentRiskSnapshot snapshot) {
        return snapshot.getCalculatedAt() == null || snapshot.getCalculatedAt().isBefore(LocalDateTime.now().minusHours(24));
    }

    private StudentRiskSnapshot generateSnapshotForStudent(
            Long studentId,
            User actor,
            CalculationTrigger trigger,
            AiAuditContext auditContext,
            AiAuditEventType eventType
    ) {
        User student = requireStudent(studentId);
        RiskScoringConfig config = riskConfigurationService.getActiveConfig();
        RiskCalculationContext context = buildContext(studentId, config);
        List<RiskIndicatorResult> indicators = List.of(
                attendanceRiskCollector.collect(context),
                latenessRiskCollector.collect(context),
                homeworkRiskCollector.collect(context),
                gradeRiskCollector.collect(context)
        );

        RiskScoringEngine.RiskComputationResult computation = riskScoringEngine.compute(config, indicators);
        StudentEnrollment enrollment = studentEnrollmentRepository.findLatestActiveEnrollmentByStudentId(studentId).orElse(null);

        BigDecimal attendanceRate = rawIndicatorValue(indicators, RiskIndicatorCode.ATTENDANCE);
        BigDecimal gradeAverage = rawIndicatorValue(indicators, RiskIndicatorCode.GRADE);
        int missingHomeworkCount = computeApproxMissingHomeworkCount(indicators);
        long availableIndicators = indicators.stream().filter(indicator -> !indicator.missingData()).count();
        SnapshotStatus status = availableIndicators < 2 ? SnapshotStatus.INSUFFICIENT_DATA : SnapshotStatus.GENERATED;

        StudentRiskSnapshot snapshot = new StudentRiskSnapshot();
        snapshot.setStudent(student);
        snapshot.setCreatedByUser(actor);
        snapshot.setClassEntity(enrollment != null ? enrollment.getClassEntity() : null);
        snapshot.setGradeLevel(enrollment != null && enrollment.getClassEntity() != null ? enrollment.getClassEntity().getGrade() : null);
        snapshot.setSnapshotStatus(status);
        snapshot.setRiskLevel(computation.riskLevel());
        snapshot.setRiskScore(computation.riskScore());
        snapshot.setAttendanceRate(attendanceRate);
        snapshot.setMissingHomeworkCount(missingHomeworkCount);
        snapshot.setGradeAverage(gradeAverage);
        snapshot.setSourceSummaryJson(aiContextAssemblerService.toJson(mapOf(
                "indicators", indicators.stream().collect(Collectors.toMap(
                        indicator -> indicator.indicatorCode().name(),
                        indicator -> mapOf(
                                "rawValue", indicator.rawValue(),
                                "normalizedRiskValue", indicator.normalizedRiskValue(),
                                "dataPointsCount", indicator.dataPointsCount(),
                                "missingData", indicator.missingData()
                        )
                )),
                "availableIndicatorCount", availableIndicators
        )));
        snapshot.setReasonSummary(computation.reasonSummary());
        snapshot.setRecommendedAction(computation.recommendedAction());
        snapshot.setCalculatedAt(LocalDateTime.now());
        snapshot.setCalculationWindowStart(context.gradeWindowStart());
        snapshot.setCalculationWindowEnd(context.gradeWindowEnd());
        snapshot.setModelVersionLabel(MODEL_VERSION);
        snapshot.setScoringConfigVersion(config.getConfigVersion());
        snapshot.setCalculationTrigger(trigger);
        snapshot.setIsPlaceholder(false);

        StudentRiskSnapshot saved = studentRiskSnapshotRepository.save(snapshot);
        List<StudentRiskIndicatorSnapshot> persistedIndicators = computation.weightedIndicators().stream()
                .map(weighted -> toIndicatorSnapshot(saved, weighted))
                .toList();
        indicatorSnapshotRepository.saveAll(persistedIndicators);
        saved.setIndicatorSnapshots(persistedIndicators);

        aiAuditService.record(
                eventType,
                AiEntityType.STUDENT_RISK_SNAPSHOT,
                saved.getId(),
                actor,
                student,
                null,
                AiAuditActionStatus.SUCCESS,
                null,
                aiContextAssemblerService.toJson(mapOf(
                        "trigger", trigger.name(),
                        "riskScore", saved.getRiskScore(),
                        "riskLevel", saved.getRiskLevel().name(),
                        "configVersion", saved.getScoringConfigVersion()
                )),
                null,
                saved.getSourceSummaryJson(),
                auditContext
        );
        return saved;
    }

    private StudentRiskIndicatorSnapshot toIndicatorSnapshot(StudentRiskSnapshot snapshot, RiskScoringEngine.WeightedIndicator weighted) {
        StudentRiskIndicatorSnapshot indicatorSnapshot = new StudentRiskIndicatorSnapshot();
        indicatorSnapshot.setRiskSnapshot(snapshot);
        indicatorSnapshot.setIndicatorCode(weighted.indicator().indicatorCode());
        indicatorSnapshot.setRawValue(weighted.indicator().rawValue());
        indicatorSnapshot.setNormalizedRiskValue(weighted.indicator().normalizedRiskValue().setScale(2, RoundingMode.HALF_UP));
        indicatorSnapshot.setWeight(weighted.weight());
        indicatorSnapshot.setWeightedContribution(weighted.weightedContribution());
        indicatorSnapshot.setDataPointsCount(weighted.indicator().dataPointsCount());
        indicatorSnapshot.setIsMissingData(weighted.indicator().missingData());
        indicatorSnapshot.setDetailsJson(weighted.indicator().detailsJson());
        return indicatorSnapshot;
    }

    private int computeApproxMissingHomeworkCount(List<RiskIndicatorResult> indicators) {
        return indicators.stream()
                .filter(indicator -> indicator.indicatorCode() == RiskIndicatorCode.HOMEWORK)
                .findFirst()
                .map(indicator -> {
                    if (indicator.rawValue() == null || indicator.dataPointsCount() == 0) {
                        return 0;
                    }
                    return BigDecimal.valueOf(indicator.dataPointsCount())
                            .multiply(indicator.rawValue())
                            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                            .intValue();
                })
                .orElse(0);
    }

    private BigDecimal rawIndicatorValue(List<RiskIndicatorResult> indicators, RiskIndicatorCode indicatorCode) {
        return indicators.stream()
                .filter(indicator -> indicator.indicatorCode() == indicatorCode)
                .findFirst()
                .map(RiskIndicatorResult::rawValue)
                .orElse(null);
    }

    private RiskCalculationContext buildContext(Long studentId, RiskScoringConfig config) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        return new RiskCalculationContext(
                studentId,
                today.minusDays(config.getAttendanceWindowDays()),
                today,
                today.minusDays(config.getHomeworkWindowDays()),
                today,
                now.minusDays(config.getGradeWindowDays()),
                now
        );
    }

    private User requireStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!student.isStudent()) {
            throw new IllegalArgumentException("Provided user is not a student");
        }
        return student;
    }

    private Map<String, Object> mapOf(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            map.put(String.valueOf(entries[index]), entries[index + 1]);
        }
        return map;
    }
}
