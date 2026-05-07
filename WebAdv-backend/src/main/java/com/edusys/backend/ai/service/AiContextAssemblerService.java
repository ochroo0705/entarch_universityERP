package com.edusys.backend.ai.service;

import com.edusys.backend.ai.model.RiskLevel;
import com.edusys.backend.model.Attendance;
import com.edusys.backend.model.HomeworkSubmission;
import com.edusys.backend.repository.AttendanceRepository;
import com.edusys.backend.repository.GradeRepository;
import com.edusys.backend.repository.HomeworkSubmissionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiContextAssemblerService {

    public record RiskComputation(
            BigDecimal attendanceRate,
            int missingHomeworkCount,
            BigDecimal gradeAverage,
            int riskScore,
            RiskLevel riskLevel,
            String reasonSummary,
            String recommendedAction,
            String sourceSummaryJson
    ) {
    }

    private final AttendanceRepository attendanceRepository;
    private final HomeworkSubmissionRepository homeworkSubmissionRepository;
    private final GradeRepository gradeRepository;
    private final ObjectMapper objectMapper;

    public AiContextAssemblerService(
            AttendanceRepository attendanceRepository,
            HomeworkSubmissionRepository homeworkSubmissionRepository,
            GradeRepository gradeRepository,
            ObjectMapper objectMapper
    ) {
        this.attendanceRepository = attendanceRepository;
        this.homeworkSubmissionRepository = homeworkSubmissionRepository;
        this.gradeRepository = gradeRepository;
        this.objectMapper = objectMapper;
    }

    public RiskComputation buildRiskComputation(Long studentId) {
        long attendanceTotal = attendanceRepository.countByStudent_Id(studentId);
        long attendedCount = attendanceRepository.countByStudent_IdAndStatusIn(
                studentId,
                List.of(Attendance.Status.present, Attendance.Status.late, Attendance.Status.excused, Attendance.Status.sick)
        );
        int missingHomeworkCount = Math.toIntExact(
                homeworkSubmissionRepository.countByStudent_IdAndStatusIn(studentId, List.of(HomeworkSubmission.Status.missing))
        );

        Double averageGradeValue = gradeRepository.findAverageGradeValueByStudentId(studentId);
        BigDecimal attendanceRate = attendanceTotal == 0
                ? BigDecimal.valueOf(100)
                : BigDecimal.valueOf(attendedCount * 100.0 / attendanceTotal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gradeAverage = averageGradeValue == null
                ? null
                : BigDecimal.valueOf(averageGradeValue).setScale(2, RoundingMode.HALF_UP);

        int riskScore = 0;
        if (attendanceRate.doubleValue() < 80) riskScore += 45;
        else if (attendanceRate.doubleValue() < 90) riskScore += 20;

        if (missingHomeworkCount >= 5) riskScore += 30;
        else if (missingHomeworkCount >= 2) riskScore += 15;

        if (gradeAverage != null) {
            if (gradeAverage.doubleValue() < 60) riskScore += 35;
            else if (gradeAverage.doubleValue() < 75) riskScore += 15;
        }

        riskScore = Math.min(riskScore, 100);
        RiskLevel riskLevel = riskScore >= 70 ? RiskLevel.HIGH : riskScore >= 40 ? RiskLevel.MEDIUM : RiskLevel.LOW;

        String reasonSummary = buildReasonSummary(attendanceRate, missingHomeworkCount, gradeAverage, riskLevel);
        String recommendedAction = buildRecommendedAction(riskLevel);
        String sourceSummaryJson = toJson(Map.of(
                "attendanceRate", attendanceRate,
                "attendanceRecordCount", attendanceTotal,
                "missingHomeworkCount", missingHomeworkCount,
                "gradeAverage", gradeAverage,
                "calculationType", "phase1-placeholder"
        ));

        return new RiskComputation(
                attendanceRate,
                missingHomeworkCount,
                gradeAverage,
                riskScore,
                riskLevel,
                reasonSummary,
                recommendedAction,
                sourceSummaryJson
        );
    }

    public String toJson(Map<String, ?> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to serialize AI payload");
        }
    }

    private String buildReasonSummary(BigDecimal attendanceRate, int missingHomeworkCount, BigDecimal gradeAverage, RiskLevel riskLevel) {
        Map<String, Object> signals = new LinkedHashMap<>();
        signals.put("attendanceRate", attendanceRate + "%");
        signals.put("missingHomeworkCount", missingHomeworkCount);
        signals.put("gradeAverage", gradeAverage != null ? gradeAverage : "N/A");
        signals.put("summary", switch (riskLevel) {
            case HIGH -> "Multiple academic signals suggest this student may need immediate staff follow-up.";
            case MEDIUM -> "Some academic indicators need review before they become larger problems.";
            case LOW -> "Current indicators look stable, but the snapshot remains a placeholder baseline.";
        });
        return signals.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .reduce((left, right) -> left + "; " + right)
                .orElse("No risk signals available");
    }

    private String buildRecommendedAction(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case HIGH -> "Review attendance and homework history with the homeroom or subject teacher, then prepare a parent outreach draft.";
            case MEDIUM -> "Monitor the next grading period and prepare a gentle parent communication draft if the trend continues.";
            case LOW -> "Keep observing regular attendance, homework completion, and grade trends.";
        };
    }
}
