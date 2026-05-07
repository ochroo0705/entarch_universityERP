package com.edusys.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request to validate OTP")
public record ValidateOtpRequestDTO(
        @Schema(description = "Email or phone number used to receive OTP", example = "user@example.com")
        @NotBlank(message = "Identifier (email or phone) is required")
        String identifier,

        @Schema(description = "6-digit OTP code", example = "123456")
        @NotBlank(message = "OTP is required")
        @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
        String otp
) {
}

