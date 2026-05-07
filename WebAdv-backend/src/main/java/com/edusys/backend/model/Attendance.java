package com.edusys.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    public enum Status {
        present, absent, late, excused, sick
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "teaching_assignment_id")
    private TeachingAssignment teachingAssignment;

    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    @Column(name = "period_number")
    private Integer periodNumber;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String remarks;

    @ManyToOne
    @JoinColumn(name = "marked_by")
    private User markedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public TeachingAssignment getTeachingAssignment() { return teachingAssignment; }
    public void setTeachingAssignment(TeachingAssignment teachingAssignment) { this.teachingAssignment = teachingAssignment; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public Integer getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(Integer periodNumber) { this.periodNumber = periodNumber; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public User getMarkedBy() { return markedBy; }
    public void setMarkedBy(User markedBy) { this.markedBy = markedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}
