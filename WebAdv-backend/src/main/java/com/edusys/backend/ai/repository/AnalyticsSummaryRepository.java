package com.edusys.backend.ai.repository;

import com.edusys.backend.ai.model.AnalyticsSummary;
import com.edusys.backend.ai.model.SummaryScopeType;
import com.edusys.backend.ai.model.SummaryStatus;
import com.edusys.backend.ai.model.SummaryType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AnalyticsSummaryRepository extends JpaRepository<AnalyticsSummary, Long> {

    @Override
    @EntityGraph(attributePaths = {"requestedByUser"})
    List<AnalyticsSummary> findAll();

    @Override
    @EntityGraph(attributePaths = {"requestedByUser"})
    Optional<AnalyticsSummary> findById(Long id);

    @EntityGraph(attributePaths = {"requestedByUser"})
    List<AnalyticsSummary> findBySummaryTypeAndScopeTypeAndScopeKeyOrderByGeneratedAtDesc(
            SummaryType summaryType,
            SummaryScopeType scopeType,
            String scopeKey
    );

    @EntityGraph(attributePaths = {"requestedByUser"})
    Optional<AnalyticsSummary> findFirstBySummaryTypeAndScopeTypeAndScopeKeyAndPeriodStartAndPeriodEndAndInputFingerprintAndStatusOrderByGeneratedAtDesc(
            SummaryType summaryType,
            SummaryScopeType scopeType,
            String scopeKey,
            LocalDate periodStart,
            LocalDate periodEnd,
            String inputFingerprint,
            SummaryStatus status
    );
}
