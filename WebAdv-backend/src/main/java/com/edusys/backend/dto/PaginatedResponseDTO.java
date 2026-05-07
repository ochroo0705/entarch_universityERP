package com.edusys.backend.dto;

import java.util.List;

public record PaginatedResponseDTO<T>(
        List<T> items,
        int page,
        int pageSize,
        long total,
        int totalPages
) {
}
