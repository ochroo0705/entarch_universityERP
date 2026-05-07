package com.edusys.backend.ai.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class RiskRecalculationRunGuardService {

    private final AtomicBoolean scopeRunInProgress = new AtomicBoolean(false);
    private final AtomicReference<ScopeRunState> activeScopeRun = new AtomicReference<>();

    public ScopeRunStartResult tryStart(String scope, int requestedStudentCount, Long actorId, String triggerLabel) {
        ScopeRunState nextState = new ScopeRunState(scope, requestedStudentCount, LocalDateTime.now(), actorId, triggerLabel);
        if (scopeRunInProgress.compareAndSet(false, true)) {
            activeScopeRun.set(nextState);
            return new ScopeRunStartResult(true, nextState);
        }
        return new ScopeRunStartResult(false, activeScopeRun.get());
    }

    public void finish() {
        activeScopeRun.set(null);
        scopeRunInProgress.set(false);
    }

    public record ScopeRunStartResult(
            boolean started,
            ScopeRunState state
    ) {
    }

    public record ScopeRunState(
            String scope,
            int requestedStudentCount,
            LocalDateTime startedAt,
            Long actorId,
            String triggerLabel
    ) {
    }
}
