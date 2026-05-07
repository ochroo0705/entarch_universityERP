package com.edusys.backend.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.ai.risk", name = "schedule-enabled", havingValue = "true", matchIfMissing = true)
public class RiskRecalculationScheduler {

    private static final Logger log = LoggerFactory.getLogger(RiskRecalculationScheduler.class);

    private final RiskSnapshotService riskSnapshotService;

    public RiskRecalculationScheduler(RiskSnapshotService riskSnapshotService) {
        this.riskSnapshotService = riskSnapshotService;
    }

    @Scheduled(cron = "${app.ai.risk.recalculation-cron:0 0 2 * * *}")
    public void runNightlyRecalculation() {
        int processed = riskSnapshotService.runScheduledRecalculation();
        log.info("Scheduled AI risk recalculation completed for {} students", processed);
    }
}
