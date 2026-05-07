package com.edusys.backend.ai.dto.summary;

import com.edusys.backend.ai.model.SummaryType;

import java.time.LocalDate;

public class AnalyticsSummaryQueryRequest {
    private SummaryType summaryType;
    private Long classId;
    private Integer gradeLevel;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String languageCode;

    public SummaryType getSummaryType() { return summaryType; }
    public void setSummaryType(SummaryType summaryType) { this.summaryType = summaryType; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Integer getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(Integer gradeLevel) { this.gradeLevel = gradeLevel; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
}
