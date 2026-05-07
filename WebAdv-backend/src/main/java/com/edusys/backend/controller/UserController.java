package com.edusys.backend.controller;

import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.dto.RoleAssignmentRequestDTO;
import com.edusys.backend.dto.RoleOptionDTO;
import com.edusys.backend.dto.TeacherListItemDTO;
import com.edusys.backend.dto.UserListItemDTO;
import com.edusys.backend.dto.UserListQueryDTO;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.UserRepository;
import com.edusys.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users (teachers, students, parents)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/teacher")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a teacher", description = "Create a new teacher user (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<User> createTeacher(@RequestBody User user) {
        User created = userService.createTeacher(user);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/student")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a student", description = "Create a new student user (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<User> createStudent(@RequestBody User user) {
        User created = userService.createStudent(user);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/parent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a parent", description = "Create a new parent user (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parent created successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<User> createParent(@RequestBody User user) {
        User created = userService.createParent(user);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('FINANCE_STAFF','CAFETERIA_STAFF') and #query.role == 1)")
    @Operation(summary = "List users", description = "Get a paginated list of users (Admin only)")
    public ResponseEntity<PaginatedResponseDTO<UserListItemDTO>> listAllUsers(@Valid @ModelAttribute UserListQueryDTO query) {
        return ResponseEntity.ok(userService.listUsers(query));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List assignable roles", description = "Get the role flag catalog used for staff permissions")
    public ResponseEntity<List<RoleOptionDTO>> listRoles() {
        return ResponseEntity.ok(userService.getRoleOptions());
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign user roles", description = "Replace a user's role flags (Admin only)")
    public ResponseEntity<User> updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody RoleAssignmentRequestDTO request) {
        return ResponseEntity.ok(userService.updateUserRoles(id, request));
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List teachers", description = "Get list of teacher IDs and names (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teachers fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<TeacherListItemDTO>> listTeachers() {
        List<TeacherListItemDTO> teachers = userRepository.findIdAndNameByRoleFlagSet(User.ROLE_TEACHER)
                .stream()
                .map(v -> new TeacherListItemDTO(v.getId(), v.getFirstName(), v.getLastName()))
                .toList();
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<User> getUser(@Parameter(description = "User ID") @PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
