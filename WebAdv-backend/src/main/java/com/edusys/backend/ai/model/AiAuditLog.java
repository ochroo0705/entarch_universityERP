package com.edusys.backend.ai.model;

import com.edusys.backend.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_audit_log")
public class AiAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private AiAuditEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private AiEntityType entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", nullable = false)
    private User actorUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_student_id")
    private User targetStudent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_parent_user_id")
    private User targetParentUser;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "correlation_id")
    private String correlationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_status", nullable = false)
    private AiAuditActionStatus actionStatus;

    @Column(name = "reason_code")
    private String reasonCode;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "provider_model")
    private String providerModel;

    @Column(name = "details_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String detailsJson;

    @Column(name = "old_value_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String oldValueJson;

    @Column(name = "new_value_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String newValueJson;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AiAuditEventType getEventType() { return eventType; }
    public void setEventType(AiAuditEventType eventType) { this.eventType = eventType; }
    public AiEntityType getEntityType() { return entityType; }
    public void setEntityType(AiEntityType entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public User getActorUser() { return actorUser; }
    public void setActorUser(User actorUser) { this.actorUser = actorUser; }
    public User getTargetStudent() { return targetStudent; }
    public void setTargetStudent(User targetStudent) { this.targetStudent = targetStudent; }
    public User getTargetParentUser() { return targetParentUser; }
    public void setTargetParentUser(User targetParentUser) { this.targetParentUser = targetParentUser; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public AiAuditActionStatus getActionStatus() { return actionStatus; }
    public void setActionStatus(AiAuditActionStatus actionStatus) { this.actionStatus = actionStatus; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getProviderModel() { return providerModel; }
    public void setProviderModel(String providerModel) { this.providerModel = providerModel; }
    public String getDetailsJson() { return detailsJson; }
    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }
    public String getOldValueJson() { return oldValueJson; }
    public void setOldValueJson(String oldValueJson) { this.oldValueJson = oldValueJson; }
    public String getNewValueJson() { return newValueJson; }
    public void setNewValueJson(String newValueJson) { this.newValueJson = newValueJson; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
