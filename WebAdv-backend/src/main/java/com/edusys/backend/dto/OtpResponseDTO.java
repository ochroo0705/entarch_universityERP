package com.edusys.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record OtpResponseDTO(
        boolean success,
        String message,
        String recipient
) {
}

