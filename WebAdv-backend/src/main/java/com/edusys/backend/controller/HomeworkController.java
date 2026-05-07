package com.edusys.backend.controller;

import com.edusys.backend.dto.HomeworkCreateDto;
import com.edusys.backend.dto.HomeworkResponseDto;
import com.edusys.backend.model.Homework;
import com.edusys.backend.model.HomeworkAttachment;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.UserRepository;
import com.edusys.backend.service.FileStorageService;
import com.edusys.backend.service.HomeworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/homework")
@Tag(name = "Homework", description = "APIs for managing homework assignments")
@SecurityRequirement(name = "bearerAuth")
public class HomeworkController {
    private final UserRepository userRepository;
    private final HomeworkService service;
    private final FileStorageService fileStorageService;

    public HomeworkController(UserRepository userRepository, HomeworkService service, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.service = service;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    @Operation(summary = "Get all homework for current user", description = "Get all homework assignments for the current authenticated user")
    public List<HomeworkResponseDto> getAllForCurrentUser() {
        return service.getHomeworkForCurrentUser(getCurrentUser());
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @PostMapping
    @Operation(summary = "Create homework", description = "Create a new homework assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Homework created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public HomeworkResponseDto createHomework(@RequestBody HomeworkCreateDto request) {
        return service.createHomework(request);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create homework with attachments", description = "Create homework with optional image and PDF attachments")
    public HomeworkResponseDto createHomeworkMultipart(
            @RequestPart("payload") HomeworkCreateDto request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return service.createHomework(request, files == null ? List.of() : files);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get homework by ID", description = "Retrieve homework assignment details by ID")
    public HomeworkResponseDto getHomeworkById(@Parameter(description = "Homework ID") @PathVariable Long id) {
        return service.getHomeworkById(id);
    }

    @GetMapping("/teaching-assignment/{teachingAssignmentId}")
    @Operation(summary = "Get homework by teaching assignment", description = "Get all homework for a specific teaching assignment")
    public List<HomeworkResponseDto> getHomeworkByTeachingAssignment(
            @Parameter(description = "Teaching Assignment ID") @PathVariable Long teachingAssignmentId
    ) {
        return service.getHomeworkByTeachingAssignment(teachingAssignmentId, getCurrentUser());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update homework", description = "Update an existing homework assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Homework updated successfully"),
            @ApiResponse(responseCode = "404", description = "Homework not found")
    })
    public HomeworkResponseDto updateHomework(
            @Parameter(description = "Homework ID") @PathVariable Long id,
            @RequestBody HomeworkCreateDto request
    ) {
        return service.updateHomework(id, request, getCurrentUser());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update homework with attachments", description = "Update homework and manage attachments")
    public HomeworkResponseDto updateHomeworkMultipart(
            @Parameter(description = "Homework ID") @PathVariable Long id,
            @RequestPart("payload") HomeworkCreateDto request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "removeAttachmentIds", required = false) List<Long> removeAttachmentIds
    ) {
        return service.updateHomework(
                id,
                request,
                files == null ? List.of() : files,
                removeAttachmentIds == null ? List.of() : removeAttachmentIds,
                getCurrentUser()
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete homework", description = "Delete a homework assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Homework deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Homework not found")
    })
    public void deleteHomework(@Parameter(description = "Homework ID") @PathVariable Long id) {
        service.deleteHomework(id, getCurrentUser());
    }

    @GetMapping("/student")
    @Operation(summary = "Get homework for current student", description = "Get all homework assignments for the current student user")
    public List<HomeworkResponseDto> getHomeworkForStudent() {
        return service.getHomeworkForCurrentStudent(getCurrentUser());
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT') and @studentAccess.canAccessStudent(#studentId)")
    @Operation(summary = "Get homework for specific student", description = "Parent, teacher, or admin can view homework for a specific student")
    public List<HomeworkResponseDto> getHomeworkForStudentId(@Parameter(description = "Student ID") @PathVariable Long studentId) {
        return service.getHomeworkForStudentId(studentId);
    }

    @GetMapping("/{homeworkId}/attachments/{attachmentId}/download")
    @Operation(summary = "Download homework attachment", description = "Download a homework attachment if the current user can access the homework")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long homeworkId, @PathVariable Long attachmentId) {
        HomeworkAttachment attachment = service.getAttachmentForHomework(homeworkId, attachmentId, getCurrentUser());
        return buildAttachmentResponse(attachment.getStoredPath(), attachment.getMimeType(), attachment.getOriginalFilename(), false);
    }

    @GetMapping("/{homeworkId}/attachments/{attachmentId}/preview")
    @Operation(summary = "Preview homework attachment", description = "Preview a homework attachment inline if the current user can access the homework")
    public ResponseEntity<Resource> previewAttachment(@PathVariable Long homeworkId, @PathVariable Long attachmentId) {
        HomeworkAttachment attachment = service.getAttachmentForHomework(homeworkId, attachmentId, getCurrentUser());
        return buildAttachmentResponse(attachment.getStoredPath(), attachment.getMimeType(), attachment.getOriginalFilename(), true);
    }

    @GetMapping("/{homeworkId}/legacy-attachment/download")
    @Operation(summary = "Download legacy homework attachment", description = "Download a legacy single-url homework attachment through homework authorization")
    public ResponseEntity<Resource> downloadLegacyAttachment(@PathVariable Long homeworkId) {
        Homework homework = service.getAccessibleHomework(homeworkId, getCurrentUser());
        return buildLegacyAttachmentResponse(homework, false);
    }

    @GetMapping("/{homeworkId}/legacy-attachment/preview")
    @Operation(summary = "Preview legacy homework attachment", description = "Preview a legacy single-url homework attachment through homework authorization")
    public ResponseEntity<Resource> previewLegacyAttachment(@PathVariable Long homeworkId) {
        Homework homework = service.getAccessibleHomework(homeworkId, getCurrentUser());
        return buildLegacyAttachmentResponse(homework, true);
    }

    private ResponseEntity<Resource> buildLegacyAttachmentResponse(Homework homework, boolean inline) {
        String attachmentUrl = homework.getAttachmentUrl();
        if (attachmentUrl == null || attachmentUrl.isBlank()) {
            throw new IllegalArgumentException("Legacy attachment not found");
        }

        String storedPath = attachmentUrl.replace("/api/files/download/", "");
        String filename = storedPath.substring(storedPath.lastIndexOf('/') + 1);
        String lower = filename.toLowerCase();
        String mimeType = lower.endsWith(".pdf") ? "application/pdf"
                : lower.endsWith(".png") ? "image/png"
                : (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) ? "image/jpeg"
                : "application/octet-stream";
        return buildAttachmentResponse(storedPath, mimeType, filename, inline);
    }

    private ResponseEntity<Resource> buildAttachmentResponse(String storedPath, String mimeType, String filename, boolean inline) {
        Resource resource = fileStorageService.loadAsResource(storedPath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, (inline ? "inline" : "attachment") + "; filename=\"" + filename + "\"")
                .body(resource);
    }
}
