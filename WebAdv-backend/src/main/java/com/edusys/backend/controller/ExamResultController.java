package com.edusys.backend.controller;

import com.edusys.backend.dto.ExamResultPublishRequestDTO;
import com.edusys.backend.dto.ExamResultResponseDTO;
import com.edusys.backend.dto.ExamResultUpsertRequestDTO;
import com.edusys.backend.dto.ExamRosterItemDTO;
import com.edusys.backend.service.ExamResultService;
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
@RequestMapping("/api/grades/exams")
@Tag(name = "Exam Results", description = "APIs for managing exam scores and published exam results")
@SecurityRequirement(name = "bearerAuth")
public class ExamResultController {

    private final ExamResultService examResultService;

    public ExamResultController(ExamResultService examResultService) {
        this.examResultService = examResultService;
    }

    @PostMapping("/results")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Create or update an exam result", description = "Teacher or admin records an exam result for a student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exam result saved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ExamResultResponseDTO> upsertExamResult(@Valid @RequestBody ExamResultUpsertRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examResultService.upsertExamResult(request));
    }

    @PutMapping("/results/{examResultId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Update an exam result", description = "Teacher or admin updates an existing exam result")
    public ResponseEntity<ExamResultResponseDTO> updateExamResult(
            @Parameter(description = "Exam result ID") @PathVariable Long examResultId,
            @Valid @RequestBody ExamResultUpsertRequestDTO request
    ) {
        ExamResultResponseDTO existing = examResultService.getExamResult(examResultId);
        if (!existing.examScheduleId().equals(request.examScheduleId()) || !existing.studentId().equals(request.studentId())) {
            throw new IllegalArgumentException("Exam schedule ID and student ID must match the existing exam result");
        }
        return ResponseEntity.ok(examResultService.upsertExamResult(request));
    }

    @PostMapping("/results/{examResultId}/publish")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Publish or unpublish exam result", description = "Teacher or admin changes exam result visibility")
    public ResponseEntity<ExamResultResponseDTO> publishExamResult(
            @Parameter(description = "Exam result ID") @PathVariable Long examResultId,
            @Valid @RequestBody ExamResultPublishRequestDTO request
    ) {
        return ResponseEntity.ok(examResultService.updatePublishStatus(examResultId, request));
    }

    @GetMapping("/results/{examResultId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'TEACHER', 'ADMIN')")
    @Operation(summary = "Get exam result by ID", description = "Role-aware exam result detail lookup")
    public ResponseEntity<ExamResultResponseDTO> getExamResult(
            @Parameter(description = "Exam result ID") @PathVariable Long examResultId
    ) {
        return ResponseEntity.ok(examResultService.getExamResult(examResultId));
    }

    @GetMapping("/results")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all exam results", description = "Admin overview of all exam results")
    public ResponseEntity<List<ExamResultResponseDTO>> getAllExamResults() {
        return ResponseEntity.ok(examResultService.getAllExamResults());
    }

    @GetMapping("/exam-schedule/{examScheduleId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'TEACHER', 'ADMIN')")
    @Operation(summary = "Get exam results by exam schedule", description = "List result records for one exam schedule")
    public ResponseEntity<List<ExamResultResponseDTO>> getExamResultsByExamSchedule(
            @Parameter(description = "Exam schedule ID") @PathVariable Long examScheduleId
    ) {
        return ResponseEntity.ok(examResultService.getExamResultsByExamSchedule(examScheduleId));
    }

    @GetMapping("/exam-schedule/{examScheduleId}/roster")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Get exam schedule roster", description = "Teacher/admin roster view for one scheduled exam, including students without scores yet")
    public ResponseEntity<List<ExamRosterItemDTO>> getExamScheduleRoster(
            @Parameter(description = "Exam schedule ID") @PathVariable Long examScheduleId
    ) {
        return ResponseEntity.ok(examResultService.getExamScheduleRoster(examScheduleId));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("(hasAnyRole('STUDENT', 'PARENT') and @studentAccess.canAccessStudent(#studentId)) or hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Get exam results for a student", description = "Student, parent, teacher, or admin can view a student's exam results")
    public ResponseEntity<List<ExamResultResponseDTO>> getStudentExamResults(
            @Parameter(description = "Student ID") @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(examResultService.getStudentExamResults(studentId));
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Get exam results for a teacher", description = "Teacher or admin can view exam results owned by a teacher")
    public ResponseEntity<List<ExamResultResponseDTO>> getTeacherExamResults(
            @Parameter(description = "Teacher ID") @PathVariable Long teacherId
    ) {
        return ResponseEntity.ok(examResultService.getTeacherExamResults(teacherId));
    }

    @GetMapping("/teaching-assignment/{teachingAssignmentId}/roster")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Get exam roster by teaching assignment", description = "Teacher/admin roster view for exam result entry screens")
    public ResponseEntity<List<ExamRosterItemDTO>> getTeachingAssignmentExamRoster(
            @Parameter(description = "Teaching assignment ID") @PathVariable Long teachingAssignmentId
    ) {
        return ResponseEntity.ok(examResultService.getTeachingAssignmentExamRoster(teachingAssignmentId));
    }

    @GetMapping("/class/{classId}/roster")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Get exam roster by class", description = "Teacher/admin class-level exam roster")
    public ResponseEntity<List<ExamRosterItemDTO>> getClassExamRoster(
            @Parameter(description = "Class ID") @PathVariable Long classId
    ) {
        return ResponseEntity.ok(examResultService.getClassExamRoster(classId));
    }
}
