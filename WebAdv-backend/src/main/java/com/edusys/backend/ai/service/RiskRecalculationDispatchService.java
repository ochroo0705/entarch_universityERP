package com.edusys.backend.ai.service;

import com.edusys.backend.ai.dto.RiskRecalculationJobResponse;
import com.edusys.backend.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskRecalculationDispatchService {

    private final RiskRecalculationAsyncWorker riskRecalculationAsyncWorker;
    private final RiskRecalculationRunGuardService riskRecalculationRunGuardService;

    public RiskRecalculationDispatchService(
            RiskRecalculationAsyncWorker riskRecalculationAsyncWorker,
            RiskRecalculationRunGuardService riskRecalculationRunGuardService
    ) {
        this.riskRecalculationAsyncWorker = riskRecalculationAsyncWorker;
        this.riskRecalculationRunGuardService = riskRecalculationRunGuardService;
    }

    public RiskRecalculationJobResponse dispatchScopeRecalculation(User actor, List<Long> studentIds, String scope) {
        RiskRecalculationRunGuardService.ScopeRunStartResult runStart =
                riskRecalculationRunGuardService.tryStart(scope, studentIds.size(), actor.getId(), "MANUAL_ADMIN");

        if (!runStart.started()) {
            RiskRecalculationRunGuardService.ScopeRunState activeRun = runStart.state();
            return new RiskRecalculationJobResponse(
                    "ALREADY_RUNNING",
                    activeRun != null ? activeRun.scope() : scope,
                    activeRun != null ? activeRun.requestedStudentCount() : studentIds.size(),
                    activeRun != null ? activeRun.startedAt() : null,
                    "Another risk recalculation is already running. Reusing the active background job."
            );
        }

        try {
            riskRecalculationAsyncWorker.recalculateScopeAsync(actor.getId(), studentIds);
        } catch (RuntimeException exception) {
            riskRecalculationRunGuardService.finish();
            throw exception;
        }

        return new RiskRecalculationJobResponse(
                "STARTED",
                scope,
                studentIds.size(),
                runStart.state().startedAt(),
                "Risk recalculation started in the background."
        );
    }
}
