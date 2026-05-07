package com.edusys.backend.controller;

import com.edusys.backend.dto.ChildDashboardDTO;
import com.edusys.backend.dto.ChildSummaryDTO;
import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.dto.ParentStudentLinkDTO;
import com.edusys.backend.dto.ParentStudentListItemDTO;
import com.edusys.backend.dto.ParentStudentListQueryDTO;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.ParentStudent;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.UserRepository;
import com.edusys.backend.service.ParentStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/parent-students")
@Tag(name = "Parent-Student Relationships", description = "APIs for managing parent-student relationships")
@SecurityRequirement(name = "bearerAuth")
public class ParentStudentController {

    private final ParentStudentService parentStudentService;
    private final UserRepository userRepository;

    public ParentStudentController(ParentStudentService parentStudentService, UserRepository userRepository) {
        this.parentStudentService = parentStudentService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all parent-student relationships", description = "Get all parent-student relationships (Admin only)")
    public PaginatedResponseDTO<ParentStudentListItemDTO> getAll(@Valid @ModelAttribute ParentStudentListQueryDTO query) {
        return parentStudentService.listLinks(query);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create parent-student relationship", description = "Create a new parent-student relationship (Admin only)")
    public ParentStudent create(@RequestBody ParentStudent ps) {
        return parentStudentService.save(ps);
    }

    @PostMapping("/link")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Link parent to student", description = "Link a parent to a student (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Link created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ParentStudent link(@Valid @RequestBody ParentStudentLinkDTO dto) {
        return parentStudentService.linkParentToStudent(dto);
    }

    @GetMapping("/me/children")
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "Get my children", description = "Parent gets list of all linked children")
    public List<ChildSummaryDTO> getMyChildren() {
        return parentStudentService.getChildrenForParent(getCurrentUser());
    }

    @GetMapping("/me/children/dashboard")
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "Get children dashboard", description = "Parent gets dashboard stats for all linked children")
    public List<ChildDashboardDTO> getMyChildrenDashboard() {
        return parentStudentService.getChildDashboardForParent(getCurrentUser());
    }

    @GetMapping("/me/children/{studentId}/profile")
    @PreAuthorize("hasRole('PARENT') and @studentAccess.canAccessStudent(#studentId)")
    @Operation(summary = "Get child profile", description = "Parent views full profile for a specific linked child")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public User getChildProfile(@Parameter(description = "Student ID") @PathVariable Long studentId) {
        return userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }
}
