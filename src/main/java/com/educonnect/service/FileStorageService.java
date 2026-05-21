package com.educonnect.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    // Dosyaların kaydedileceği klasörün adı (Projenin ana dizininde oluşacak)
    private final String UPLOAD_DIR = "uploads/";

    public FileStorageService() {
        // Sistem ilk çalıştığında eğer "uploads" adında bir klasör yoksa, otomatik olarak oluşturur
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    // Dosyayı alıp klasöre kaydeden ve yeni dosya adını döndüren metot
    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Boş dosya yüklenemez!");
        }

        // Aynı isimde iki dosya çakışmasın diye dosyanın başına rastgele benzersiz bir kod (UUID) ekliyoruz
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

        // Dosyanın tam olarak nereye kaydedileceğini belirliyoruz (Örn: uploads/1234-5678_vesikalik.jpg)
        Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);

        // Dosyayı fiziksel olarak o klasöre kopyalıyoruz
        Files.copy(file.getInputStream(), filePath);

        // Sadece yeni dosya adını geri dönüyoruz ki veritabanına bunu kaydedelim
        return uniqueFileName;
    }
}