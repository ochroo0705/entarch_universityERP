package com.edusys.backend.ai.service;

import com.edusys.backend.ai.model.AiAuditEventType;
import com.edusys.backend.ai.model.CalculationTrigger;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskRecalculationAsyncWorker {

    private static final Logger log = LoggerFactory.getLogger(RiskRecalculationAsyncWorker.class);

    private final RiskSnapshotService riskSnapshotService;
    private final UserRepository userRepository;
    private final RiskRecalculationRunGuardService riskRecalculationRunGuardService;

    public RiskRecalculationAsyncWorker(
            RiskSnapshotService riskSnapshotService,
            UserRepository userRepository,
            RiskRecalculationRunGuardService riskRecalculationRunGuardService
    ) {
        this.riskSnapshotService = riskSnapshotService;
        this.userRepository = userRepository;
        this.riskRecalculationRunGuardService = riskRecalculationRunGuardService;
    }

    @Async
    public void recalculateScopeAsync(Long actorId, List<Long> studentIds) {
        try {
            User actor = userRepository.findById(actorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Actor not found for background risk recalculation"));

            int successCount = 0;
            for (Long studentId : studentIds) {
                try {
                    riskSnapshotService.generateSnapshotInNewTransaction(
                            studentId,
                            actor,
                            CalculationTrigger.MANUAL_ADMIN,
                            AiAuditEventType.RISK_SNAPSHOT_RECALCULATED
                    );
                    successCount++;
                } catch (RuntimeException exception) {
                    log.error("Background AI risk recalculation failed for student {}", studentId, exception);
                }
            }
            log.info("Background AI risk recalculation finished: {} of {} students succeeded", successCount, studentIds.size());
        } finally {
            riskRecalculationRunGuardService.finish();
        }
    }
}
