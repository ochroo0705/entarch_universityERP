package com.edusys.backend.ai.service;

import com.edusys.backend.ai.model.RiskLevel;
import com.edusys.backend.ai.model.RiskScoringConfig;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RuleBasedRiskScoringEngine implements RiskScoringEngine {

    @Override
    public RiskComputationResult compute(RiskScoringConfig config, List<RiskIndicatorResult> indicatorResults) {
        List<WeightedIndicator> weighted = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (RiskIndicatorResult indicator : indicatorResults) {
            BigDecimal weight = weightFor(config, indicator);
            BigDecimal contribution = indicator.normalizedRiskValue()
                    .multiply(weight)
                    .setScale(2, RoundingMode.HALF_UP);
            weighted.add(new WeightedIndicator(indicator, weight, contribution));
            total = total.add(contribution);
        }

        int score = total.setScale(0, RoundingMode.HALF_UP).intValue();
        if (score < 0) score = 0;
        if (score > 100) score = 100;

        RiskLevel riskLevel = score <= config.getLowMaxScore()
                ? RiskLevel.LOW
                : score <= config.getMediumMaxScore() ? RiskLevel.MEDIUM : RiskLevel.HIGH;

        String reasonSummary = weighted.stream()
                .sorted(Comparator.comparing(WeightedIndicator::weightedContribution).reversed())
                .limit(2)
                .map(item -> item.indicator().indicatorCode().name() + ": " + item.weightedContribution())
                .reduce((left, right) -> left + "; " + right)
                .orElse("No indicators available");

        String action = switch (riskLevel) {
            case HIGH -> "Prioritize staff follow-up, review attendance and homework barriers, and consider parent outreach.";
            case MEDIUM -> "Monitor this student closely and review the strongest contributing indicators with the teaching team.";
            case LOW -> "Continue monitoring regular attendance, homework completion, and grade trends.";
        };

        return new RiskComputationResult(score, riskLevel, weighted, reasonSummary, action);
    }

    private BigDecimal weightFor(RiskScoringConfig config, RiskIndicatorResult indicator) {
        return switch (indicator.indicatorCode()) {
            case ATTENDANCE -> config.getAttendanceWeight();
            case LATENESS -> config.getLatenessWeight();
            case HOMEWORK -> config.getHomeworkWeight();
            case GRADE -> config.getGradeWeight();
        };
    }
}
