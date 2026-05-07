package com.edusys.backend.ai.model;

import com.edusys.backend.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_risk_snapshot")
public class StudentRiskSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private com.edusys.backend.model.Class classEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "snapshot_status", nullable = false)
    private SnapshotStatus snapshotStatus = SnapshotStatus.GENERATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(name = "attendance_rate", precision = 5, scale = 2)
    private BigDecimal attendanceRate;

    @Column(name = "missing_homework_count", nullable = false)
    private Integer missingHomeworkCount = 0;

    @Column(name = "grade_average", precision = 5, scale = 2)
    private BigDecimal gradeAverage;

    @Column(name = "source_summary_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String sourceSummaryJson;

    @Column(name = "reason_summary")
    private String reasonSummary;

    @Column(name = "recommended_action")
    private String recommendedAction;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "calculation_window_start")
    private LocalDateTime calculationWindowStart;

    @Column(name = "calculation_window_end")
    private LocalDateTime calculationWindowEnd;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedByUser;

    @Column(name = "model_version_label")
    private String modelVersionLabel;

    @Column(name = "scoring_config_version")
    private String scoringConfigVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_trigger")
    private CalculationTrigger calculationTrigger;

    @Column(name = "calculation_error")
    private String calculationError;

    @Column(name = "grade_level")
    private Integer gradeLevel;

    @Column(name = "is_placeholder", nullable = false)
    private Boolean isPlaceholder = true;

    @OneToMany(mappedBy = "riskSnapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentRiskIndicatorSnapshot> indicatorSnapshots = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (calculatedAt == null) calculatedAt = now;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public User getCreatedByUser() { return createdByUser; }
    public void setCreatedByUser(User createdByUser) { this.createdByUser = createdByUser; }
    public com.edusys.backend.model.Class getClassEntity() { return classEntity; }
    public void setClassEntity(com.edusys.backend.model.Class classEntity) { this.classEntity = classEntity; }
    public SnapshotStatus getSnapshotStatus() { return snapshotStatus; }
    public void setSnapshotStatus(SnapshotStatus snapshotStatus) { this.snapshotStatus = snapshotStatus; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public BigDecimal getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(BigDecimal attendanceRate) { this.attendanceRate = attendanceRate; }
    public Integer getMissingHomeworkCount() { return missingHomeworkCount; }
    public void setMissingHomeworkCount(Integer missingHomeworkCount) { this.missingHomeworkCount = missingHomeworkCount; }
    public BigDecimal getGradeAverage() { return gradeAverage; }
    public void setGradeAverage(BigDecimal gradeAverage) { this.gradeAverage = gradeAverage; }
    public String getSourceSummaryJson() { return sourceSummaryJson; }
    public void setSourceSummaryJson(String sourceSummaryJson) { this.sourceSummaryJson = sourceSummaryJson; }
    public String getReasonSummary() { return reasonSummary; }
    public void setReasonSummary(String reasonSummary) { this.reasonSummary = reasonSummary; }
    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    public LocalDateTime getCalculationWindowStart() { return calculationWindowStart; }
    public void setCalculationWindowStart(LocalDateTime calculationWindowStart) { this.calculationWindowStart = calculationWindowStart; }
    public LocalDateTime getCalculationWindowEnd() { return calculationWindowEnd; }
    public void setCalculationWindowEnd(LocalDateTime calculationWindowEnd) { this.calculationWindowEnd = calculationWindowEnd; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public User getReviewedByUser() { return reviewedByUser; }
    public void setReviewedByUser(User reviewedByUser) { this.reviewedByUser = reviewedByUser; }
    public String getModelVersionLabel() { return modelVersionLabel; }
    public void setModelVersionLabel(String modelVersionLabel) { this.modelVersionLabel = modelVersionLabel; }
    public String getScoringConfigVersion() { return scoringConfigVersion; }
    public void setScoringConfigVersion(String scoringConfigVersion) { this.scoringConfigVersion = scoringConfigVersion; }
    public CalculationTrigger getCalculationTrigger() { return calculationTrigger; }
    public void setCalculationTrigger(CalculationTrigger calculationTrigger) { this.calculationTrigger = calculationTrigger; }
    public String getCalculationError() { return calculationError; }
    public void setCalculationError(String calculationError) { this.calculationError = calculationError; }
    public Integer getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(Integer gradeLevel) { this.gradeLevel = gradeLevel; }
    public Boolean getIsPlaceholder() { return isPlaceholder; }
    public void setIsPlaceholder(Boolean placeholder) { isPlaceholder = placeholder; }
    public List<StudentRiskIndicatorSnapshot> getIndicatorSnapshots() { return indicatorSnapshots; }
    public void setIndicatorSnapshots(List<StudentRiskIndicatorSnapshot> indicatorSnapshots) { this.indicatorSnapshots = indicatorSnapshots; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
