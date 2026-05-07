package com.edusys.backend.university.dto;

import java.util.List;

public record CourseOptionResponse(
        Long id,
        String courseName,
        String courseCode,
        Integer academicLevel,
        Integer credits,
        Boolean mandatory,
        List<String> prerequisites
) {}
