package com.edusys.backend.university.dto;

import java.time.LocalDateTime;

public record ServiceRequestCommentResponse(
        Long id,
        Long authorId,
        String authorName,
        String commentText,
        Boolean internal,
        LocalDateTime createdAt
) {}
