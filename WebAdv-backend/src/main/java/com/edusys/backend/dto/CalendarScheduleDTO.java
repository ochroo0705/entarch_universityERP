package com.edusys.backend.dto;

import java.util.List;
import java.util.Map;

public record CalendarScheduleDTO(
        Map<Integer, List<CalendarSlotDTO>> scheduleByDay
) {}
