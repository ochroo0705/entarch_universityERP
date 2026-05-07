package com.edusys.backend.university.dto;

import com.edusys.backend.university.model.ApplicantStatus;

import java.time.LocalDateTime;

public record ApplicantResponse(
        Long id,
        String applicationNumber,
        String firstName,
        String lastName,
        String email,
        String phone,
        String program,
        ApplicantStatus status,
        String decisionNotes,
        Long convertedStudentId,
        String convertedStudentName,
        LocalDateTime submittedAt,
        LocalDateTime updatedAt
) {}
