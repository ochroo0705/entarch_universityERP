package com.edusys.backend.ai.model;

import com.edusys.backend.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_summary_request")
public class AnalyticsSummaryRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analytics_summary_id")
    private AnalyticsSummary analyticsSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_type", nullable = false)
    private SummaryType summaryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false)
    private SummaryScopeType scopeType;

    @Column(name = "scope_key", nullable = false)
    private String scopeKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_mode", nullable = false)
    private SummaryRequestMode requestMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedByUser;

    @Column(name = "force_refresh", nullable = false)
    private Boolean forceRefresh = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false)
    private SummaryRequestStatus requestStatus;

    @Column(name = "input_fingerprint")
    private String inputFingerprint;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AnalyticsSummary getAnalyticsSummary() { return analyticsSummary; }
    public void setAnalyticsSummary(AnalyticsSummary analyticsSummary) { this.analyticsSummary = analyticsSummary; }
    public SummaryType getSummaryType() { return summaryType; }
    public void setSummaryType(SummaryType summaryType) { this.summaryType = summaryType; }
    public SummaryScopeType getScopeType() { return scopeType; }
    public void setScopeType(SummaryScopeType scopeType) { this.scopeType = scopeType; }
    public String getScopeKey() { return scopeKey; }
    public void setScopeKey(String scopeKey) { this.scopeKey = scopeKey; }
    public SummaryRequestMode getRequestMode() { return requestMode; }
    public void setRequestMode(SummaryRequestMode requestMode) { this.requestMode = requestMode; }
    public User getRequestedByUser() { return requestedByUser; }
    public void setRequestedByUser(User requestedByUser) { this.requestedByUser = requestedByUser; }
    public Boolean getForceRefresh() { return forceRefresh; }
    public void setForceRefresh(Boolean forceRefresh) { this.forceRefresh = forceRefresh; }
    public SummaryRequestStatus getRequestStatus() { return requestStatus; }
    public void setRequestStatus(SummaryRequestStatus requestStatus) { this.requestStatus = requestStatus; }
    public String getInputFingerprint() { return inputFingerprint; }
    public void setInputFingerprint(String inputFingerprint) { this.inputFingerprint = inputFingerprint; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
