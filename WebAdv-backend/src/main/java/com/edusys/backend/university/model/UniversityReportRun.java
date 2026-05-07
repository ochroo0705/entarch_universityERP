package com.edusys.backend.university.model;

import com.edusys.backend.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "university_report_runs")
public class UniversityReportRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "report_definition_id")
    private UniversityReportDefinition reportDefinition;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String filters;

    @Column(name = "snapshot_payload", columnDefinition = "TEXT")
    private String snapshotPayload;

    @Column(name = "row_count", nullable = false)
    private Long rowCount = 0L;

    @ManyToOne
    @JoinColumn(name = "actor_user_id")
    private User actorUser;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UniversityReportDefinition getReportDefinition() { return reportDefinition; }
    public void setReportDefinition(UniversityReportDefinition reportDefinition) { this.reportDefinition = reportDefinition; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFilters() { return filters; }
    public void setFilters(String filters) { this.filters = filters; }
    public String getSnapshotPayload() { return snapshotPayload; }
    public void setSnapshotPayload(String snapshotPayload) { this.snapshotPayload = snapshotPayload; }
    public Long getRowCount() { return rowCount; }
    public void setRowCount(Long rowCount) { this.rowCount = rowCount; }
    public User getActorUser() { return actorUser; }
    public void setActorUser(User actorUser) { this.actorUser = actorUser; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
