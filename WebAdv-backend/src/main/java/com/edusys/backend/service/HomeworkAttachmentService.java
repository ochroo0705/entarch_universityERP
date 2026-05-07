package com.edusys.backend.service;

import com.edusys.backend.model.Homework;
import com.edusys.backend.model.HomeworkAttachment;
import com.edusys.backend.repository.HomeworkAttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class HomeworkAttachmentService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final byte[] PDF_SIGNATURE = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D};
    private static final byte[] PNG_SIGNATURE = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG_PREFIX = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    private static final Map<String, Set<String>> ALLOWED_EXTENSIONS = Map.of(
            "application/pdf", Set.of("pdf"),
            "image/png", Set.of("png"),
            "image/jpeg", Set.of("jpg", "jpeg")
    );

    private final FileStorageService fileStorageService;
    private final HomeworkAttachmentRepository homeworkAttachmentRepository;

    public HomeworkAttachmentService(
            FileStorageService fileStorageService,
            HomeworkAttachmentRepository homeworkAttachmentRepository
    ) {
        this.fileStorageService = fileStorageService;
        this.homeworkAttachmentRepository = homeworkAttachmentRepository;
    }

    public List<HomeworkAttachment> storeAttachments(Homework homework, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        List<HomeworkAttachment> attachments = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String mimeType = validateAndNormalize(file);
            String storedPath = fileStorageService.store(file, "homework");

            HomeworkAttachment attachment = new HomeworkAttachment();
            attachment.setHomework(homework);
            attachment.setOriginalFilename(safeOriginalFilename(file));
            attachment.setStoredPath(storedPath);
            attachment.setMimeType(mimeType);
            attachment.setSizeBytes(file.getSize());
            attachment.setUploadedAt(LocalDateTime.now());
            attachments.add(homeworkAttachmentRepository.save(attachment));
        }
        return attachments;
    }

    public void deleteAttachment(HomeworkAttachment attachment) {
        fileStorageService.delete(attachment.getStoredPath());
        homeworkAttachmentRepository.delete(attachment);
    }

    public void deleteLegacyAttachment(String attachmentUrl) {
        String storedPath = extractStoredPath(attachmentUrl);
        if (storedPath != null && !storedPath.isBlank()) {
            fileStorageService.delete(storedPath);
        }
    }

    public boolean isPreviewable(String mimeType) {
        return "application/pdf".equals(mimeType) || (mimeType != null && mimeType.startsWith("image/"));
    }

    public String extractStoredPath(String attachmentUrl) {
        if (attachmentUrl == null || attachmentUrl.isBlank()) {
            return null;
        }
        String prefix = "/api/files/download/";
        if (!attachmentUrl.startsWith(prefix)) {
            return null;
        }
        return attachmentUrl.substring(prefix.length());
    }

    private String validateAndNormalize(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Attachment file cannot be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Attachment exceeds the 10MB limit");
        }

        String mimeType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT).trim();
        if (!ALLOWED_EXTENSIONS.containsKey(mimeType)) {
            throw new IllegalArgumentException("Unsupported attachment type. Only JPG, PNG, and PDF files are allowed");
        }

        String extension = getExtension(safeOriginalFilename(file));
        if (!ALLOWED_EXTENSIONS.get(mimeType).contains(extension)) {
            throw new IllegalArgumentException("Attachment extension does not match the uploaded file type");
        }

        validateSignature(file, mimeType);
        return mimeType;
    }

    private void validateSignature(MultipartFile file, String mimeType) {
        byte[] signature = readSignature(file);
        boolean valid = switch (mimeType) {
            case "application/pdf" -> startsWith(signature, PDF_SIGNATURE);
            case "image/png" -> startsWith(signature, PNG_SIGNATURE);
            case "image/jpeg" -> startsWith(signature, JPEG_PREFIX);
            default -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException("Attachment content does not match the uploaded file type");
        }
    }

    private byte[] readSignature(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return inputStream.readNBytes(8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read attachment content", e);
        }
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        return value.length >= prefix.length && Arrays.equals(Arrays.copyOf(value, prefix.length), prefix);
    }

    private String safeOriginalFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return "attachment";
        }
        return Paths.get(originalFilename).getFileName().toString();
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
