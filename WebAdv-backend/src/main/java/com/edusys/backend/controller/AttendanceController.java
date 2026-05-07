package com.edusys.backend.controller;

import com.edusys.backend.dto.*;
import com.edusys.backend.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance", description = "APIs for managing student attendance")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/mark")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
            summary = "Mark attendance for a student",
            description = "Teacher marks attendance for a single student"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attendance marked successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<AttendanceResponseDTO> markAttendance(
            @Valid @RequestBody AttendanceRequestDTO request) {

        AttendanceResponseDTO response =
                attendanceService.markAttendance(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/mark-bulk")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
            summary = "Mark attendance for multiple students",
            description = "Teacher marks attendance for multiple students at once"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bulk attendance marked successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<AttendanceResponseDTO>> markBulkAttendance(
            @Valid @RequestBody BulkAttendanceRequestDTO request) {

        List<AttendanceResponseDTO> response =
                attendanceService.markBulkAttendance(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'TEACHER', 'ADMIN') and @studentAccess.canAccessStudent(#studentId)")
    @Operation(
            summary = "Get student attendance records",
            description = "Get attendance records of a student within a date range"
    )
    public ResponseEntity<List<AttendanceResponseDTO>> getStudentAttendance(
            @Parameter(description = "Student ID") @PathVariable Long studentId,
            @Parameter(description = "Start date (ISO format)") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @Parameter(description = "End date (ISO format)") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return ResponseEntity.ok(
                attendanceService.getStudentAttendance(studentId, startDate, endDate)
        );
    }

    @GetMapping("/statistics/class/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
            summary = "Get class attendance statistics",
            description = "View attendance statistics for a class within a date range"
    )
    public ResponseEntity<ClassAttendanceSummaryDTO> getClassAttendanceStatistics(
            @Parameter(description = "Class ID") @PathVariable Long classId,
            @Parameter(description = "Start date (ISO format)") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @Parameter(description = "End date (ISO format)") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return ResponseEntity.ok(
                attendanceService.getClassAttendanceSummary(classId, startDate, endDate)
        );
    }

    @GetMapping("/class/{classId}/date")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
            summary = "Get class attendance for a specific date",
            description = "Retrieve all attendance records for a class on a given date"
    )
    public ResponseEntity<List<AttendanceResponseDTO>> getClassAttendanceByDate(
            @Parameter(description = "Class ID") @PathVariable Long classId,
            @Parameter(description = "Date (ISO format)") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        return ResponseEntity.ok(
                attendanceService.getClassAttendanceByDate(classId, date)
        );
    }

    @GetMapping("/class/{classId}/dates")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
            summary = "Get dates with attendance records",
            description = "Retrieve dates that have attendance records for a class in a date range"
    )
    public ResponseEntity<List<LocalDate>> getClassAttendanceDates(
            @Parameter(description = "Class ID") @PathVariable Long classId,
            @Parameter(description = "Start date (ISO format)") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO format)") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(
                attendanceService.getClassAttendanceDates(classId, startDate, endDate)
        );
    }

    @GetMapping("/parent-warning/{studentId}")
    @PreAuthorize("hasAnyRole('PARENT', 'TEACHER', 'ADMIN') and @studentAccess.canAccessStudent(#studentId)")
    @Operation(
            summary = "Get parent warning for student attendance",
            description = "Parent or teacher views attendance warning summary for a student"
    )
    public ResponseEntity<ParentWarningDTO> getParentWarning(
            @Parameter(description = "Student ID") @PathVariable Long studentId,
            @Parameter(description = "Start date (ISO format)") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @Parameter(description = "End date (ISO format)") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return ResponseEntity.ok(
                attendanceService.getParentWarning(studentId, startDate, endDate)
        );
    }
}
