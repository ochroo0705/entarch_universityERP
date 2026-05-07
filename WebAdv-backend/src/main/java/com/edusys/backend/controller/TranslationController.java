package com.edusys.backend.controller;

import com.edusys.backend.dto.TranslationBulkDTO;
import com.edusys.backend.dto.TranslationDTO;
import com.edusys.backend.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/translations")
@Tag(name = "Translations", description = "APIs for managing bilingual content translations")
@SecurityRequirement(name = "bearerAuth")
public class TranslationController {

    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @GetMapping("/{entityType}/{entityId}")
    @Operation(summary = "Get translations for an entity",
               description = "Returns all translations for a specific entity. Optionally filter by locale.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Translations retrieved successfully")
    })
    public List<TranslationDTO> getTranslations(
            @Parameter(description = "Entity type (e.g., subject, announcement, homework)")
            @PathVariable String entityType,
            @Parameter(description = "Entity ID")
            @PathVariable Long entityId,
            @Parameter(description = "Optional locale filter (e.g., en, mn)")
            @RequestParam(required = false) String locale) {
        if (locale != null && !locale.isBlank()) {
            return translationService.getTranslations(entityType, entityId, locale);
        }
        return translationService.getTranslations(entityType, entityId);
    }

    @GetMapping("/{entityType}/batch")
    @Operation(summary = "Get translations for multiple entities",
               description = "Returns translations for a batch of entity IDs, grouped by entity ID.")
    public Map<Long, List<TranslationDTO>> getTranslationsBatch(
            @Parameter(description = "Entity type") @PathVariable String entityType,
            @Parameter(description = "Comma-separated entity IDs") @RequestParam List<Long> ids) {
        return translationService.getTranslationsBatch(entityType, ids);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set a single translation",
               description = "Create or update a single translation entry (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Translation saved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public TranslationDTO setTranslation(@Valid @RequestBody TranslationDTO dto) {
        return translationService.setTranslation(dto);
    }

    @PutMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set multiple translations",
               description = "Create or update multiple translations at once (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Translations saved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public List<TranslationDTO> setTranslationsBulk(@Valid @RequestBody TranslationBulkDTO bulkDTO) {
        return translationService.setTranslationsBulk(bulkDTO.translations());
    }

    @DeleteMapping("/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete all translations for an entity",
               description = "Remove all translation entries for a specific entity (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Translations deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteTranslations(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        translationService.deleteTranslations(entityType, entityId);
        return ResponseEntity.noContent().build();
    }
}
