package com.edusys.backend.controller;

import com.edusys.backend.dto.ClassCreateDTO;
import com.edusys.backend.dto.ClassResponseDTO;
import com.edusys.backend.dto.AssistantTeacherAssignDTO;
import com.edusys.backend.dto.TeacherSummaryDTO;
import com.edusys.backend.model.Class;
import com.edusys.backend.service.ClassService;
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
@RequestMapping("/api/classes")
@Tag(name = "Class Management", description = "APIs for managing classes and assistant teachers")
@SecurityRequirement(name = "bearerAuth")
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    @Operation(summary = "Get all classes", description = "Retrieve list of all classes")
    public List<Class> getAll() {
        return classService.findAll();
    }

    @GetMapping("/my-teaching")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get my teaching classes", description = "Get classes taught by current teacher")
    public List<ClassResponseDTO> getMyTeachingClasses() {
        return classService.getClassesTaughtByCurrentTeacher();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create class", description = "Create a new class (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Class created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ClassResponseDTO> create(@Valid @RequestBody ClassCreateDTO dto) {
        ClassResponseDTO created = classService.createClassAsAdmin(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate class", description = "Deactivate a class by ID (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Class deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Class not found")
    })
    public ResponseEntity<Void> deactivateClass(
            @Parameter(description = "Class ID") @PathVariable Long id) {
        classService.deactivateClassAsAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/assistants")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get assistant teachers", description = "Get all assistant teachers for a class")
    public List<TeacherSummaryDTO> getAssistantTeachers(
            @Parameter(description = "Class ID") @PathVariable Long id) {
        return classService.getAssistantTeachersForClass(id);
    }

    @PostMapping("/{id}/assistants")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Add assistant teacher", description = "Add an assistant teacher to a class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Assistant teacher added"),
            @ApiResponse(responseCode = "404", description = "Class or teacher not found")
    })
    public ResponseEntity<List<TeacherSummaryDTO>> addAssistantTeacher(
            @Parameter(description = "Class ID") @PathVariable Long id,
            @Valid @RequestBody AssistantTeacherAssignDTO dto
    ) {
        List<TeacherSummaryDTO> updated = classService.addAssistantTeacher(id, dto.teacherId());
        return ResponseEntity.status(HttpStatus.CREATED).body(updated);
    }

    @DeleteMapping("/{id}/assistants/{teacherId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Remove assistant teacher", description = "Remove an assistant teacher from a class")
    public ResponseEntity<List<TeacherSummaryDTO>> removeAssistantTeacher(
            @Parameter(description = "Class ID") @PathVariable Long id,
            @Parameter(description = "Teacher ID") @PathVariable Long teacherId
    ) {
        List<TeacherSummaryDTO> updated = classService.removeAssistantTeacher(id, teacherId);
        return ResponseEntity.ok(updated);
    }
}
