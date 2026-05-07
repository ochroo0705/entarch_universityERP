package com.edusys.backend.ai.service;

public interface RiskIndicatorCollector {
    RiskIndicatorResult collect(RiskCalculationContext context);
}
