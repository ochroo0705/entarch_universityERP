package com.edusys.backend.controller;

import com.edusys.backend.dto.SchoolStatsDTO;
import com.edusys.backend.repository.ClassRepository;
import com.edusys.backend.repository.TeachingAssignmentRepository;
import com.edusys.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Statistics", description = "APIs for admin dashboard statistics")
@SecurityRequirement(name = "bearerAuth")
public class AdminStatsController {

    private static final int ROLE_STUDENT = 1;
    private static final int ROLE_TEACHER = 2;

    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;

    public AdminStatsController(
            UserRepository userRepository,
            ClassRepository classRepository,
            TeachingAssignmentRepository teachingAssignmentRepository
    ) {
        this.userRepository = userRepository;
        this.classRepository = classRepository;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get school statistics", description = "Get overall school statistics including teacher, student, class, and teaching assignment counts (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<SchoolStatsDTO> getSchoolStats() {
        long teacherCount = userRepository.countByRoleFlagSet(ROLE_TEACHER);
        long studentCount = userRepository.countByRoleFlagSet(ROLE_STUDENT);
        long classCount = classRepository.count();
        long teachingAssignmentCount = teachingAssignmentRepository.count();

        return ResponseEntity.ok(new SchoolStatsDTO(
                teacherCount,
                studentCount,
                classCount,
                teachingAssignmentCount
        ));
    }
}
