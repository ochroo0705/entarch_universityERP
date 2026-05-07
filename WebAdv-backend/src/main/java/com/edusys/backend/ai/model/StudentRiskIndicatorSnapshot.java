package com.edusys.backend.ai.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_risk_indicator_snapshot")
public class StudentRiskIndicatorSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_snapshot_id", nullable = false)
    private StudentRiskSnapshot riskSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "indicator_code", nullable = false)
    private RiskIndicatorCode indicatorCode;

    @Column(name = "raw_value", precision = 8, scale = 2)
    private BigDecimal rawValue;

    @Column(name = "normalized_risk_value", nullable = false, precision = 8, scale = 2)
    private BigDecimal normalizedRiskValue;

    @Column(name = "weight", nullable = false, precision = 8, scale = 4)
    private BigDecimal weight;

    @Column(name = "weighted_contribution", nullable = false, precision = 8, scale = 2)
    private BigDecimal weightedContribution;

    @Column(name = "data_points_count", nullable = false)
    private Integer dataPointsCount = 0;

    @Column(name = "is_missing_data", nullable = false)
    private Boolean isMissingData = false;

    @Column(name = "details_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String detailsJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StudentRiskSnapshot getRiskSnapshot() { return riskSnapshot; }
    public void setRiskSnapshot(StudentRiskSnapshot riskSnapshot) { this.riskSnapshot = riskSnapshot; }
    public RiskIndicatorCode getIndicatorCode() { return indicatorCode; }
    public void setIndicatorCode(RiskIndicatorCode indicatorCode) { this.indicatorCode = indicatorCode; }
    public BigDecimal getRawValue() { return rawValue; }
    public void setRawValue(BigDecimal rawValue) { this.rawValue = rawValue; }
    public BigDecimal getNormalizedRiskValue() { return normalizedRiskValue; }
    public void setNormalizedRiskValue(BigDecimal normalizedRiskValue) { this.normalizedRiskValue = normalizedRiskValue; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public BigDecimal getWeightedContribution() { return weightedContribution; }
    public void setWeightedContribution(BigDecimal weightedContribution) { this.weightedContribution = weightedContribution; }
    public Integer getDataPointsCount() { return dataPointsCount; }
    public void setDataPointsCount(Integer dataPointsCount) { this.dataPointsCount = dataPointsCount; }
    public Boolean getIsMissingData() { return isMissingData; }
    public void setIsMissingData(Boolean missingData) { isMissingData = missingData; }
    public String getDetailsJson() { return detailsJson; }
    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
