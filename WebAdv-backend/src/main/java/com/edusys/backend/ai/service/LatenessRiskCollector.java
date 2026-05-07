package com.edusys.backend.ai.service;

import com.edusys.backend.ai.model.RiskIndicatorCode;
import com.edusys.backend.model.Attendance;
import com.edusys.backend.repository.AttendanceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class LatenessRiskCollector implements RiskIndicatorCollector {

    private final AttendanceRepository attendanceRepository;
    private final AiContextAssemblerService aiContextAssemblerService;

    public LatenessRiskCollector(AttendanceRepository attendanceRepository, AiContextAssemblerService aiContextAssemblerService) {
        this.attendanceRepository = attendanceRepository;
        this.aiContextAssemblerService = aiContextAssemblerService;
    }

    @Override
    public RiskIndicatorResult collect(RiskCalculationContext context) {
        AttendanceWindowMetrics metrics = attendanceRepository.summarizeAttendanceWindow(
                context.studentId(),
                context.attendanceWindowStart(),
                context.attendanceWindowEnd(),
                java.util.List.of(Attendance.Status.present, Attendance.Status.excused, Attendance.Status.sick, Attendance.Status.late),
                Attendance.Status.late
        );
        long total = metrics.totalRecords();
        if (total == 0) {
            return new RiskIndicatorResult(
                    RiskIndicatorCode.LATENESS,
                    null,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    0,
                    true,
                    aiContextAssemblerService.toJson(Map.of("lateCount", 0, "lateRate", 0))
            );
        }
        long lateCount = metrics.lateRecords();
        BigDecimal lateRate = BigDecimal.valueOf(lateCount * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
        BigDecimal normalized = BigDecimal.valueOf(Math.min(lateRate.doubleValue() * 2, 100)).setScale(2, RoundingMode.HALF_UP);
        return new RiskIndicatorResult(
                RiskIndicatorCode.LATENESS,
                lateRate,
                normalized,
                Math.toIntExact(total),
                false,
                aiContextAssemblerService.toJson(Map.of("lateCount", lateCount, "lateRate", lateRate, "totalRecords", total))
        );
    }
}
