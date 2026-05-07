package com.edusys.backend.university.dto;

import jakarta.validation.constraints.NotNull;

public record CourseCorequisiteRequest(
        @NotNull Long subjectId,
        @NotNull Long corequisiteSubjectId
) {}
