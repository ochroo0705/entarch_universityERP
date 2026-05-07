package com.edusys.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "classes")
public class Class {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name", nullable = false)
    private String className;

    private Integer grade;
    private String section;

    @ManyToOne
    @JoinColumn(name = "homeroom_teacher_id")
    private User homeroomTeacher;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "academic_year")
    private String academicYear;

    @Column(name = "student_count")
    private Integer studentCount = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("classEntity")
    private Set<StudentEnrollment> studentEnrollments;

        @ManyToMany
        @JoinTable(
            name = "class_assistant_teachers",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
        )
        private Set<User> assistantTeachers = new HashSet<>();


    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public User getHomeroomTeacher() { return homeroomTeacher; }
    public void setHomeroomTeacher(User homeroomTeacher) { this.homeroomTeacher = homeroomTeacher; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Set<StudentEnrollment> getStudentEnrollments() {
        return studentEnrollments;
    }

    public void setStudentEnrollments(Set<StudentEnrollment> studentEnrollments) {
        this.studentEnrollments = studentEnrollments;
    }

    public Set<User> getAssistantTeachers() {
        return assistantTeachers;
    }

    public void setAssistantTeachers(Set<User> assistantTeachers) {
        this.assistantTeachers = assistantTeachers;
    }

}
