package com.edusys.backend.ai.dto;

import java.util.List;

public record RiskDashboardBundleResponse(
        List<RiskDashboardListItemResponse> items,
        RiskSummaryResponse summary
) {
}
