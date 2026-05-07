package com.edusys.backend.university.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "university_academic_policies")
public class UniversityAcademicPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_name", nullable = false, length = 120)
    private String policyName;

    @Column(name = "min_term_credits", nullable = false)
    private Integer minTermCredits;

    @Column(name = "max_term_credits", nullable = false)
    private Integer maxTermCredits;

    @Column(name = "probation_max_term_credits", nullable = false)
    private Integer probationMaxTermCredits;

    @Column(name = "min_average_grade_good_standing", nullable = false, precision = 5, scale = 2)
    private BigDecimal minAverageGradeGoodStanding;

    @Column(name = "block_registration_when_probation", nullable = false)
    private Boolean blockRegistrationWhenProbation;

    @Column(name = "allow_repeat_completed_courses", nullable = false)
    private Boolean allowRepeatCompletedCourses;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }
    public Integer getMinTermCredits() { return minTermCredits; }
    public void setMinTermCredits(Integer minTermCredits) { this.minTermCredits = minTermCredits; }
    public Integer getMaxTermCredits() { return maxTermCredits; }
    public void setMaxTermCredits(Integer maxTermCredits) { this.maxTermCredits = maxTermCredits; }
    public Integer getProbationMaxTermCredits() { return probationMaxTermCredits; }
    public void setProbationMaxTermCredits(Integer probationMaxTermCredits) { this.probationMaxTermCredits = probationMaxTermCredits; }
    public BigDecimal getMinAverageGradeGoodStanding() { return minAverageGradeGoodStanding; }
    public void setMinAverageGradeGoodStanding(BigDecimal minAverageGradeGoodStanding) { this.minAverageGradeGoodStanding = minAverageGradeGoodStanding; }
    public Boolean getBlockRegistrationWhenProbation() { return blockRegistrationWhenProbation; }
    public void setBlockRegistrationWhenProbation(Boolean blockRegistrationWhenProbation) { this.blockRegistrationWhenProbation = blockRegistrationWhenProbation; }
    public Boolean getAllowRepeatCompletedCourses() { return allowRepeatCompletedCourses; }
    public void setAllowRepeatCompletedCourses(Boolean allowRepeatCompletedCourses) { this.allowRepeatCompletedCourses = allowRepeatCompletedCourses; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
