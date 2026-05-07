package com.edusys.backend.model;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "periods")
public class Period {

    public enum PeriodType {
        LESSON("lesson"), BREAK("break"), LUNCH("lunch");

        private final String value;
        PeriodType(String value) { this.value = value; }
        public String getValue() { return value; }

        public static PeriodType fromValue(String v) {
            for (PeriodType pt : values()) {
                if (pt.value.equals(v)) return pt;
            }
            throw new IllegalArgumentException("Unknown PeriodType: " + v);
        }
    }

    @jakarta.persistence.Converter(autoApply = true)
    public static class PeriodTypeConverter implements jakarta.persistence.AttributeConverter<PeriodType, String> {
        @Override
        public String convertToDatabaseColumn(PeriodType attr) {
            return attr == null ? null : attr.getValue();
        }
        @Override
        public PeriodType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : PeriodType.fromValue(dbData);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_number", unique = true)
    private Integer periodNumber;

    private LocalTime startTime;
    private LocalTime endTime;

    @Convert(converter = PeriodTypeConverter.class)
    private PeriodType periodType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(Integer periodNumber) { this.periodNumber = periodNumber; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public PeriodType getPeriodType() { return periodType; }
    public void setPeriodType(PeriodType periodType) { this.periodType = periodType; }

}
