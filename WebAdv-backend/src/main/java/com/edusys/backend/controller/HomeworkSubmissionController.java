package com.edusys.backend.controller;

import com.edusys.backend.dto.HomeworkSubmissionCreateDto;
import com.edusys.backend.dto.HomeworkSubmissionResponseDto;
import com.edusys.backend.model.HomeworkSubmission;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.UserRepository;
import com.edusys.backend.service.FileStorageService;
import com.edusys.backend.service.HomeworkSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/homework-submissions")
@Tag(name = "Homework Submissions", description = "APIs for managing homework submissions")
@SecurityRequirement(name = "bearerAuth")
public class HomeworkSubmissionController {

    private final HomeworkSubmissionService service;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public HomeworkSubmissionController(HomeworkSubmissionService service, UserRepository userRepository, FileStorageService fileStorageService) {
        this.service = service;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all homework submissions", description = "Get all homework submissions (Admin only)")
    public List<HomeworkSubmission> getAll() {
        return service.findAll();
    }

    @PostMapping(value = "/homework/{homeworkId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Submit homework", description = "Student submits homework with optional file attachment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Homework submitted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public HomeworkSubmissionResponseDto submitHomework(
            @Parameter(description = "Homework ID") @PathVariable Long homeworkId,
            @RequestParam(value = "submissionText", required = false) String submissionText,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        String attachmentUrl = null;
        if (file != null && !file.isEmpty()) {
            String filePath = fileStorageService.store(file, "submissions");
            attachmentUrl = "/api/files/download/" + filePath;
        }
        HomeworkSubmissionCreateDto dto = new HomeworkSubmissionCreateDto(submissionText, attachmentUrl);
        return service.submitHomework(homeworkId, dto, getCurrentUser());
    }

    @PostMapping(value = "/homework/{homeworkId}/student/{studentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PARENT') and @studentAccess.canAccessStudent(#studentId)")
    @Operation(summary = "Submit homework for child", description = "Parent submits homework on behalf of their child")
    public HomeworkSubmissionResponseDto submitHomeworkForChild(
            @Parameter(description = "Homework ID") @PathVariable Long homeworkId,
            @Parameter(description = "Student ID") @PathVariable Long studentId,
            @RequestParam(value = "submissionText", required = false) String submissionText,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        String attachmentUrl = null;
        if (file != null && !file.isEmpty()) {
            String filePath = fileStorageService.store(file, "submissions");
            attachmentUrl = "/api/files/download/" + filePath;
        }
        HomeworkSubmissionCreateDto dto = new HomeworkSubmissionCreateDto(submissionText, attachmentUrl);
        return service.submitHomeworkForStudentId(homeworkId, dto, studentId, getCurrentUser());
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @GetMapping("/homework/{homeworkId}/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get my submission", description = "Student gets their own submission for a homework")
    public HomeworkSubmissionResponseDto getMySubmission(
            @Parameter(description = "Homework ID") @PathVariable Long homeworkId) {
        return service.getMySubmission(homeworkId, getCurrentUser());
    }

    @GetMapping("/homework/{homeworkId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get submissions for homework", description = "Teacher gets all submissions for a homework assignment")
    public List<HomeworkSubmissionResponseDto> getSubmissionsForHomework(
            @Parameter(description = "Homework ID") @PathVariable Long homeworkId
    ) {
        return service.getSubmissionsForHomework(homeworkId, getCurrentUser());
    }

    @GetMapping("/homework/{homeworkId}/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT') and @studentAccess.canAccessStudent(#studentId)")
    @Operation(summary = "Get submission for student", description = "Get a specific student's submission for a homework")
    public HomeworkSubmissionResponseDto getSubmissionForHomeworkAndStudent(
            @Parameter(description = "Homework ID") @PathVariable Long homeworkId,
            @Parameter(description = "Student ID") @PathVariable Long studentId
    ) {
        return service.getSubmissionForHomeworkAndStudent(homeworkId, studentId, getCurrentUser());
    }

    @GetMapping("/{submissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get submission by ID", description = "Get homework submission details by ID")
    public HomeworkSubmissionResponseDto getSubmissionById(
            @Parameter(description = "Submission ID") @PathVariable Long submissionId
    ) {
        return service.getSubmissionById(submissionId, getCurrentUser());
    }

    @GetMapping("/class/{classId}/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT') and @studentAccess.canAccessStudent(#studentId)")
    @Operation(summary = "Get student submissions in class", description = "Get all homework submissions for a student in a specific class")
    public List<HomeworkSubmissionResponseDto> getSubmissionsForStudentInClass(
            @Parameter(description = "Class ID") @PathVariable Long classId,
            @Parameter(description = "Student ID") @PathVariable Long studentId
    ) {
        return service.getSubmissionsForStudentInClass(classId, studentId, getCurrentUser());
    }



}
