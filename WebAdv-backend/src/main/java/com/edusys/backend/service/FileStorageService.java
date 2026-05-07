package com.edusys.backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.storage.type:local}")
    private String storageType;

    @Value("${app.storage.local.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.storage.s3.bucket:}")
    private String bucket;

    @Value("${app.storage.s3.region:us-east-1}")
    private String region;

    @Value("${app.storage.s3.endpoint:}")
    private String endpoint;

    @Value("${app.storage.s3.access-key:}")
    private String accessKey;

    @Value("${app.storage.s3.secret-key:}")
    private String secretKey;

    @Value("${app.storage.s3.path-style-access-enabled:true}")
    private boolean pathStyleAccessEnabled;

    @Value("${app.storage.s3.key-prefix:}")
    private String keyPrefix;

    private Path rootLocation;
    private S3Client s3Client;

    @PostConstruct
    public void init() {
        if (isS3Storage()) {
            validateS3Configuration();
            s3Client = buildS3Client();
            return;
        }

        rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String store(MultipartFile file, String subfolder) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "file";
        }

        originalFilename = Paths.get(originalFilename).getFileName().toString();

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String storedFilename = UUID.randomUUID() + extension;
        String relativePath = normalizeRelativePath(subfolder + "/" + storedFilename);

        if (isS3Storage()) {
            storeInS3(file, relativePath);
        } else {
            storeLocally(file, relativePath);
        }

        return relativePath;
    }

    public Resource loadAsResource(String filePath) {
        String normalizedPath = normalizeRelativePath(filePath);
        if (isS3Storage()) {
            return loadFromS3(normalizedPath);
        }

        try {
            Path file = resolveLocalPath(normalizedPath);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("File not found: " + normalizedPath);
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + normalizedPath, e);
        }
    }

    public void delete(String filePath) {
        String normalizedPath = normalizeRelativePath(filePath);
        if (isS3Storage()) {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(buildS3Key(normalizedPath))
                    .build());
            return;
        }

        try {
            Path file = resolveLocalPath(normalizedPath);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + normalizedPath, e);
        }
    }

    public Path resolvePath(String filePath) {
        if (isS3Storage()) {
            throw new UnsupportedOperationException("Local path resolution is unavailable for object storage");
        }
        return resolveLocalPath(normalizeRelativePath(filePath));
    }

    private void storeLocally(MultipartFile file, String relativePath) {
        try {
            Path targetPath = resolveLocalPath(relativePath);
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private void storeInS3(MultipartFile file, String relativePath) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(buildS3Key(relativePath))
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read upload for object storage", e);
        }
    }

    private Resource loadFromS3(String relativePath) {
        try {
            ResponseBytes<?> objectBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(buildS3Key(relativePath))
                    .build());
            String filename = Paths.get(relativePath).getFileName().toString();
            return new ByteArrayResource(objectBytes.asByteArray()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
        } catch (NoSuchKeyException e) {
            throw new RuntimeException("File not found: " + relativePath, e);
        }
    }

    private Path resolveLocalPath(String filePath) {
        Path file = rootLocation.resolve(filePath).normalize();
        if (!file.startsWith(rootLocation)) {
            throw new SecurityException("Cannot access file outside upload directory");
        }
        return file;
    }

    private String normalizeRelativePath(String filePath) {
        String normalized = filePath.replace("\\", "/");
        if (normalized.contains("..")) {
            throw new SecurityException("Cannot access file outside upload directory");
        }
        return normalized;
    }

    private String buildS3Key(String relativePath) {
        String normalizedPrefix = keyPrefix == null ? "" : keyPrefix.trim();
        if (normalizedPrefix.endsWith("/")) {
            normalizedPrefix = normalizedPrefix.substring(0, normalizedPrefix.length() - 1);
        }
        return normalizedPrefix.isEmpty() ? relativePath : normalizedPrefix + "/" + relativePath;
    }

    private boolean isS3Storage() {
        return "s3".equalsIgnoreCase(storageType) || "object-storage".equalsIgnoreCase(storageType);
    }

    private void validateS3Configuration() {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("Object storage bucket must be configured when app.storage.type=s3");
        }
        if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Object storage credentials must be configured when app.storage.type=s3");
        }
    }

    private S3Client buildS3Client() {
        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .chunkedEncodingEnabled(false)
                        .pathStyleAccessEnabled(pathStyleAccessEnabled)
                        .build());

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }
}
