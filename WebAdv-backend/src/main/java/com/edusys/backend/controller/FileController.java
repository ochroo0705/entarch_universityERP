package com.edusys.backend.controller;

import com.edusys.backend.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File Management", description = "APIs for uploading and downloading files")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload a file", description = "Upload a file and get back the stored file path")
    public Map<String, String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subfolder", defaultValue = "general") String subfolder
    ) {
        // Whitelist subfolder values to prevent directory traversal
        if (!subfolder.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException("Invalid subfolder name");
        }

        String filePath = fileStorageService.store(file, subfolder);
        return Map.of(
                "filePath", filePath,
                "downloadUrl", "/api/files/download/" + filePath
        );
    }

    @GetMapping("/download/{subfolder}/{filename}")
    @Operation(summary = "Download a file", description = "Download a previously uploaded file")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String subfolder,
            @PathVariable String filename
    ) {
        if (!subfolder.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException("Invalid subfolder name");
        }
        // Sanitize filename
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String filePath = subfolder + "/" + filename;
        Resource resource = fileStorageService.loadAsResource(filePath);

        String contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
