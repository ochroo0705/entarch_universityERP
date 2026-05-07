package com.edusys.backend.controller;

import com.edusys.backend.dto.ExamScheduleCreateDTO;
import com.edusys.backend.dto.ExamScheduleListQueryDTO;
import com.edusys.backend.dto.ExamScheduleResponseDTO;
import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.service.ExamScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-schedules")
@Tag(name = "Exam Schedules", description = "APIs for managing structured exam schedules")
@SecurityRequirement(name = "bearerAuth")
public class ExamScheduleController {

    private final ExamScheduleService examScheduleService;

    public ExamScheduleController(ExamScheduleService examScheduleService) {
        this.examScheduleService = examScheduleService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create exam schedule", description = "Create a new exam schedule entry (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exam schedule created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ExamScheduleResponseDTO> createExamSchedule(@Valid @RequestBody ExamScheduleCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examScheduleService.createExamSchedule(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all exam schedules", description = "Get all exam schedule entries (Admin only)")
    public ResponseEntity<PaginatedResponseDTO<ExamScheduleResponseDTO>> getAllExamSchedules(@Valid @ModelAttribute ExamScheduleListQueryDTO query) {
        return ResponseEntity.ok(examScheduleService.listExamSchedules(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get exam schedule by ID", description = "Get exam schedule details by ID (Admin only)")
    public ResponseEntity<ExamScheduleResponseDTO> getExamScheduleById(@Parameter(description = "Exam schedule ID") @PathVariable Long id) {
        return ResponseEntity.ok(examScheduleService.getExamScheduleById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update exam schedule", description = "Update an exam schedule entry (Admin only)")
    public ResponseEntity<ExamScheduleResponseDTO> updateExamSchedule(
            @Parameter(description = "Exam schedule ID") @PathVariable Long id,
            @Valid @RequestBody ExamScheduleCreateDTO dto
    ) {
        return ResponseEntity.ok(examScheduleService.updateExamSchedule(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete exam schedule", description = "Soft delete an exam schedule entry (Admin only)")
    public ResponseEntity<Void> deleteExamSchedule(@Parameter(description = "Exam schedule ID") @PathVariable Long id) {
        examScheduleService.deleteExamSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get teacher exam schedules", description = "Get published exam schedules for a teacher")
    public ResponseEntity<List<ExamScheduleResponseDTO>> getTeacherExamSchedules(
            @Parameter(description = "Teacher ID") @PathVariable Long teacherId
    ) {
        return ResponseEntity.ok(examScheduleService.getTeacherExamSchedules(teacherId));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'PARENT') and @studentAccess.canAccessStudent(#studentId)")
    @Operation(summary = "Get student exam schedules", description = "Get published exam schedules for a student")
    public ResponseEntity<List<ExamScheduleResponseDTO>> getStudentExamSchedules(
            @Parameter(description = "Student ID") @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(examScheduleService.getStudentExamSchedules(studentId));
    }
}
