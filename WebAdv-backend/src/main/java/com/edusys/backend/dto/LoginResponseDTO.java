package com.edusys.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record LoginResponseDTO(
        Long userId,
        String username,
        String email,
        String firstName,
        String lastName,
        String role,
        List<String> roles,
        String token
) {
}

