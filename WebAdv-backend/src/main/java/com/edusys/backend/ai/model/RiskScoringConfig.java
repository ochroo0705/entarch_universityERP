package com.edusys.backend.ai.model;

import com.edusys.backend.model.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_scoring_config")
public class RiskScoringConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false)
    private String configKey;

    @Column(name = "config_version", nullable = false)
    private String configVersion;

    @Column(name = "attendance_weight", nullable = false, precision = 8, scale = 4)
    private BigDecimal attendanceWeight;

    @Column(name = "lateness_weight", nullable = false, precision = 8, scale = 4)
    private BigDecimal latenessWeight;

    @Column(name = "homework_weight", nullable = false, precision = 8, scale = 4)
    private BigDecimal homeworkWeight;

    @Column(name = "grade_weight", nullable = false, precision = 8, scale = 4)
    private BigDecimal gradeWeight;

    @Column(name = "low_max_score", nullable = false)
    private Integer lowMaxScore;

    @Column(name = "medium_max_score", nullable = false)
    private Integer mediumMaxScore;

    @Column(name = "attendance_window_days", nullable = false)
    private Integer attendanceWindowDays;

    @Column(name = "homework_window_days", nullable = false)
    private Integer homeworkWindowDays;

    @Column(name = "grade_window_days", nullable = false)
    private Integer gradeWindowDays;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

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
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigVersion() { return configVersion; }
    public void setConfigVersion(String configVersion) { this.configVersion = configVersion; }
    public BigDecimal getAttendanceWeight() { return attendanceWeight; }
    public void setAttendanceWeight(BigDecimal attendanceWeight) { this.attendanceWeight = attendanceWeight; }
    public BigDecimal getLatenessWeight() { return latenessWeight; }
    public void setLatenessWeight(BigDecimal latenessWeight) { this.latenessWeight = latenessWeight; }
    public BigDecimal getHomeworkWeight() { return homeworkWeight; }
    public void setHomeworkWeight(BigDecimal homeworkWeight) { this.homeworkWeight = homeworkWeight; }
    public BigDecimal getGradeWeight() { return gradeWeight; }
    public void setGradeWeight(BigDecimal gradeWeight) { this.gradeWeight = gradeWeight; }
    public Integer getLowMaxScore() { return lowMaxScore; }
    public void setLowMaxScore(Integer lowMaxScore) { this.lowMaxScore = lowMaxScore; }
    public Integer getMediumMaxScore() { return mediumMaxScore; }
    public void setMediumMaxScore(Integer mediumMaxScore) { this.mediumMaxScore = mediumMaxScore; }
    public Integer getAttendanceWindowDays() { return attendanceWindowDays; }
    public void setAttendanceWindowDays(Integer attendanceWindowDays) { this.attendanceWindowDays = attendanceWindowDays; }
    public Integer getHomeworkWindowDays() { return homeworkWindowDays; }
    public void setHomeworkWindowDays(Integer homeworkWindowDays) { this.homeworkWindowDays = homeworkWindowDays; }
    public Integer getGradeWindowDays() { return gradeWindowDays; }
    public void setGradeWindowDays(Integer gradeWindowDays) { this.gradeWindowDays = gradeWindowDays; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
    public User getCreatedByUser() { return createdByUser; }
    public void setCreatedByUser(User createdByUser) { this.createdByUser = createdByUser; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
