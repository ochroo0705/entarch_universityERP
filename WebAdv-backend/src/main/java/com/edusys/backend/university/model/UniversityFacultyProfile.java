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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "university_faculty_profiles")
public class UniversityFacultyProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "faculty_user_id", nullable = false)
    private User facultyUser;

    @Column(name = "employee_number", length = 60)
    private String employeeNumber;

    @Column(nullable = false, length = 120)
    private String department;

    @Column(name = "academic_rank", length = 120)
    private String academicRank;

    @Column(name = "employment_status", nullable = false, length = 40)
    private String employmentStatus;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "office_location", length = 120)
    private String officeLocation;

    @Column(name = "workload_target_credits", nullable = false)
    private Integer workloadTargetCredits;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getFacultyUser() { return facultyUser; }
    public void setFacultyUser(User facultyUser) { this.facultyUser = facultyUser; }
    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getAcademicRank() { return academicRank; }
    public void setAcademicRank(String academicRank) { this.academicRank = academicRank; }
    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public String getOfficeLocation() { return officeLocation; }
    public void setOfficeLocation(String officeLocation) { this.officeLocation = officeLocation; }
    public Integer getWorkloadTargetCredits() { return workloadTargetCredits; }
    public void setWorkloadTargetCredits(Integer workloadTargetCredits) { this.workloadTargetCredits = workloadTargetCredits; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
