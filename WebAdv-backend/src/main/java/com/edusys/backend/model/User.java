package com.edusys.backend.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    public static final int ROLE_STUDENT = 1;
    public static final int ROLE_TEACHER = 2;
    public static final int ROLE_PARENT = 4;
    public static final int ROLE_ADMIN = 8;
    public static final int ROLE_COUNSELOR = 16;
    public static final int ROLE_NURSE = 32;
    public static final int ROLE_FINANCE_STAFF = 64;
    public static final int ROLE_LIBRARIAN = 128;
    public static final int ROLE_TRANSPORT_COORDINATOR = 256;
    public static final int ROLE_ADMISSIONS_STAFF = 512;
    public static final int ROLE_CAFETERIA_STAFF = 1024;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonAlias({"password", "rawPassword", "password_hash", "passwordHash"})
    private String passwordHash;

    private String firstName;
    private String lastName;
    private Integer roleFlags;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String profilePicture;
    private Boolean isActive = true;

    @Column(name = "teacher_subjects")
    private String teacherSubjects;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "student")
    private Set<StudentEnrollment> studentEnrollments;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getRoleFlags() {
        return roleFlags;
    }

    public void setRoleFlags(Integer roleFlags) {
        this.roleFlags = roleFlags;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTeacherSubjects() {
        return teacherSubjects;
    }

    public void setTeacherSubjects(String teacherSubjects) {
        this.teacherSubjects = teacherSubjects;
    }

    public Set<StudentEnrollment> getStudentEnrollments() {
        return studentEnrollments;
    }

    public void setStudentEnrollments(Set<StudentEnrollment> studentEnrollments) {
        this.studentEnrollments = studentEnrollments;
    }

    // ── Role helpers ──
    public boolean hasRole(int flag) {
        return roleFlags != null && (roleFlags & flag) != 0;
    }

    public boolean isStudent() { return hasRole(ROLE_STUDENT); }
    public boolean isTeacher() { return hasRole(ROLE_TEACHER); }
    public boolean isParent()  { return hasRole(ROLE_PARENT); }
    public boolean isAdmin()   { return hasRole(ROLE_ADMIN); }
    public boolean isCounselor() { return hasRole(ROLE_COUNSELOR); }
    public boolean isNurse() { return hasRole(ROLE_NURSE); }
    public boolean isFinanceStaff() { return hasRole(ROLE_FINANCE_STAFF); }
    public boolean isLibrarian() { return hasRole(ROLE_LIBRARIAN); }
    public boolean isTransportCoordinator() { return hasRole(ROLE_TRANSPORT_COORDINATOR); }
    public boolean isAdmissionsStaff() { return hasRole(ROLE_ADMISSIONS_STAFF); }
    public boolean isCafeteriaStaff() { return hasRole(ROLE_CAFETERIA_STAFF); }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
