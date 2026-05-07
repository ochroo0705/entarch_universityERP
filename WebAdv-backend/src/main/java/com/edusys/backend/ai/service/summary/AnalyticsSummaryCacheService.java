package com.edusys.backend.ai.service.summary;

import com.edusys.backend.ai.model.AnalyticsSummary;
import com.edusys.backend.ai.model.SummaryStatus;
import com.edusys.backend.ai.repository.AnalyticsSummaryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AnalyticsSummaryCacheService {

    private final AnalyticsSummaryRepository analyticsSummaryRepository;

    public AnalyticsSummaryCacheService(AnalyticsSummaryRepository analyticsSummaryRepository) {
        this.analyticsSummaryRepository = analyticsSummaryRepository;
    }

    public Optional<AnalyticsSummary> findReusableSummary(AnalyticsSummaryInputBuilder.PreparedAnalyticsSummaryInput prepared) {
        return analyticsSummaryRepository
                .findFirstBySummaryTypeAndScopeTypeAndScopeKeyAndPeriodStartAndPeriodEndAndInputFingerprintAndStatusOrderByGeneratedAtDesc(
                        prepared.input().summaryType(),
                        prepared.input().scopeType(),
                        prepared.input().scopeKey(),
                        prepared.input().periodStart(),
                        prepared.input().periodEnd(),
                        prepared.fingerprint(),
                        SummaryStatus.READY
                )
                .filter(summary -> summary.getStaleAfter() == null || summary.getStaleAfter().isAfter(LocalDateTime.now()));
    }

    public boolean isStale(AnalyticsSummary summary) {
        return summary.getStatus() == SummaryStatus.STALE
                || summary.getStaleAfter() == null
                || summary.getStaleAfter().isBefore(LocalDateTime.now());
    }
}
