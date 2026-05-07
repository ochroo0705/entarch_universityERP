package com.edusys.backend.dto;

import jakarta.validation.constraints.NotNull;

public record RoleAssignmentRequestDTO(
        @NotNull Integer roleFlags
) {}
