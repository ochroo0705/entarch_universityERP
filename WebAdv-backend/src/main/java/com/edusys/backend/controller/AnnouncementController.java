package com.edusys.backend.controller;

import com.edusys.backend.model.Announcement;
import com.edusys.backend.model.User;
import com.edusys.backend.service.AnnouncementService;
import com.edusys.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@Tag(name = "Announcements", description = "APIs for managing announcements")
@SecurityRequirement(name = "bearerAuth")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserRepository userRepository;

    public AnnouncementController(AnnouncementService announcementService, UserRepository userRepository) {
        this.announcementService = announcementService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Get all announcements", description = "Returns a list of all announcements")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "404", description = "No announcements found")
    })
    public List<Announcement> getAll() {
        return announcementService.findAll();
    }

    @PostMapping
    @Operation(summary = "Create announcement", description = "Create a new announcement")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Announcement created successfully")
    })
    public Announcement create(@org.springframework.web.bind.annotation.RequestBody Announcement announcement,
                                Authentication authentication) {
        String username = authentication.getName();
        userRepository.findByUsername(username).ifPresent(announcement::setCreatedBy);
        announcement.setCreatedAt(LocalDateTime.now());
        return announcementService.save(announcement);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get announcement by ID", description = "Returns a single announcement by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "404", description = "Announcement not found")
    })
    public ResponseEntity<Announcement> getById(@PathVariable Long id) {
        return announcementService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete announcement", description = "Delete an announcement (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Announcement deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Announcement not found")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (announcementService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        announcementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
