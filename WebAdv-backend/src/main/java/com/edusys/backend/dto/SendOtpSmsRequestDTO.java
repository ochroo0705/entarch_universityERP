package com.edusys.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request to send OTP via SMS")
public record SendOtpSmsRequestDTO(
        @Schema(description = "Phone number (8 digits for Mongolia or E.164 format)", example = "99001122")
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^(\\d{8}|\\+?[1-9]\\d{1,14})$", message = "Invalid phone number. Use 8 digits (e.g., 99001122) or E.164 format (e.g., +976XXXXXXXX)")
        String phoneNumber
) {
}

