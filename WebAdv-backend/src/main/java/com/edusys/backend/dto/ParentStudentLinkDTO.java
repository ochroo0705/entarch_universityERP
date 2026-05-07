package com.edusys.backend.dto;

import com.edusys.backend.model.ParentStudent;
import jakarta.validation.constraints.NotNull;

public record ParentStudentLinkDTO(
        @NotNull Long parentId,
        @NotNull Long studentId,
        ParentStudent.Relationship relationship,
        Boolean isPrimaryContact
) {}
