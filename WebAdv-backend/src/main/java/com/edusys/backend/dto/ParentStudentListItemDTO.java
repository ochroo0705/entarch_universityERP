package com.edusys.backend.dto;

public record ParentStudentListItemDTO(
        Long id,
        UserSummaryDTO parent,
        UserSummaryDTO student,
        String relationship,
        Boolean isPrimaryContact
) {
    public record UserSummaryDTO(
            Long id,
            String username,
            String firstName,
            String lastName
    ) {
    }
}
