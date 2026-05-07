package com.edusys.backend.ai.service;

import com.edusys.backend.ai.model.RiskLevel;
import com.edusys.backend.ai.model.RiskScoringConfig;

import java.math.BigDecimal;
import java.util.List;

public interface RiskScoringEngine {

    RiskComputationResult compute(RiskScoringConfig config, List<RiskIndicatorResult> indicatorResults);

    record WeightedIndicator(
            RiskIndicatorResult indicator,
            BigDecimal weight,
            BigDecimal weightedContribution
    ) {
    }

    record RiskComputationResult(
            int riskScore,
            RiskLevel riskLevel,
            List<WeightedIndicator> weightedIndicators,
            String reasonSummary,
            String recommendedAction
    ) {
    }
}
