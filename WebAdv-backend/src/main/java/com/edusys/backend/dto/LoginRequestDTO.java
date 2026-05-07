package com.edusys.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request with username and password")
public record LoginRequestDTO(
        @Schema(description = "Username", example = "john_doe")
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "Password", example = "password123")
        @NotBlank(message = "Password is required")
        String password
) {
}


