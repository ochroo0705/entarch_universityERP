package com.edusys.backend.controller;

import com.edusys.backend.dto.*;
import com.edusys.backend.service.GradeService;
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
@RequestMapping("/api/grades")
@Tag(name = "Grades", description = "APIs for managing student grades")
@SecurityRequirement(name = "bearerAuth")
public class GradesController {

    private final GradeService gradeService;

    public GradesController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Assign grade to student", description = "Teacher assigns a grade to a student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Grade assigned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<GradeResponseDTO> assignGrade(
            @Valid @RequestBody GradeRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gradeService.assignGrade(request));
    }

    @PutMapping("/{gradeId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update grade", description = "Teacher updates an existing grade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grade updated successfully"),
            @ApiResponse(responseCode = "404", description = "Grade not found")
    })
    public ResponseEntity<GradeResponseDTO> updateGrade(
            @Parameter(description = "Grade ID") @PathVariable Long gradeId,
            @Valid @RequestBody GradeRequestDTO request) {

        return ResponseEntity.ok(
                gradeService.updateGrade(gradeId, request)
        );
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'TEACHER', 'ADMIN') and @studentAccess.canAccessStudent(#studentId)")
    @Operation(summary = "Get student grades", description = "Get all grades of a student, optionally filtered by quarter")
    public ResponseEntity<List<GradeResponseDTO>> getStudentGrades(
            @Parameter(description = "Student ID") @PathVariable Long studentId,
            @Parameter(description = "Quarter number (optional)") @RequestParam(required = false) Integer quarter) {

        return ResponseEntity.ok(
                gradeService.getStudentGrades(studentId, quarter)
        );
    }

    @GetMapping("/student/{studentId}/gpa")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'TEACHER', 'ADMIN') and @studentAccess.canAccessStudent(#studentId)")
    @Operation(summary = "Get student GPA", description = "View student's GPA for a specific quarter")
    public ResponseEntity<StudentGPADTO> getStudentGPA(
            @Parameter(description = "Student ID") @PathVariable Long studentId,
            @Parameter(description = "Quarter number") @RequestParam Integer quarter) {

        return ResponseEntity.ok(
                gradeService.calculateStudentGPA(studentId, quarter)
        );
    }

    @GetMapping("/student/{studentId}/trends")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'TEACHER', 'ADMIN') and @studentAccess.canAccessStudent(#studentId)")
    @Operation(summary = "Get grade trends", description = "View student's grade trends across quarters")
    public ResponseEntity<GradeTrendsDTO> getGradeTrends(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {

        return ResponseEntity.ok(
                gradeService.getGradeTrends(studentId)
        );
    }
}
