package com.edusys.backend.controller;

import com.edusys.backend.dto.*;
import com.edusys.backend.service.ScheduleService;
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
@RequestMapping("/api/schedules")
@Tag(name = "Schedules", description = "APIs for managing class schedules and calendars")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create schedule", description = "Create a new schedule entry (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Schedule created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ScheduleResponseDTO> createSchedule(@Valid @RequestBody ScheduleCreateDTO dto) {
        ScheduleResponseDTO created = scheduleService.createSchedule(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all schedules", description = "Get all schedule entries (Admin only)")
    public ResponseEntity<PaginatedResponseDTO<ScheduleResponseDTO>> getAllSchedules(@Valid @ModelAttribute ScheduleListQueryDTO query) {
        return ResponseEntity.ok(scheduleService.listSchedules(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get schedule by ID", description = "Get schedule details by ID (Admin only)")
    public ResponseEntity<ScheduleResponseDTO> getScheduleById(
            @Parameter(description = "Schedule ID") @PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update schedule", description = "Update an existing schedule (Admin only)")
    public ResponseEntity<ScheduleResponseDTO> updateSchedule(
            @Parameter(description = "Schedule ID") @PathVariable Long id,
            @Valid @RequestBody ScheduleCreateDTO dto) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete schedule", description = "Delete a schedule entry (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Schedule deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Schedule not found")
    })
    public ResponseEntity<Void> deleteSchedule(
            @Parameter(description = "Schedule ID") @PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Auto-generate schedule",
            description = "Automatically compute a viable schedule from active teaching assignments and periods")
    public ResponseEntity<List<ScheduleResponseDTO>> generateSchedule(
            @RequestParam(defaultValue = "true") boolean clearExisting) {
        List<ScheduleResponseDTO> generated = scheduleService.generateSchedule(clearExisting);
        return ResponseEntity.status(HttpStatus.CREATED).body(generated);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'PARENT') and @studentAccess.canAccessStudent(#studentId)")
    @Operation(summary = "Get student schedule", description = "Get schedule for a specific student")
    public ResponseEntity<List<StudentScheduleDTO>> getStudentSchedule(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        return ResponseEntity.ok(scheduleService.getStudentSchedule(studentId));
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get teacher schedule", description = "Get schedule for a specific teacher")
    public ResponseEntity<List<TeacherScheduleDTO>> getTeacherSchedule(
            @Parameter(description = "Teacher ID") @PathVariable Long teacherId) {
        return ResponseEntity.ok(scheduleService.getTeacherSchedule(teacherId));
    }

    @GetMapping("/teacher/{teacherId}/classes")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get teacher classes", description = "Get all classes taught by a teacher")
    public ResponseEntity<List<TeacherClassDTO>> getTeacherClasses(
            @Parameter(description = "Teacher ID") @PathVariable Long teacherId) {
        return ResponseEntity.ok(scheduleService.getTeacherClasses(teacherId));
    }

    @GetMapping("/teacher/{teacherId}/calendar")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get teacher calendar", description = "Get calendar view for a teacher")
    public ResponseEntity<CalendarScheduleDTO> getTeacherCalendar(
            @Parameter(description = "Teacher ID") @PathVariable Long teacherId) {
        return ResponseEntity.ok(scheduleService.getTeacherCalendar(teacherId));
    }

    @GetMapping("/student/{studentId}/calendar")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'PARENT') and @studentAccess.canAccessStudent(#studentId)")
    @Operation(summary = "Get student calendar", description = "Get calendar view for a student")
    public ResponseEntity<CalendarScheduleDTO> getStudentCalendar(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        return ResponseEntity.ok(scheduleService.getStudentCalendar(studentId));
    }
}
