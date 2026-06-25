package com.educonnect.service;

import com.educonnect.dto.AnnouncementDTO;
import com.educonnect.model.Announcement;
import com.educonnect.model.AnnouncementType;
import com.educonnect.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;
import com.educonnect.repository.TeacherRepository;
import com.educonnect.model.Teacher;
import org.springframework.web.multipart.MultipartFile;
import com.educonnect.repository.ClassroomRepository;
import com.educonnect.model.Classroom;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository, TeacherRepository teacherRepository, ClassroomRepository classroomRepository) {
        this.announcementRepository = announcementRepository;
        this.teacherRepository = teacherRepository;
        this.classroomRepository=classroomRepository;
    }

    // Eski metot (geriye dönük uyumluluk için burada durabilir)
    public Announcement createAnnouncement(Announcement announcement, String username) {
        Teacher author = teacherRepository.findByUserUsername(username).orElse(null);
        announcement.setAuthor(author);
        announcement.setCreatedDate(LocalDateTime.now());
        return announcementRepository.save(announcement);
    }

    // 🚀 YENİ: Dosyalı Duyuru Oluşturma Motoru
    public Announcement createAnnouncementWithFile(String title, String content, AnnouncementType type, Long classroomId, MultipartFile file, String username) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setType(type);
        announcement.setCreatedDate(LocalDateTime.now());

        // Yazar Ataması
        Teacher author = teacherRepository.findByUserUsername(username).orElse(null);
        announcement.setAuthor(author);

        // Sınıf Ataması
        if (classroomId != null) {
            Classroom classroom = classroomRepository.findById(classroomId).orElse(null);
            announcement.setClassroom(classroom);
        }

        // 📂 DOSYA YÜKLEME İŞLEMİ
        if (file != null && !file.isEmpty()) {
            try {
                // Dosyaları kaydedeceğimiz ana klasör
                String uploadDir = "uploads/announcements/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Dosya isimlerinin çakışmaması için benzersiz bir kod ekliyoruz
                String originalFilename = file.getOriginalFilename();
                String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
                Path filePath = uploadPath.resolve(uniqueFilename);

                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                announcement.setFileName(originalFilename);
                announcement.setFileUrl("/uploads/announcements/" + uniqueFilename);

            } catch (Exception e) {
                throw new RuntimeException("Dosya yüklenirken bir hata oluştu: " + e.getMessage());
            }
        }

        return announcementRepository.save(announcement);
    }

    // 🚀 SİHİRLİ DÖNÜŞÜM METODU (Sadece en güncel hali bırakıldı)
    private AnnouncementDTO convertToDTO(Announcement a) {
        return new AnnouncementDTO(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getCreatedDate(),
                a.getAuthor() != null ? a.getAuthor().getFirstName() + " " + a.getAuthor().getLastName() : "Yönetim (Admin)",
                a.getType(),
                a.getClassroom() != null ? a.getClassroom().getName() : "Genel Duyuru",
                a.getFileName(), // 🚀 Dosya Adı
                a.getFileUrl()   // 🚀 Dosya Linki
        );
    }

    public List<AnnouncementDTO> getAllAnnouncements() {
        return announcementRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getAnnouncementsByType(AnnouncementType type) {
        return announcementRepository.findByType(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getAnnouncementsByAuthorId(Long authorId) {
        return announcementRepository.findByAuthorId(authorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getAnnouncementsAfter(LocalDateTime date) {
        return announcementRepository.findByCreatedDateAfter(date).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}