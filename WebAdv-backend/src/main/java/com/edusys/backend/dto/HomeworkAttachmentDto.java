package com.edusys.backend.dto;

import java.time.LocalDateTime;

public record HomeworkAttachmentDto(
        Long id,
        String originalFilename,
        String mimeType,
        Long size,
        LocalDateTime uploadedAt,
        String downloadUrl,
        String previewUrl,
        boolean previewable,
        String kind
) {}
