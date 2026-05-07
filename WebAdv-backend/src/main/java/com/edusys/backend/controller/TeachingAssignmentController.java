package com.edusys.backend.controller;

import com.edusys.backend.dto.HomeworkCreateRequestDto;
import com.edusys.backend.dto.HomeworkResponseDto;
import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.dto.TeachingAssignmentAssignDTO;
import com.edusys.backend.dto.TeachingAssignmentClassDetailDTO;
import com.edusys.backend.dto.TeachingAssignmentDetailDTO;
import com.edusys.backend.dto.TeachingAssignmentGroupDTO;
import com.edusys.backend.dto.TeachingAssignmentListItemDTO;
import com.edusys.backend.dto.TeachingAssignmentListQueryDTO;
import com.edusys.backend.dto.TeachingAssignmentResponseDTO;
import com.edusys.backend.model.TeachingAssignment;
import com.edusys.backend.service.HomeworkService;
import com.edusys.backend.service.TeachingAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/teaching-assignments")
@Tag(name = "Teaching Assignments", description = "APIs for managing teaching assignments (teacher-subject-class relationships)")
@SecurityRequirement(name = "bearerAuth")
public class TeachingAssignmentController {

    private final TeachingAssignmentService service;
    private final HomeworkService homeworkService;

    public TeachingAssignmentController(TeachingAssignmentService service, HomeworkService homeworkService) {
        this.service = service;
        this.homeworkService = homeworkService;
    }

    @GetMapping
    @Operation(summary = "Get all teaching assignments", description = "Get all teaching assignments")
    public PaginatedResponseDTO<TeachingAssignmentListItemDTO> getAll(@Valid @ModelAttribute TeachingAssignmentListQueryDTO query) {
        return service.listAssignments(query);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get my teaching assignments", description = "Get all active teaching assignments for the authenticated teacher")
    public ResponseEntity<List<TeachingAssignmentDetailDTO>> getMine(Authentication authentication) {
        return ResponseEntity.ok(service.getMyTeachingAssignments(authentication));
    }

    @GetMapping("/{taId}")
    @Operation(summary = "Get teaching assignment group", description = "Treat taId as a group key and list all classes taught under the same teacher/subject/year/semester")
    public ResponseEntity<TeachingAssignmentGroupDTO> getGroup(
            @Parameter(description = "Teaching Assignment ID (group key)") @PathVariable Long taId
    ) {
        return ResponseEntity.ok(service.getGroup(taId));
    }

    @GetMapping("/{taId}/{classId}")
    @Operation(summary = "Get class details for teaching assignment", description = "Get details of a specific class under a teaching-assignment group")
    public ResponseEntity<TeachingAssignmentClassDetailDTO> getClassDetails(
            @Parameter(description = "Teaching Assignment ID (group key)") @PathVariable Long taId,
            @Parameter(description = "Class ID") @PathVariable Long classId
    ) {
        return ResponseEntity.ok(service.getClassDetails(taId, classId));
    }

    @PostMapping
    @Operation(summary = "Create teaching assignment", description = "Create a new teaching assignment")
    public TeachingAssignment create(@RequestBody TeachingAssignment assignment) {
        return service.save(assignment);
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign teacher to subject", description = "Admin assigns a teacher to teach a subject in a class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Teaching assignment created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<TeachingAssignmentResponseDTO> assign(@Valid @RequestBody TeachingAssignmentAssignDTO dto) {
        TeachingAssignment created = service.assignTeacherToSubject(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.toResponseDTO(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update teaching assignment", description = "Admin updates an existing teaching assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teaching assignment updated successfully"),
            @ApiResponse(responseCode = "404", description = "Teaching assignment not found")
    })
    public ResponseEntity<TeachingAssignmentResponseDTO> update(
            @Parameter(description = "Teaching Assignment ID") @PathVariable Long id,
            @Valid @RequestBody TeachingAssignmentAssignDTO dto
    ) {
        TeachingAssignment updated = service.updateTeachingAssignment(id, dto);
        return ResponseEntity.ok(service.toResponseDTO(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate teaching assignment", description = "Admin deactivates a teaching assignment (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Teaching assignment deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Teaching assignment not found")
    })
    public ResponseEntity<Void> delete(@Parameter(description = "Teaching Assignment ID") @PathVariable Long id) {
        service.deactivateTeachingAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taId}/{classId}/homework")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create homework for class", description = "Create a new homework assignment for the class under the given teaching-assignment group without needing teachingAssignmentId in the request body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Homework created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public HomeworkResponseDto createHomeworkForTeachingAssignmentClass(
            @Parameter(description = "Teaching Assignment ID (group key)") @PathVariable Long taId,
            @Parameter(description = "Class ID") @PathVariable Long classId,
            @RequestBody HomeworkCreateRequestDto request
    ) {
        TeachingAssignmentClassDetailDTO details = service.getClassDetails(taId, classId);
        return homeworkService.createHomework(request.toCreateDto(details.teachingAssignmentId()));
    }

    @PostMapping(value = "/{taId}/{classId}/homework", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create homework for class with attachments", description = "Create a new homework assignment for the class with optional image or PDF attachments")
    public HomeworkResponseDto createHomeworkForTeachingAssignmentClassMultipart(
            @Parameter(description = "Teaching Assignment ID (group key)") @PathVariable Long taId,
            @Parameter(description = "Class ID") @PathVariable Long classId,
            @RequestPart("payload") HomeworkCreateRequestDto request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        TeachingAssignmentClassDetailDTO details = service.getClassDetails(taId, classId);
        return homeworkService.createHomework(
                request.toCreateDto(details.teachingAssignmentId()),
                files == null ? List.of() : files
        );
    }
}
