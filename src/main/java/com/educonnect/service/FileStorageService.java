package com.educonnect.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 🚀 DRY FIX: AnnouncementService used to reimplement this exact same
 * "create dir if missing, prefix with a UUID, copy the file" logic inline,
 * with its own separate uploads/announcements/ directory. Two copies of the
 * same infrastructure concern, in two different layers, meant a future
 * change (e.g. moving to S3) would have had to happen twice.
 *
 * This is now the ONLY place that touches the filesystem for uploads.
 * Callers pass a logical subdirectory ("", "announcements", ...) and get
 * back both the stored filename and the public URL to save on the entity.
 */
@Service
public class FileStorageService {

    private static final String BASE_UPLOAD_DIR = "uploads";

    public StoredFile storeFile(MultipartFile file) throws IOException {
        return storeFile(file, "");
    }

    public StoredFile storeFile(MultipartFile file, String subDirectory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Boş dosya yüklenemez!");
        }

        String normalizedSubDir = (subDirectory == null || subDirectory.isBlank()) ? "" : subDirectory.trim();
        Path uploadPath = normalizedSubDir.isEmpty()
                ? Paths.get(BASE_UPLOAD_DIR)
                : Paths.get(BASE_UPLOAD_DIR, normalizedSubDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFileName = file.getOriginalFilename();
        // Sanitize filename: remove path separators and normalize to base name
        String safeOriginal = originalFileName == null ? "unnamed" : Paths.get(originalFileName).getFileName().toString();
        // Remove potentially problematic characters and limit length
        safeOriginal = safeOriginal.replaceAll("[^A-Za-z0-9._-]", "_");
        if (safeOriginal.length() > 100) safeOriginal = safeOriginal.substring(0, 100);

        String uniqueFileName = UUID.randomUUID() + "_" + safeOriginal;
        Path filePath = uploadPath.resolve(uniqueFileName).normalize();

        // Ensure the file stays inside the upload directory
        if (!filePath.startsWith(uploadPath.toAbsolutePath())) {
            throw new SecurityException("Invalid file path");
        }

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String publicUrl = "/" + BASE_UPLOAD_DIR + "/"
                + (normalizedSubDir.isEmpty() ? "" : normalizedSubDir + "/")
                + uniqueFileName;

        return new StoredFile(safeOriginal, uniqueFileName, publicUrl);
    }

    public void deleteFile(String publicUrl) throws IOException {
        if (publicUrl == null || publicUrl.isBlank()) {
            return;
        }
        String relative = publicUrl.startsWith("/") ? publicUrl.substring(1) : publicUrl;
        Files.deleteIfExists(Paths.get(relative));
    }

    /**
     * Result of a successful upload: the name the user originally gave the file,
     * the unique name it was actually stored under, and the URL to serve it from.
     */
    public record StoredFile(String originalFileName, String storedFileName, String publicUrl) {
    }
}