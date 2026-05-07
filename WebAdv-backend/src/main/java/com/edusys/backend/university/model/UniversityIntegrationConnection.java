package com.edusys.backend.university.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "university_integration_connections")
public class UniversityIntegrationConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "integration_key", nullable = false, unique = true, length = 60)
    private String integrationKey;

    @Column(name = "display_name", nullable = false, length = 160)
    private String displayName;

    @Column(name = "endpoint_url", length = 500)
    private String endpointUrl;

    @Column(name = "adapter_mode", nullable = false, length = 40)
    private String adapterMode = "MOCK";

    @Column(name = "auth_type", nullable = false, length = 40)
    private String authType = "NONE";

    @Column(name = "secret_ref", length = 160)
    private String secretRef;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "last_status", nullable = false, length = 40)
    private String lastStatus = "READY";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIntegrationKey() { return integrationKey; }
    public void setIntegrationKey(String integrationKey) { this.integrationKey = integrationKey; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    public String getAdapterMode() { return adapterMode; }
    public void setAdapterMode(String adapterMode) { this.adapterMode = adapterMode; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    public String getSecretRef() { return secretRef; }
    public void setSecretRef(String secretRef) { this.secretRef = secretRef; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getLastStatus() { return lastStatus; }
    public void setLastStatus(String lastStatus) { this.lastStatus = lastStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
