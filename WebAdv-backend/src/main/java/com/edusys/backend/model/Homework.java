package com.edusys.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "homework")
public class Homework {

    public enum Type {
        HOMEWORK("homework"), PROJECT("project"), QUIZ("quiz"), TEST("test");

        private final String dbValue;
        Type(String dbValue) { this.dbValue = dbValue; }
        public String getDbValue() { return dbValue; }

        public static Type fromDbValue(String v) {
            for (Type t : values()) {
                if (t.dbValue.equals(v)) return t;
            }
            throw new IllegalArgumentException("Unknown homework type: " + v);
        }
    }

    @jakarta.persistence.Converter(autoApply = true)
    public static class TypeConverter implements AttributeConverter<Type, String> {
        @Override
        public String convertToDatabaseColumn(Type attr) {
            return attr == null ? null : attr.getDbValue();
        }

        @Override
        public Type convertToEntityAttribute(String dbData) {
            return dbData == null ? null : Type.fromDbValue(dbData);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teaching_assignment_id")
    private TeachingAssignment teachingAssignment;

    private String title;
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "max_score")
    private Integer maxScore;

    @Convert(converter = TypeConverter.class)
    private Type type;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "homework", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("uploadedAt ASC")
    private List<HomeworkAttachment> attachments = new ArrayList<>();

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TeachingAssignment getTeachingAssignment() { return teachingAssignment; }
    public void setTeachingAssignment(TeachingAssignment teachingAssignment) { this.teachingAssignment = teachingAssignment; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<HomeworkAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<HomeworkAttachment> attachments) { this.attachments = attachments; }
}
