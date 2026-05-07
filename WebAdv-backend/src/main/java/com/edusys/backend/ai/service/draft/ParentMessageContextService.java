package com.edusys.backend.ai.service.draft;

import com.edusys.backend.ai.model.IssueType;
import com.edusys.backend.ai.model.ParentMessageDraft;
import com.edusys.backend.ai.model.StudentRiskIndicatorSnapshot;
import com.edusys.backend.ai.model.StudentRiskSnapshot;
import com.edusys.backend.ai.repository.StudentRiskSnapshotRepository;
import com.edusys.backend.ai.service.AiContextAssemblerService;
import com.edusys.backend.model.StudentEnrollment;
import com.edusys.backend.repository.StudentEnrollmentRepository;
import com.edusys.backend.service.GradeService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParentMessageContextService {

    private static final String PROMPT_VERSION = "phase3-parent-message-v1";

    private final StudentRiskSnapshotRepository studentRiskSnapshotRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final AiContextAssemblerService aiContextAssemblerService;
    private final GradeService gradeService;

    public ParentMessageContextService(
            StudentRiskSnapshotRepository studentRiskSnapshotRepository,
            StudentEnrollmentRepository studentEnrollmentRepository,
            AiContextAssemblerService aiContextAssemblerService,
            GradeService gradeService
    ) {
        this.studentRiskSnapshotRepository = studentRiskSnapshotRepository;
        this.studentEnrollmentRepository = studentEnrollmentRepository;
        this.aiContextAssemblerService = aiContextAssemblerService;
        this.gradeService = gradeService;
    }

    public ParentMessageGenerationInput buildInput(ParentMessageDraft draft) {
        StudentRiskSnapshot riskSnapshot = draft.getRiskSnapshot() != null
                ? draft.getRiskSnapshot()
                : studentRiskSnapshotRepository.findFirstByStudent_IdOrderByCalculatedAtDesc(draft.getStudent().getId()).orElse(null);

        AiContextAssemblerService.RiskComputation fallbackRisk = aiContextAssemblerService.buildRiskComputation(draft.getStudent().getId());
        String className = resolveClassName(draft, riskSnapshot);
        String attendanceSummary = buildAttendanceSummary(riskSnapshot, fallbackRisk);
        String gradeTrendSummary = buildGradeTrendSummary(draft.getStudent().getId(), riskSnapshot, fallbackRisk);
        List<String> topIndicators = riskSnapshot == null
                ? List.of()
                : riskSnapshot.getIndicatorSnapshots().stream()
                .sorted(Comparator.comparing(StudentRiskIndicatorSnapshot::getWeightedContribution).reversed())
                .limit(3)
                .map(indicator -> indicator.getIndicatorCode().name())
                .toList();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("student_first_name", draft.getStudent().getFirstName());
        payload.put("class_name", className);
        payload.put("issue_type", draft.getIssueType().name());
        payload.put("attendance_summary", attendanceSummary);
        payload.put("missing_assignment_count", riskSnapshot != null ? riskSnapshot.getMissingHomeworkCount() : fallbackRisk.missingHomeworkCount());
        payload.put("grade_trend_summary", gradeTrendSummary);
        payload.put("risk_level", riskSnapshot != null && riskSnapshot.getRiskLevel() != null ? riskSnapshot.getRiskLevel().name() : fallbackRisk.riskLevel().name());
        payload.put("top_indicators", topIndicators);
        payload.put("teacher_note", redactTeacherNote(draft.getTeacherNote()));
        payload.put("desired_tone", normalizeTone(draft.getToneLabel()));
        payload.put("language_code", normalizeLanguage(draft.getLanguageCode()));
        payload.put("channel", draft.getChannel().name());
        payload.put("goal", draft.getGoalLabel());

        return new ParentMessageGenerationInput(
                draft.getId(),
                safeName(draft.getStudent().getFirstName()),
                className,
                draft.getIssueType() == null ? IssueType.GENERAL_FOLLOW_UP : draft.getIssueType(),
                riskSnapshot != null && riskSnapshot.getAttendanceRate() != null ? riskSnapshot.getAttendanceRate() : fallbackRisk.attendanceRate(),
                attendanceSummary,
                riskSnapshot != null ? riskSnapshot.getMissingHomeworkCount() : fallbackRisk.missingHomeworkCount(),
                gradeTrendSummary,
                riskSnapshot != null && riskSnapshot.getRiskLevel() != null ? riskSnapshot.getRiskLevel().name() : fallbackRisk.riskLevel().name(),
                topIndicators,
                redactTeacherNote(draft.getTeacherNote()),
                normalizeTone(draft.getToneLabel()),
                normalizeLanguage(draft.getLanguageCode()),
                draft.getChannel(),
                draft.getGoalLabel() == null || draft.getGoalLabel().isBlank() ? "Invite partnership on next steps" : draft.getGoalLabel(),
                aiContextAssemblerService.toJson(payload),
                PROMPT_VERSION
        );
    }

    private String resolveClassName(ParentMessageDraft draft, StudentRiskSnapshot riskSnapshot) {
        if (riskSnapshot != null && riskSnapshot.getClassEntity() != null) {
            return riskSnapshot.getClassEntity().getClassName();
        }
        return studentEnrollmentRepository.findLatestActiveEnrollmentByStudentId(draft.getStudent().getId())
                .map(StudentEnrollment::getClassEntity)
                .map(com.edusys.backend.model.Class::getClassName)
                .orElse("Current class");
    }

    private String buildAttendanceSummary(StudentRiskSnapshot riskSnapshot, AiContextAssemblerService.RiskComputation fallbackRisk) {
        BigDecimal rate = riskSnapshot != null && riskSnapshot.getAttendanceRate() != null
                ? riskSnapshot.getAttendanceRate()
                : fallbackRisk.attendanceRate();
        if (rate == null) {
            return "Attendance summary unavailable";
        }
        return "Attendance rate is " + rate.setScale(2, RoundingMode.HALF_UP) + "%.";
    }

    private String buildGradeTrendSummary(Long studentId, StudentRiskSnapshot riskSnapshot, AiContextAssemblerService.RiskComputation fallbackRisk) {
        try {
            var trend = gradeService.getGradeTrends(studentId);
            return "Grade trend is " + trend.trend().toLowerCase() + " with " + trend.quarterComparison().size() + " recorded quarter summaries.";
        } catch (Exception ignored) {
            BigDecimal average = riskSnapshot != null && riskSnapshot.getGradeAverage() != null
                    ? riskSnapshot.getGradeAverage()
                    : fallbackRisk.gradeAverage();
            return average == null
                    ? "Grade trend summary unavailable."
                    : "Current average grade is " + average.setScale(2, RoundingMode.HALF_UP) + ".";
        }
    }

    private String redactTeacherNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }
        String normalized = note.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        return normalized.length() > 500 ? normalized.substring(0, 500) : normalized;
    }

    private String normalizeTone(String tone) {
        if (tone == null || tone.isBlank()) {
            return "supportive";
        }
        String normalized = tone.toLowerCase().replaceAll("[^a-z\\- ]", "").trim();
        return normalized.isBlank() ? "supportive" : normalized;
    }

    private String normalizeLanguage(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return "mn";
        }
        String normalized = languageCode.toLowerCase().replaceAll("[^a-z\\-]", "").trim();
        return normalized.isBlank() ? "mn" : normalized;
    }

    private String safeName(String firstName) {
        return firstName == null || firstName.isBlank() ? "the student" : firstName.trim();
    }
}
