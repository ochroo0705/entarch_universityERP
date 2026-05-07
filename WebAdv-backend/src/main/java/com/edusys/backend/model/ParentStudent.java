package com.edusys.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "parent_students")
public class ParentStudent {

    public enum Relationship {
        father, mother, guardian, other,
        FATHER, MOTHER, GUARDIAN, OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private User parent;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @Enumerated(EnumType.STRING)
    private Relationship relationship;

    @Column(name = "is_primary_contact")
    private Boolean isPrimaryContact = false;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getParent() { return parent; }
    public void setParent(User parent) { this.parent = parent; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Relationship getRelationship() { return relationship; }
    public void setRelationship(Relationship relationship) { this.relationship = relationship; }

    public Boolean getIsPrimaryContact() { return isPrimaryContact; }
    public void setIsPrimaryContact(Boolean isPrimaryContact) { this.isPrimaryContact = isPrimaryContact; }
}
