package com.edusys.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grades")
public class Grade {

    public enum GradeType {
        QUARTER("quarter"), MIDTERM("midterm"), FINAL("final"), YEARLY("yearly");

        private final String dbValue;
        GradeType(String dbValue) { this.dbValue = dbValue; }
        public String getDbValue() { return dbValue; }

        public static GradeType fromDbValue(String v) {
            for (GradeType t : values()) {
                if (t.dbValue.equals(v)) return t;
            }
            throw new IllegalArgumentException("Unknown grade type: " + v);
        }
    }

    @jakarta.persistence.Converter(autoApply = true)
    public static class GradeTypeConverter implements AttributeConverter<GradeType, String> {
        @Override
        public String convertToDatabaseColumn(GradeType attr) {
            return attr == null ? null : attr.getDbValue();
        }

        @Override
        public GradeType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : GradeType.fromDbValue(dbData);
        }
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

    private Integer quarter;
    private Integer gradeValue;

    @Convert(converter = GradeTypeConverter.class)
    private GradeType gradeType;

    @ManyToOne
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public TeachingAssignment getTeachingAssignment() { return teachingAssignment; }
    public void setTeachingAssignment(TeachingAssignment teachingAssignment) { this.teachingAssignment = teachingAssignment; }

    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }

    public Integer getGradeValue() { return gradeValue; }
    public void setGradeValue(Integer gradeValue) { this.gradeValue = gradeValue; }

    public GradeType getGradeType() { return gradeType; }
    public void setGradeType(GradeType gradeType) { this.gradeType = gradeType; }

    public User getRecordedBy() { return recordedBy; }
    public void setRecordedBy(User recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
