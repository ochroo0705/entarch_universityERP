package com.edusys.backend.ai.service;

import com.edusys.backend.ai.model.RiskIndicatorCode;
import com.edusys.backend.model.Attendance;
import com.edusys.backend.repository.AttendanceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceRiskCollector implements RiskIndicatorCollector {

    private final AttendanceRepository attendanceRepository;
    private final AiContextAssemblerService aiContextAssemblerService;

    public AttendanceRiskCollector(AttendanceRepository attendanceRepository, AiContextAssemblerService aiContextAssemblerService) {
        this.attendanceRepository = attendanceRepository;
        this.aiContextAssemblerService = aiContextAssemblerService;
    }

    @Override
    public RiskIndicatorResult collect(RiskCalculationContext context) {
        AttendanceWindowMetrics metrics = attendanceRepository.summarizeAttendanceWindow(
                context.studentId(),
                context.attendanceWindowStart(),
                context.attendanceWindowEnd(),
                List.of(Attendance.Status.present, Attendance.Status.excused, Attendance.Status.sick, Attendance.Status.late),
                Attendance.Status.late
        );
        long total = metrics.totalRecords();
        if (total == 0) {
            return new RiskIndicatorResult(
                    RiskIndicatorCode.ATTENDANCE,
                    null,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    0,
                    true,
                    aiContextAssemblerService.toJson(Map.of("totalRecords", 0, "windowType", "attendance"))
            );
        }

        long attended = metrics.attendedRecords();
        BigDecimal attendanceRate = BigDecimal.valueOf(attended * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
        BigDecimal normalized = BigDecimal.valueOf(Math.max(0, 100 - attendanceRate.doubleValue())).setScale(2, RoundingMode.HALF_UP);
        return new RiskIndicatorResult(
                RiskIndicatorCode.ATTENDANCE,
                attendanceRate,
                normalized,
                Math.toIntExact(total),
                false,
                aiContextAssemblerService.toJson(Map.of("attendanceRate", attendanceRate, "totalRecords", total, "attendedRecords", attended))
        );
    }
}
