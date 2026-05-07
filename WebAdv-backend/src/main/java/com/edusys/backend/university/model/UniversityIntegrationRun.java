package com.edusys.backend.university.model;

import com.edusys.backend.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "university_integration_runs")
public class UniversityIntegrationRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "integration_key", nullable = false, length = 60)
    private String integrationKey;

    @Column(name = "integration_name", nullable = false, length = 160)
    private String integrationName;

    @Column(nullable = false, length = 160)
    private String direction;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "result_message", columnDefinition = "TEXT")
    private String resultMessage;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne
    @JoinColumn(name = "actor_user_id")
    private User actorUser;

    @Column(name = "exchanged_at")
    private LocalDateTime exchangedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIntegrationKey() { return integrationKey; }
    public void setIntegrationKey(String integrationKey) { this.integrationKey = integrationKey; }
    public String getIntegrationName() { return integrationName; }
    public void setIntegrationName(String integrationName) { this.integrationName = integrationName; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getResultMessage() { return resultMessage; }
    public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public User getActorUser() { return actorUser; }
    public void setActorUser(User actorUser) { this.actorUser = actorUser; }
    public LocalDateTime getExchangedAt() { return exchangedAt; }
    public void setExchangedAt(LocalDateTime exchangedAt) { this.exchangedAt = exchangedAt; }
}
