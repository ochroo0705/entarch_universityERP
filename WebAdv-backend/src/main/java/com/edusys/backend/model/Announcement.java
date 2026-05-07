package com.edusys.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
public class Announcement {

    public enum Priority {
        low, normal, high, urgent
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @Column(name = "target_role_flags")
    private Integer targetRoleFlags;

    @ManyToOne
    @JoinColumn(name = "target_class_id")
    private Class targetClass;  // JPA relationship

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.normal;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;  // JPA relationship

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getTargetRoleFlags() { return targetRoleFlags; }
    public void setTargetRoleFlags(Integer targetRoleFlags) { this.targetRoleFlags = targetRoleFlags; }

    public Class getTargetClass() { return targetClass; }
    public void setTargetClass(Class targetClass) { this.targetClass = targetClass; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

}
