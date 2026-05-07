package com.edusys.backend.university.dto;

import jakarta.validation.constraints.NotNull;

public record CoursePrerequisiteRequest(
        @NotNull Long subjectId,
        @NotNull Long prerequisiteSubjectId,
        String groupCode
) {}
