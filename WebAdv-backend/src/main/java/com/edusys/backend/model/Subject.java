package com.edusys.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Column(name = "subject_name_mn")
    private String subjectNameMn;

    @Column(name = "subject_code", unique = true)
    private String subjectCode;

    @Column(name = "grade_level")
    private Integer gradeLevel;

    @Column(name = "hours_per_week")
    private Integer hoursPerWeek;

    @Column(name = "is_mandatory")
    private Boolean isMandatory = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return subjectName; }
    public void setName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectNameMn() { return subjectNameMn; }
    public void setSubjectNameMn(String subjectNameMn) { this.subjectNameMn = subjectNameMn; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public Integer getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(Integer gradeLevel) { this.gradeLevel = gradeLevel; }

    public Integer getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(Integer hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }

    public Boolean getIsMandatory() { return isMandatory; }
    public void setIsMandatory(Boolean isMandatory) { this.isMandatory = isMandatory; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}
