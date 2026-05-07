package com.edusys.backend.ai.model;

import com.edusys.backend.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "parent_message_draft")
public class ParentMessageDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user_id", nullable = false)
    private User parentUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_snapshot_id")
    private StudentRiskSnapshot riskSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "draft_status", nullable = false)
    private DraftStatus draftStatus = DraftStatus.REQUESTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private DraftChannel channel = DraftChannel.EMAIL;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false)
    private IssueType issueType = IssueType.GENERAL_FOLLOW_UP;

    @Column(name = "teacher_note", length = 1000)
    private String teacherNote;

    @Column(name = "goal_label")
    private String goalLabel;

    @Column(name = "generated_subject")
    private String generatedSubject;

    @Column(name = "generated_message_body")
    private String generatedMessageBody;

    @Column(name = "current_subject")
    private String currentSubject;

    @Column(name = "current_message_body")
    private String currentMessageBody;

    @Column(name = "tone_label")
    private String toneLabel;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "generation_source")
    private String generationSource;

    @Column(name = "generation_provider")
    private String generationProvider;

    @Column(name = "generation_model")
    private String generationModel;

    @Column(name = "provider_request_id")
    private String providerRequestId;

    @Column(name = "generation_prompt_version")
    private String generationPromptVersion;

    @Column(name = "generation_input_redacted_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String generationInputRedactedJson;

    @Column(name = "generation_output_redacted_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String generationOutputRedactedJson;

    @Column(name = "generation_error_code")
    private String generationErrorCode;

    @Column(name = "generation_error_message")
    private String generationErrorMessage;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "is_placeholder", nullable = false)
    private Boolean isPlaceholder = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_edited_by_user_id")
    private User lastEditedByUser;

    @Column(name = "last_edited_at")
    private LocalDateTime lastEditedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedByUser;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by_user_id")
    private User rejectedByUser;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (lastEditedAt == null) {
            lastEditedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public User getParentUser() { return parentUser; }
    public void setParentUser(User parentUser) { this.parentUser = parentUser; }
    public StudentRiskSnapshot getRiskSnapshot() { return riskSnapshot; }
    public void setRiskSnapshot(StudentRiskSnapshot riskSnapshot) { this.riskSnapshot = riskSnapshot; }
    public User getCreatedByUser() { return createdByUser; }
    public void setCreatedByUser(User createdByUser) { this.createdByUser = createdByUser; }
    public DraftStatus getDraftStatus() { return draftStatus; }
    public void setDraftStatus(DraftStatus draftStatus) { this.draftStatus = draftStatus; }
    public DraftChannel getChannel() { return channel; }
    public void setChannel(DraftChannel channel) { this.channel = channel; }
    public IssueType getIssueType() { return issueType; }
    public void setIssueType(IssueType issueType) { this.issueType = issueType; }
    public String getTeacherNote() { return teacherNote; }
    public void setTeacherNote(String teacherNote) { this.teacherNote = teacherNote; }
    public String getGoalLabel() { return goalLabel; }
    public void setGoalLabel(String goalLabel) { this.goalLabel = goalLabel; }
    public String getGeneratedSubject() { return generatedSubject; }
    public void setGeneratedSubject(String generatedSubject) { this.generatedSubject = generatedSubject; }
    public String getGeneratedMessageBody() { return generatedMessageBody; }
    public void setGeneratedMessageBody(String generatedMessageBody) { this.generatedMessageBody = generatedMessageBody; }
    public String getCurrentSubject() { return currentSubject; }
    public void setCurrentSubject(String currentSubject) { this.currentSubject = currentSubject; }
    public String getCurrentMessageBody() { return currentMessageBody; }
    public void setCurrentMessageBody(String currentMessageBody) { this.currentMessageBody = currentMessageBody; }
    public String getToneLabel() { return toneLabel; }
    public void setToneLabel(String toneLabel) { this.toneLabel = toneLabel; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
    public String getGenerationSource() { return generationSource; }
    public void setGenerationSource(String generationSource) { this.generationSource = generationSource; }
    public String getGenerationProvider() { return generationProvider; }
    public void setGenerationProvider(String generationProvider) { this.generationProvider = generationProvider; }
    public String getGenerationModel() { return generationModel; }
    public void setGenerationModel(String generationModel) { this.generationModel = generationModel; }
    public String getProviderRequestId() { return providerRequestId; }
    public void setProviderRequestId(String providerRequestId) { this.providerRequestId = providerRequestId; }
    public String getGenerationPromptVersion() { return generationPromptVersion; }
    public void setGenerationPromptVersion(String generationPromptVersion) { this.generationPromptVersion = generationPromptVersion; }
    public String getGenerationInputRedactedJson() { return generationInputRedactedJson; }
    public void setGenerationInputRedactedJson(String generationInputRedactedJson) { this.generationInputRedactedJson = generationInputRedactedJson; }
    public String getGenerationOutputRedactedJson() { return generationOutputRedactedJson; }
    public void setGenerationOutputRedactedJson(String generationOutputRedactedJson) { this.generationOutputRedactedJson = generationOutputRedactedJson; }
    public String getGenerationErrorCode() { return generationErrorCode; }
    public void setGenerationErrorCode(String generationErrorCode) { this.generationErrorCode = generationErrorCode; }
    public String getGenerationErrorMessage() { return generationErrorMessage; }
    public void setGenerationErrorMessage(String generationErrorMessage) { this.generationErrorMessage = generationErrorMessage; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public Boolean getIsPlaceholder() { return isPlaceholder; }
    public void setIsPlaceholder(Boolean placeholder) { isPlaceholder = placeholder; }
    public User getLastEditedByUser() { return lastEditedByUser; }
    public void setLastEditedByUser(User lastEditedByUser) { this.lastEditedByUser = lastEditedByUser; }
    public LocalDateTime getLastEditedAt() { return lastEditedAt; }
    public void setLastEditedAt(LocalDateTime lastEditedAt) { this.lastEditedAt = lastEditedAt; }
    public User getApprovedByUser() { return approvedByUser; }
    public void setApprovedByUser(User approvedByUser) { this.approvedByUser = approvedByUser; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public User getRejectedByUser() { return rejectedByUser; }
    public void setRejectedByUser(User rejectedByUser) { this.rejectedByUser = rejectedByUser; }
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
