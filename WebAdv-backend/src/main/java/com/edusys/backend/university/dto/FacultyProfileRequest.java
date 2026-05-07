package com.edusys.backend.university.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record FacultyProfileRequest(
        @NotNull Long facultyUserId,
        @Size(max = 60) String employeeNumber,
        @NotBlank @Size(max = 120) String department,
        @Size(max = 120) String academicRank,
        @Size(max = 40) String employmentStatus,
        LocalDate hireDate,
        @Size(max = 120) String officeLocation,
        @NotNull @Min(0) @Max(40) Integer workloadTargetCredits
) {}
