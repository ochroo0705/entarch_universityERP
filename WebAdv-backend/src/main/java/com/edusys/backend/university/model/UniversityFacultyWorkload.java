package com.edusys.backend.university.model;

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
@Table(name = "university_faculty_workloads")
public class UniversityFacultyWorkload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "faculty_profile_id")
    private UniversityFacultyProfile facultyProfile;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(nullable = false)
    private Integer semester;

    @Column(name = "teaching_credits", nullable = false)
    private Integer teachingCredits = 0;

    @Column(name = "advising_credits", nullable = false)
    private Integer advisingCredits = 0;

    @Column(name = "research_credits", nullable = false)
    private Integer researchCredits = 0;

    @Column(name = "committee_credits", nullable = false)
    private Integer committeeCredits = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UniversityFacultyProfile getFacultyProfile() { return facultyProfile; }
    public void setFacultyProfile(UniversityFacultyProfile facultyProfile) { this.facultyProfile = facultyProfile; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
    public Integer getTeachingCredits() { return teachingCredits; }
    public void setTeachingCredits(Integer teachingCredits) { this.teachingCredits = teachingCredits; }
    public Integer getAdvisingCredits() { return advisingCredits; }
    public void setAdvisingCredits(Integer advisingCredits) { this.advisingCredits = advisingCredits; }
    public Integer getResearchCredits() { return researchCredits; }
    public void setResearchCredits(Integer researchCredits) { this.researchCredits = researchCredits; }
    public Integer getCommitteeCredits() { return committeeCredits; }
    public void setCommitteeCredits(Integer committeeCredits) { this.committeeCredits = committeeCredits; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
