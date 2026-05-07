package com.edusys.backend.ai.service;

import com.edusys.backend.ai.model.RiskIndicatorCode;
import com.edusys.backend.repository.GradeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class GradeRiskCollector implements RiskIndicatorCollector {

    private final GradeRepository gradeRepository;
    private final AiContextAssemblerService aiContextAssemblerService;

    public GradeRiskCollector(GradeRepository gradeRepository, AiContextAssemblerService aiContextAssemblerService) {
        this.gradeRepository = gradeRepository;
        this.aiContextAssemblerService = aiContextAssemblerService;
    }

    @Override
    public RiskIndicatorResult collect(RiskCalculationContext context) {
        GradeWindowMetrics metrics = gradeRepository.summarizeStudentWindow(
                context.studentId(),
                context.gradeWindowStart(),
                context.gradeWindowEnd()
        );
        Double average = metrics.averageGrade();
        long count = metrics.gradeCount();
        if (average == null || count == 0) {
            return new RiskIndicatorResult(
                    RiskIndicatorCode.GRADE,
                    null,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    0,
                    true,
                    aiContextAssemblerService.toJson(Map.of("averageGrade", "N/A", "gradeCount", 0))
            );
        }
        BigDecimal averageGrade = BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
        BigDecimal normalized = BigDecimal.valueOf(Math.max(0, 100 - averageGrade.doubleValue())).setScale(2, RoundingMode.HALF_UP);
        return new RiskIndicatorResult(
                RiskIndicatorCode.GRADE,
                averageGrade,
                normalized,
                Math.toIntExact(count),
                false,
                aiContextAssemblerService.toJson(Map.of("averageGrade", averageGrade, "gradeCount", count))
        );
    }
}
