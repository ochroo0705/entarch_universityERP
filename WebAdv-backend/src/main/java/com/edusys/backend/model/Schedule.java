package com.edusys.backend.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teaching_assignment_id")
    private TeachingAssignment teachingAssignment;

    @Column(name = "day_of_week")
    private Integer dayOfWeek; // 1-7

    @Column(name = "period_number")
    private Integer periodNumber;

    private LocalTime startTime;
    private LocalTime endTime;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TeachingAssignment getTeachingAssignment() { return teachingAssignment; }
    public void setTeachingAssignment(TeachingAssignment teachingAssignment) { this.teachingAssignment = teachingAssignment; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Integer getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(Integer periodNumber) { this.periodNumber = periodNumber; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

}
