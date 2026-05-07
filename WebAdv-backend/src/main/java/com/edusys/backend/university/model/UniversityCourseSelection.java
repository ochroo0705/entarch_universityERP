package com.edusys.backend.university.model;

import com.edusys.backend.model.FeeInvoice;
import com.edusys.backend.model.Subject;
import com.edusys.backend.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "university_course_selections")
public class UniversityCourseSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(nullable = false)
    private Integer semester;

    @Column(nullable = false)
    private Integer credits;

    @Enumerated(EnumType.STRING)
    private CourseSelectionStatus status = CourseSelectionStatus.SELECTED;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private FeeInvoice invoice;

    @Column(name = "selected_at")
    private LocalDateTime selectedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public CourseSelectionStatus getStatus() { return status; }
    public void setStatus(CourseSelectionStatus status) { this.status = status; }
    public FeeInvoice getInvoice() { return invoice; }
    public void setInvoice(FeeInvoice invoice) { this.invoice = invoice; }
    public LocalDateTime getSelectedAt() { return selectedAt; }
    public void setSelectedAt(LocalDateTime selectedAt) { this.selectedAt = selectedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
