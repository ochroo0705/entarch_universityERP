package com.edusys.backend.ai.model;

import com.edusys.backend.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_summary")
public class AnalyticsSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_type", nullable = false)
    private SummaryType summaryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false)
    private SummaryScopeType scopeType;

    @Column(name = "scope_key", nullable = false)
    private String scopeKey;

    @Column(name = "scope_label")
    private String scopeLabel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedByUser;

    @Column(name = "generated_for_role", nullable = false)
    private String generatedForRole;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "comparison_period_start")
    private LocalDate comparisonPeriodStart;

    @Column(name = "comparison_period_end")
    private LocalDate comparisonPeriodEnd;

    @Column(name = "input_fingerprint", nullable = false)
    private String inputFingerprint;

    @Column(name = "input_redacted_json", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String inputRedactedJson;

    @Column(name = "summary_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String summaryJson;

    @Column(name = "headline")
    private String headline;

    @Column(name = "overall_summary_text")
    private String overallSummaryText;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "provider_model")
    private String providerModel;

    @Column(name = "provider_request_id")
    private String providerRequestId;

    @Column(name = "prompt_version", nullable = false)
    private String promptVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SummaryStatus status;

    @Column(name = "generation_error_code")
    private String generationErrorCode;

    @Column(name = "generation_error_message")
    private String generationErrorMessage;

    @Column(name = "is_placeholder", nullable = false)
    private Boolean isPlaceholder = Boolean.FALSE;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "stale_after")
    private LocalDateTime staleAfter;

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SummaryType getSummaryType() { return summaryType; }
    public void setSummaryType(SummaryType summaryType) { this.summaryType = summaryType; }
    public SummaryScopeType getScopeType() { return scopeType; }
    public void setScopeType(SummaryScopeType scopeType) { this.scopeType = scopeType; }
    public String getScopeKey() { return scopeKey; }
    public void setScopeKey(String scopeKey) { this.scopeKey = scopeKey; }
    public String getScopeLabel() { return scopeLabel; }
    public void setScopeLabel(String scopeLabel) { this.scopeLabel = scopeLabel; }
    public User getRequestedByUser() { return requestedByUser; }
    public void setRequestedByUser(User requestedByUser) { this.requestedByUser = requestedByUser; }
    public String getGeneratedForRole() { return generatedForRole; }
    public void setGeneratedForRole(String generatedForRole) { this.generatedForRole = generatedForRole; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public LocalDate getComparisonPeriodStart() { return comparisonPeriodStart; }
    public void setComparisonPeriodStart(LocalDate comparisonPeriodStart) { this.comparisonPeriodStart = comparisonPeriodStart; }
    public LocalDate getComparisonPeriodEnd() { return comparisonPeriodEnd; }
    public void setComparisonPeriodEnd(LocalDate comparisonPeriodEnd) { this.comparisonPeriodEnd = comparisonPeriodEnd; }
    public String getInputFingerprint() { return inputFingerprint; }
    public void setInputFingerprint(String inputFingerprint) { this.inputFingerprint = inputFingerprint; }
    public String getInputRedactedJson() { return inputRedactedJson; }
    public void setInputRedactedJson(String inputRedactedJson) { this.inputRedactedJson = inputRedactedJson; }
    public String getSummaryJson() { return summaryJson; }
    public void setSummaryJson(String summaryJson) { this.summaryJson = summaryJson; }
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public String getOverallSummaryText() { return overallSummaryText; }
    public void setOverallSummaryText(String overallSummaryText) { this.overallSummaryText = overallSummaryText; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getProviderModel() { return providerModel; }
    public void setProviderModel(String providerModel) { this.providerModel = providerModel; }
    public String getProviderRequestId() { return providerRequestId; }
    public void setProviderRequestId(String providerRequestId) { this.providerRequestId = providerRequestId; }
    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }
    public SummaryStatus getStatus() { return status; }
    public void setStatus(SummaryStatus status) { this.status = status; }
    public String getGenerationErrorCode() { return generationErrorCode; }
    public void setGenerationErrorCode(String generationErrorCode) { this.generationErrorCode = generationErrorCode; }
    public String getGenerationErrorMessage() { return generationErrorMessage; }
    public void setGenerationErrorMessage(String generationErrorMessage) { this.generationErrorMessage = generationErrorMessage; }
    public Boolean getIsPlaceholder() { return isPlaceholder; }
    public void setIsPlaceholder(Boolean placeholder) { isPlaceholder = placeholder; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public LocalDateTime getStaleAfter() { return staleAfter; }
    public void setStaleAfter(LocalDateTime staleAfter) { this.staleAfter = staleAfter; }
    public LocalDateTime getLastViewedAt() { return lastViewedAt; }
    public void setLastViewedAt(LocalDateTime lastViewedAt) { this.lastViewedAt = lastViewedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
