package com.edusys.backend.ai.dto;

import java.util.List;

public record AiStudentOptionResponse(
        Long studentId,
        String studentName,
        List<AiParentOptionResponse> parents
) {
    public record AiParentOptionResponse(
            Long parentUserId,
            String parentName
    ) {
    }
}
