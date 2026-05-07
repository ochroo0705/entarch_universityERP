package com.edusys.backend.controller;

import com.edusys.backend.dto.StudentEnrollmentAssignDTO;
import com.edusys.backend.dto.StudentEnrollmentListItemDTO;
import com.edusys.backend.dto.StudentEnrollmentListQueryDTO;
import com.edusys.backend.dto.StudentEnrollmentResponseDTO;
import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.model.StudentEnrollment;
import com.edusys.backend.service.StudentEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/student-enrollments")
@Tag(name = "Student Enrollments", description = "APIs for managing student enrollments in classes")
@SecurityRequirement(name = "bearerAuth")
public class StudentEnrollmentController {

    private final StudentEnrollmentService service;

    public StudentEnrollmentController(StudentEnrollmentService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all enrollments", description = "Get all student enrollments")
    public PaginatedResponseDTO<StudentEnrollmentListItemDTO> getAll(@Valid @ModelAttribute StudentEnrollmentListQueryDTO query) {
        return service.listEnrollments(query);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create enrollment", description = "Create a new student enrollment")
    public ResponseEntity<StudentEnrollmentResponseDTO> create(@Valid @RequestBody StudentEnrollmentAssignDTO dto) {
        StudentEnrollmentResponseDTO created = service.createEnrollmentAsAdmin(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/teacher/enroll")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Enroll student", description = "Teacher enrolls a student in their class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Student enrolled successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<StudentEnrollmentResponseDTO> enrollStudentAsTeacher(
            @Valid @RequestBody StudentEnrollmentAssignDTO dto
    ) {
        StudentEnrollmentResponseDTO created = service.enrollStudentAsTeacher(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/teacher/unenroll")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Unenroll student", description = "Teacher unenrolls a student from their class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Student unenrolled successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> unenrollStudentAsTeacher(
            @Valid @RequestBody StudentEnrollmentAssignDTO dto
    ) {
        service.unenrollStudentAsTeacher(dto);
        return ResponseEntity.noContent().build();
    }
}
