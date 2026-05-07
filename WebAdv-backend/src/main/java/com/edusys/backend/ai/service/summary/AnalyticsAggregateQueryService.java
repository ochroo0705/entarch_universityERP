package com.edusys.backend.ai.service.summary;

import com.edusys.backend.ai.model.RiskLevel;
import com.edusys.backend.ai.model.StudentRiskIndicatorSnapshot;
import com.edusys.backend.ai.model.StudentRiskSnapshot;
import com.edusys.backend.ai.model.SummaryScopeType;
import com.edusys.backend.ai.model.SummaryType;
import com.edusys.backend.ai.repository.StudentRiskSnapshotRepository;
import com.edusys.backend.ai.validation.AiAccessService;
import com.edusys.backend.model.Class;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.ClassRepository;
import com.edusys.backend.repository.StudentEnrollmentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AnalyticsAggregateQueryService {

    private final StudentRiskSnapshotRepository studentRiskSnapshotRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final ClassRepository classRepository;
    private final AiAccessService aiAccessService;

    public AnalyticsAggregateQueryService(
            StudentRiskSnapshotRepository studentRiskSnapshotRepository,
            StudentEnrollmentRepository studentEnrollmentRepository,
            ClassRepository classRepository,
            AiAccessService aiAccessService
    ) {
        this.studentRiskSnapshotRepository = studentRiskSnapshotRepository;
        this.studentEnrollmentRepository = studentEnrollmentRepository;
        this.classRepository = classRepository;
        this.aiAccessService = aiAccessService;
    }

    public SummaryScopeData buildScopeData(
            User actor,
            SummaryType summaryType,
            Long classId,
            Integer gradeLevel,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        SummaryType effectiveType = resolveType(actor, summaryType, classId, gradeLevel);
        SummaryScopeType scopeType = toScopeType(effectiveType);
        List<StudentRiskSnapshot> latestSnapshots = latestSnapshotsForActor(actor);
        List<StudentRiskSnapshot> scopedSnapshots = latestSnapshots.stream()
                .filter(snapshot -> matchesScope(snapshot, effectiveType, actor, classId, gradeLevel))
                .toList();

        int eligibleStudents = countEligibleStudents(actor, effectiveType, classId, gradeLevel);
        if (eligibleStudents < 5) {
            throw new AccessDeniedException("Summary generation requires at least 5 students in scope");
        }

        Map<String, Long> indicatorFrequency = scopedSnapshots.stream()
                .flatMap(snapshot -> topIndicators(snapshot).stream())
                .collect(Collectors.groupingBy(value -> value, LinkedHashMap::new, Collectors.counting()));

        List<StudentRiskSnapshot> previousSnapshots = previousSnapshots(scopedSnapshots);
        long currentHighRiskCount = scopedSnapshots.stream().filter(snapshot -> snapshot.getRiskLevel() == RiskLevel.HIGH).count();
        long previousHighRiskCount = previousSnapshots.stream().filter(snapshot -> snapshot.getRiskLevel() == RiskLevel.HIGH).count();

        String scopeKey = scopeKey(effectiveType, actor, classId, gradeLevel);
        String scopeLabel = scopeLabel(effectiveType, actor, classId, gradeLevel);
        LocalDate comparisonStart = periodStart.minusDays(Math.max(1, periodEnd.toEpochDay() - periodStart.toEpochDay() + 1));
        LocalDate comparisonEnd = periodStart.minusDays(1);

        Map<String, Object> aggregateMetrics = new LinkedHashMap<>();
        aggregateMetrics.put("totalStudents", eligibleStudents);
        aggregateMetrics.put("studentsWithSnapshots", scopedSnapshots.size());
        aggregateMetrics.put("lowRiskCount", countByRisk(scopedSnapshots, RiskLevel.LOW));
        aggregateMetrics.put("mediumRiskCount", countByRisk(scopedSnapshots, RiskLevel.MEDIUM));
        aggregateMetrics.put("highRiskCount", currentHighRiskCount);
        aggregateMetrics.put("highRiskDelta", currentHighRiskCount - previousHighRiskCount);
        aggregateMetrics.put("staleSnapshotCount", scopedSnapshots.stream().filter(this::isStale).count());
        aggregateMetrics.put("snapshotCoveragePercent", percent(scopedSnapshots.size(), eligibleStudents));
        aggregateMetrics.put("averageAttendanceRate", average(scopedSnapshots, StudentRiskSnapshot::getAttendanceRate));
        aggregateMetrics.put("averageGradeAverage", average(scopedSnapshots, StudentRiskSnapshot::getGradeAverage));
        aggregateMetrics.put("averageMissingHomeworkCount", averageInteger(scopedSnapshots, StudentRiskSnapshot::getMissingHomeworkCount));
        aggregateMetrics.put("latestCalculatedAt", scopedSnapshots.stream()
                .map(StudentRiskSnapshot::getCalculatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));

        Map<String, Object> patternSummary = new LinkedHashMap<>();
        patternSummary.put("topIndicators", indicatorFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> Map.of("indicator", entry.getKey(), "count", entry.getValue()))
                .toList());
        if (effectiveType == SummaryType.ADMIN_SCHOOL_OVERVIEW) {
            patternSummary.put("topClassesByHighRisk", topClassesByHighRisk(scopedSnapshots));
        }

        Map<String, Object> dataQuality = new LinkedHashMap<>();
        dataQuality.put("eligibleStudents", eligibleStudents);
        dataQuality.put("snapshotCount", scopedSnapshots.size());
        dataQuality.put("staleSnapshotCount", scopedSnapshots.stream().filter(this::isStale).count());
        dataQuality.put("coveragePercent", percent(scopedSnapshots.size(), eligibleStudents));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scope_type", scopeType.name());
        payload.put("scope_reference", Map.of("key", scopeKey, "label", scopeLabel));
        payload.put("period_start", periodStart);
        payload.put("period_end", periodEnd);
        payload.put("comparison_period_start", comparisonStart);
        payload.put("comparison_period_end", comparisonEnd);
        payload.put("aggregate_metrics", aggregateMetrics);
        payload.put("pattern_summary", patternSummary);
        payload.put("data_quality", dataQuality);
        payload.put("constraints", Map.of(
                "student_level_details_allowed", false,
                "must_be_grounded_in_metrics", true,
                "output_format_version", "phase4-v1"
        ));

        return new SummaryScopeData(
                effectiveType,
                scopeType,
                scopeKey,
                scopeLabel,
                periodStart,
                periodEnd,
                comparisonStart,
                comparisonEnd,
                payload
        );
    }

    private List<StudentRiskSnapshot> latestSnapshotsForActor(User actor) {
        return actor.isAdmin()
                ? studentRiskSnapshotRepository.findLatestSnapshots()
                : studentRiskSnapshotRepository.findLatestSnapshotsByStudentIds(aiAccessService.getAccessibleStudentIds(actor));
    }

    private boolean matchesScope(StudentRiskSnapshot snapshot, SummaryType type, User actor, Long classId, Integer gradeLevel) {
        return switch (type) {
            case TEACHER_CLASS_OVERVIEW -> Objects.equals(snapshot.getClassEntity() != null ? snapshot.getClassEntity().getId() : null, classId);
            case TEACHER_RISK_OVERVIEW -> actor.isTeacher() && !actor.isAdmin();
            case ADMIN_GRADE_OVERVIEW -> Objects.equals(snapshot.getGradeLevel(), gradeLevel);
            case ADMIN_SCHOOL_OVERVIEW -> true;
        };
    }

    private int countEligibleStudents(User actor, SummaryType type, Long classId, Integer gradeLevel) {
        return switch (type) {
            case TEACHER_CLASS_OVERVIEW -> studentEnrollmentRepository.findActiveStudentIdsByClassId(classId).size();
            case TEACHER_RISK_OVERVIEW -> aiAccessService.getAccessibleStudentIds(actor).size();
            case ADMIN_GRADE_OVERVIEW -> (int) studentRiskSnapshotRepository.findLatestSnapshots().stream()
                    .filter(snapshot -> Objects.equals(snapshot.getGradeLevel(), gradeLevel))
                    .map(snapshot -> snapshot.getStudent().getId())
                    .distinct()
                    .count();
            case ADMIN_SCHOOL_OVERVIEW -> studentRiskSnapshotRepository.findLatestSnapshots().size();
        };
    }

    private SummaryType resolveType(User actor, SummaryType requested, Long classId, Integer gradeLevel) {
        if (requested != null) {
            return requested;
        }
        if (actor.isAdmin()) {
            return gradeLevel != null ? SummaryType.ADMIN_GRADE_OVERVIEW : SummaryType.ADMIN_SCHOOL_OVERVIEW;
        }
        return classId != null ? SummaryType.TEACHER_CLASS_OVERVIEW : SummaryType.TEACHER_RISK_OVERVIEW;
    }

    private SummaryScopeType toScopeType(SummaryType summaryType) {
        return switch (summaryType) {
            case TEACHER_CLASS_OVERVIEW -> SummaryScopeType.TEACHER_CLASS;
            case TEACHER_RISK_OVERVIEW -> SummaryScopeType.TEACHER_OVERVIEW;
            case ADMIN_GRADE_OVERVIEW -> SummaryScopeType.ADMIN_GRADE;
            case ADMIN_SCHOOL_OVERVIEW -> SummaryScopeType.ADMIN_SCHOOL;
        };
    }

    private String scopeKey(SummaryType summaryType, User actor, Long classId, Integer gradeLevel) {
        return switch (summaryType) {
            case TEACHER_CLASS_OVERVIEW -> "CLASS:" + classId;
            case TEACHER_RISK_OVERVIEW -> "TEACHER:" + actor.getId();
            case ADMIN_GRADE_OVERVIEW -> "GRADE:" + gradeLevel;
            case ADMIN_SCHOOL_OVERVIEW -> "SCHOOL";
        };
    }

    private String scopeLabel(SummaryType summaryType, User actor, Long classId, Integer gradeLevel) {
        return switch (summaryType) {
            case TEACHER_CLASS_OVERVIEW -> classRepository.findById(classId).map(Class::getClassName).orElse("Teacher class");
            case TEACHER_RISK_OVERVIEW -> "Teacher overview for " + actor.getFullName().trim();
            case ADMIN_GRADE_OVERVIEW -> "Grade " + gradeLevel;
            case ADMIN_SCHOOL_OVERVIEW -> "School-wide overview";
        };
    }

    private long countByRisk(List<StudentRiskSnapshot> snapshots, RiskLevel level) {
        return snapshots.stream().filter(snapshot -> snapshot.getRiskLevel() == level).count();
    }

    private double average(List<StudentRiskSnapshot> snapshots, java.util.function.Function<StudentRiskSnapshot, BigDecimal> extractor) {
        List<BigDecimal> values = snapshots.stream().map(extractor).filter(Objects::nonNull).toList();
        if (values.isEmpty()) {
            return 0.0;
        }
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP).doubleValue();
    }

    private double averageInteger(List<StudentRiskSnapshot> snapshots, java.util.function.Function<StudentRiskSnapshot, Integer> extractor) {
        List<Integer> values = snapshots.stream().map(extractor).filter(Objects::nonNull).toList();
        if (values.isEmpty()) {
            return 0.0;
        }
        return BigDecimal.valueOf(values.stream().mapToInt(Integer::intValue).average().orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double percent(long part, long whole) {
        if (whole <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(part * 100.0 / whole).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private boolean isStale(StudentRiskSnapshot snapshot) {
        return snapshot.getCalculatedAt() == null || snapshot.getCalculatedAt().isBefore(LocalDateTime.now().minusHours(24));
    }

    private List<String> topIndicators(StudentRiskSnapshot snapshot) {
        return snapshot.getIndicatorSnapshots() == null ? List.of() : snapshot.getIndicatorSnapshots().stream()
                .sorted(Comparator.comparing(StudentRiskIndicatorSnapshot::getWeightedContribution).reversed())
                .limit(2)
                .map(indicator -> indicator.getIndicatorCode().name())
                .toList();
    }

    private List<StudentRiskSnapshot> previousSnapshots(List<StudentRiskSnapshot> latestSnapshots) {
        List<Long> studentIds = latestSnapshots.stream().map(snapshot -> snapshot.getStudent().getId()).distinct().toList();
        if (studentIds.isEmpty()) {
            return List.of();
        }
        Map<Long, List<StudentRiskSnapshot>> byStudent = studentRiskSnapshotRepository.findByStudent_IdInOrderByCalculatedAtDesc(studentIds).stream()
                .collect(Collectors.groupingBy(snapshot -> snapshot.getStudent().getId(), LinkedHashMap::new, Collectors.toList()));
        List<StudentRiskSnapshot> previous = new ArrayList<>();
        byStudent.values().forEach(items -> {
            if (items.size() > 1) {
                previous.add(items.get(1));
            }
        });
        return previous;
    }

    private List<Map<String, Object>> topClassesByHighRisk(List<StudentRiskSnapshot> snapshots) {
        return snapshots.stream()
                .filter(snapshot -> snapshot.getClassEntity() != null)
                .collect(Collectors.groupingBy(snapshot -> snapshot.getClassEntity().getId()))
                .values().stream()
                .map(group -> {
                    StudentRiskSnapshot sample = group.getFirst();
                    long highRiskCount = group.stream().filter(item -> item.getRiskLevel() == RiskLevel.HIGH).count();
                    return Map.<String, Object>of(
                            "classId", sample.getClassEntity().getId(),
                            "className", sample.getClassEntity().getClassName(),
                            "highRiskCount", highRiskCount,
                            "studentCount", group.size()
                    );
                })
                .sorted((left, right) -> Long.compare((Long) right.get("highRiskCount"), (Long) left.get("highRiskCount")))
                .limit(5)
                .toList();
    }

    public record SummaryScopeData(
            SummaryType summaryType,
            SummaryScopeType scopeType,
            String scopeKey,
            String scopeLabel,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate comparisonPeriodStart,
            LocalDate comparisonPeriodEnd,
            Map<String, Object> payload
    ) {
    }
}
