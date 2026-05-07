package com.edusys.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "homework_submissions")
public class HomeworkSubmission {

    public enum Status {
        assigned, submitted, late, graded, missing
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "homework_id")
    private Homework homework;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @Column(name = "submission_text")
    private String submissionText;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    private Integer score;
    private String feedback;

    @ManyToOne
    @JoinColumn(name = "graded_by")
    private User gradedBy;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Enumerated(EnumType.STRING)
    private Status status = Status.assigned;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Homework getHomework() { return homework; }
    public void setHomework(Homework homework) { this.homework = homework; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public String getSubmissionText() { return submissionText; }
    public void setSubmissionText(String submissionText) { this.submissionText = submissionText; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public User getGradedBy() { return gradedBy; }
    public void setGradedBy(User gradedBy) { this.gradedBy = gradedBy; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
