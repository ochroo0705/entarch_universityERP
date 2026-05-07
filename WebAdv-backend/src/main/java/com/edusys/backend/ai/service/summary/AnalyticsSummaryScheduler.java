package com.edusys.backend.ai.service.summary;

import com.edusys.backend.ai.audit.AiAuditContext;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsSummaryScheduler {

    private final AnalyticsSummaryWorkflowService analyticsSummaryWorkflowService;
    private final UserRepository userRepository;
    private final boolean enabled;

    public AnalyticsSummaryScheduler(
            AnalyticsSummaryWorkflowService analyticsSummaryWorkflowService,
            UserRepository userRepository,
            @Value("${app.ai.analytics-summary.schedule-enabled:true}") boolean enabled
    ) {
        this.analyticsSummaryWorkflowService = analyticsSummaryWorkflowService;
        this.userRepository = userRepository;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${app.ai.analytics-summary.refresh-cron:0 15 2 * * *}")
    public void refreshSchoolWideSummary() {
        if (!enabled) {
            return;
        }
        User admin = userRepository.findFirstAdminUser()
                .orElseThrow(() -> new ResourceNotFoundException("Admin user required for analytics summary refresh"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(admin.getUsername(), null, java.util.List.of()));
        try {
            analyticsSummaryWorkflowService.refreshSchoolWideSummary(
                    new AiAuditContext("scheduled-analytics-summary-refresh", "127.0.0.1", "scheduler")
            );
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
