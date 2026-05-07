package com.edusys.backend.dto;

import java.time.LocalDateTime;

public record UserListItemDTO(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phone,
        Integer roleFlags,
        Boolean isActive,
        LocalDateTime createdAt,
        Integer grade,
        String section,
        String className
) {
}
