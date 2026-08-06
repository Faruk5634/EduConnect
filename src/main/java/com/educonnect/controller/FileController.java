package com.educonnect.controller;

import com.educonnect.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            FileStorageService.StoredFile stored = fileStorageService.storeFile(file);
            return ResponseEntity.ok("Dosya başarıyla yüklendi! Ambar kayıt adı: " + stored.storedFileName());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Dosya ambarına yazılırken bir hata oluştu: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}