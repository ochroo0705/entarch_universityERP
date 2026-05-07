package com.edusys.backend.controller;

import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.dto.SubjectListQueryDTO;
import com.edusys.backend.model.Subject;
import com.edusys.backend.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/subjects")
@Tag(name = "Subjects", description = "APIs for managing subjects")
@SecurityRequirement(name = "bearerAuth")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    @Operation(summary = "Get all subjects", description = "Get list of all subjects")
    public PaginatedResponseDTO<Subject> getAll(@Valid @ModelAttribute SubjectListQueryDTO query) {
        return subjectService.listSubjects(query);
    }

    @PostMapping
    @Operation(summary = "Create subject", description = "Create a new subject")
    public Subject create(@RequestBody Subject subject) {
        return subjectService.save(subject);
    }
}
