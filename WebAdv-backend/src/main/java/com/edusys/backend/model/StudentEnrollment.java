package com.edusys.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "student_enrollment")
public class StudentEnrollment {

    public enum Status {
        ACTIVE, GRADUATED, TRANSFERRED, active, DROPPED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "class_id")
    @JsonIgnoreProperties("studentEnrollments")
    private Class classEntity;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    @Column(name = "student_number", unique = true)
    private String studentNumber;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Class getClassEntity() { return classEntity; }
    public void setClassEntity(Class classEntity) { this.classEntity = classEntity; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

}
