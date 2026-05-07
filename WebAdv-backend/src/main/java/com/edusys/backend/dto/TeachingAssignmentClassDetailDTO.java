package com.edusys.backend.dto;

import java.util.List;

/**
 * Details for a single class within a teaching-assignment group.
 */
public record TeachingAssignmentClassDetailDTO(
        Long groupId,
        Long teachingAssignmentId,
        TeachingAssignmentDetailDTO.TeacherDTO teacher,
        TeachingAssignmentDetailDTO.SubjectDTO subject,
        TeachingAssignmentDetailDTO.ClassInfoDTO classInfo,
        String academicYear,
        Integer semester,
        Boolean isActive
) {
}
