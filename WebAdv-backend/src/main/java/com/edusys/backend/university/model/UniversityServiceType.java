package com.edusys.backend.university.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "university_service_types")
public class UniversityServiceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "default_office", nullable = false, length = 120)
    private String defaultOffice;

    @Column(name = "sla_days", nullable = false)
    private Integer slaDays = 5;

    @Column(name = "requires_finance_clearance", nullable = false)
    private Boolean requiresFinanceClearance = false;

    @Column(name = "requires_attachment", nullable = false)
    private Boolean requiresAttachment = false;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDefaultOffice() { return defaultOffice; }
    public void setDefaultOffice(String defaultOffice) { this.defaultOffice = defaultOffice; }
    public Integer getSlaDays() { return slaDays; }
    public void setSlaDays(Integer slaDays) { this.slaDays = slaDays; }
    public Boolean getRequiresFinanceClearance() { return requiresFinanceClearance; }
    public void setRequiresFinanceClearance(Boolean requiresFinanceClearance) { this.requiresFinanceClearance = requiresFinanceClearance; }
    public Boolean getRequiresAttachment() { return requiresAttachment; }
    public void setRequiresAttachment(Boolean requiresAttachment) { this.requiresAttachment = requiresAttachment; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
