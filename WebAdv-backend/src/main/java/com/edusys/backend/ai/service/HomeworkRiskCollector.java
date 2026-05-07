package com.edusys.backend.ai.service;

import com.edusys.backend.ai.model.RiskIndicatorCode;
import com.edusys.backend.model.HomeworkSubmission;
import com.edusys.backend.repository.HomeworkSubmissionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class HomeworkRiskCollector implements RiskIndicatorCollector {

    private final HomeworkSubmissionRepository homeworkSubmissionRepository;
    private final AiContextAssemblerService aiContextAssemblerService;

    public HomeworkRiskCollector(HomeworkSubmissionRepository homeworkSubmissionRepository, AiContextAssemblerService aiContextAssemblerService) {
        this.homeworkSubmissionRepository = homeworkSubmissionRepository;
        this.aiContextAssemblerService = aiContextAssemblerService;
    }

    @Override
    public RiskIndicatorResult collect(RiskCalculationContext context) {
        HomeworkWindowMetrics metrics = homeworkSubmissionRepository.summarizeHomeworkWindow(
                context.studentId(),
                context.homeworkWindowStart(),
                context.homeworkWindowEnd(),
                HomeworkSubmission.Status.missing
        );
        long assigned = metrics.assignedHomeworkCount();
        if (assigned == 0) {
            return new RiskIndicatorResult(
                    RiskIndicatorCode.HOMEWORK,
                    null,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    0,
                    true,
                    aiContextAssemblerService.toJson(Map.of("assignedHomeworkCount", 0, "missingHomeworkCount", 0))
            );
        }
        long missing = metrics.missingHomeworkCount();
        BigDecimal missingRate = BigDecimal.valueOf(missing * 100.0 / assigned).setScale(2, RoundingMode.HALF_UP);
        return new RiskIndicatorResult(
                RiskIndicatorCode.HOMEWORK,
                missingRate,
                missingRate,
                Math.toIntExact(assigned),
                false,
                aiContextAssemblerService.toJson(Map.of("assignedHomeworkCount", assigned, "missingHomeworkCount", missing, "missingRate", missingRate))
        );
    }
}
