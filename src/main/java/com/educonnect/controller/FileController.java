package com.educonnect.controller;

import com.educonnect.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    // DOSYA YÜKLEME KAPISI -> http://localhost:8080/api/files/upload
    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Dosyayı ambar görevlisine veriyoruz, o da uploads klasörüne kaydedip bize yeni adı dönüyor
            String uniqueFileName = fileStorageService.storeFile(file);

            return ResponseEntity.ok("Dosya başarıyla yüklendi! Ambar kayıt adı: " + uniqueFileName);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Dosya ambarına yazılırken bir hata oluştu: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}