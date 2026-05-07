package com.edusys.backend.university.dto;

import java.time.LocalDateTime;

public record ServiceRequestAttachmentResponse(
        Long id,
        Long uploadedById,
        String uploadedByName,
        String originalFilename,
        String storedPath,
        String downloadUrl,
        String mimeType,
        Long sizeBytes,
        LocalDateTime uploadedAt
) {}
