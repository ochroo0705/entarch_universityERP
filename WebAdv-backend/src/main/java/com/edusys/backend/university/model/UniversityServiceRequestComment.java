package com.edusys.backend.university.model;

import com.edusys.backend.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "university_service_request_comments")
public class UniversityServiceRequestComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private UniversityServiceRequest request;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;

    @Column(nullable = false)
    private Boolean internal = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UniversityServiceRequest getRequest() { return request; }
    public void setRequest(UniversityServiceRequest request) { this.request = request; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
    public Boolean getInternal() { return internal; }
    public void setInternal(Boolean internal) { this.internal = internal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
