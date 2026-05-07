package com.edusys.backend.ai.controller;

import com.edusys.backend.ai.dto.AiStudentOptionResponse;
import com.edusys.backend.ai.service.AiLookupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiLookupController {

    private final AiLookupService aiLookupService;

    public AiLookupController(AiLookupService aiLookupService) {
        this.aiLookupService = aiLookupService;
    }

    @GetMapping("/students/access-list")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<AiStudentOptionResponse>> getAccessibleStudents() {
        return ResponseEntity.ok(aiLookupService.getAccessibleStudents());
    }
}
