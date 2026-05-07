package com.edusys.backend.dto;

import java.time.LocalDate;

public record DateRange(
        LocalDate startDate,
        LocalDate endDate
) {}

