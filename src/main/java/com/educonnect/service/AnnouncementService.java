package com.educonnect.service;

import com.educonnect.dto.AnnouncementDTO;
import com.educonnect.model.Announcement;
import com.educonnect.model.AnnouncementType;
import com.educonnect.model.User;
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
    private final UserService userService; // 🚀 EKLENDİ: Aktif kullanıcıyı ve okulunu bulmak için

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               TeacherRepository teacherRepository,
                               ClassroomRepository classroomRepository,
                               UserService userService) {
        this.announcementRepository = announcementRepository;
        this.teacherRepository = teacherRepository;
        this.classroomRepository = classroomRepository;
        this.userService = userService;
    }

    public Announcement createAnnouncement(Announcement announcement, String username) {
        User currentUser = userService.getCurrentUser(); // 🚀 SİHİRLİ DOKUNUŞ

        Teacher author = teacherRepository.findByUserUsername(username).orElse(null);
        announcement.setAuthor(author);
        announcement.setCreatedDate(LocalDateTime.now());

        announcement.setSchool(currentUser.getSchool()); // 🚀 DUYURUYU OKULA MÜHÜRLE

        return announcementRepository.save(announcement);
    }

    // 🚀 YENİ MOTOR: Birden fazla sınıfa tek seferde duyuru ve dosya fırlatma
    public void createAnnouncementWithFileForMultipleClasses(String title, String content, AnnouncementType type, List<Long> classroomIds, MultipartFile file, String username) {
        User currentUser = userService.getCurrentUser();
        Teacher author = teacherRepository.findByUserUsername(username).orElse(null);

        // 1. DOSYA YÜKLEME İŞLEMİ (Sadece 1 kere yapılır)
        String savedFileName = null;
        String savedFileUrl = null;

        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "uploads/announcements/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                savedFileName = file.getOriginalFilename();
                String uniqueFilename = UUID.randomUUID().toString() + "_" + savedFileName;
                Path filePath = uploadPath.resolve(uniqueFilename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                savedFileUrl = "/uploads/announcements/" + uniqueFilename;
            } catch (Exception e) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Dosya yüklenirken bir hata oluştu.", e);
            }
        }

        // 2. SINIFLARA DAĞITIM (Seçilen her sınıf için ayrı bir duyuru kaydı oluştur)
        if (classroomIds != null && !classroomIds.isEmpty()) {
            for (Long cId : classroomIds) {
                Announcement announcement = new Announcement();
                announcement.setTitle(title);
                announcement.setContent(content);
                announcement.setType(type);
                announcement.setCreatedDate(LocalDateTime.now());
                announcement.setSchool(currentUser.getSchool());
                announcement.setAuthor(author);

                Classroom classroom = classroomRepository.findById(cId).orElse(null);
                announcement.setClassroom(classroom);

                announcement.setFileName(savedFileName);
                announcement.setFileUrl(savedFileUrl);

                announcementRepository.save(announcement);
            }
        } else {
            // Eğer hiçbir sınıf seçilmemişse (Genel Duyuru ise)
            Announcement announcement = new Announcement();
            announcement.setTitle(title);
            announcement.setContent(content);
            announcement.setType(type);
            announcement.setCreatedDate(LocalDateTime.now());
            announcement.setSchool(currentUser.getSchool());
            announcement.setAuthor(author);
            announcement.setFileName(savedFileName);
            announcement.setFileUrl(savedFileUrl);

            announcementRepository.save(announcement);
        }
    }

    private AnnouncementDTO convertToDTO(Announcement a) {
        return new AnnouncementDTO(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getCreatedDate(),
                a.getAuthor() != null ? a.getAuthor().getFirstName() + " " + a.getAuthor().getLastName() : "Yönetim (Admin)",
                a.getType(),
                a.getClassroom() != null ? a.getClassroom().getName() : "Genel Duyuru",
                a.getFileName(),
                a.getFileUrl()
        );
    }

    public List<AnnouncementDTO> getAllAnnouncements() {
        User currentUser = userService.getCurrentUser();
        // 🚀 KİLİT: Sadece bu okula ait olanları getir
        return announcementRepository.findBySchool(currentUser.getSchool()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getAnnouncementsByType(AnnouncementType type) {
        User currentUser = userService.getCurrentUser();
        // 🚀 KİLİT: Sadece bu okula ait olanları getir
        return announcementRepository.findByTypeAndSchool(type, currentUser.getSchool()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getAnnouncementsByAuthorId(Long authorId) {
        // Zaten belirli bir öğretmeni aradığı için okul filtresi teknik olarak içerilmiş oluyor
        return announcementRepository.findByAuthorId(authorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getAnnouncementsAfter(LocalDateTime date) {
        User currentUser = userService.getCurrentUser();
        return announcementRepository.findBySchoolAndCreatedDateAfter(currentUser.getSchool(), date).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 🚀 YENİ: Duyuru silme işlemi. Dosya varsa filesystem'den de kaldırılır.
    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Duyuru bulunamadı: " + id));

        // Eğer duyurunun bir dosya URL'si varsa, dosyayı silmeye çalış
        if (announcement.getFileUrl() != null && !announcement.getFileUrl().isEmpty()) {
            try {
                String filePathStr = announcement.getFileUrl();
                // fileUrl proje içinde "/uploads/..." şeklinde tutuluyor; baştaki /'i kaldır
                if (filePathStr.startsWith("/")) {
                    filePathStr = filePathStr.substring(1);
                }
                Path filePath = Paths.get(filePathStr);
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                // Burada log atmak daha doğru olur; runtime exception fırlatmayarak silme başarısız olsa bile
                // duyurunun veritabanından kaldırılmasına devam ediyoruz.
                System.err.println("Dosya silinirken hata: " + e.getMessage());
            }
        }

        announcementRepository.deleteById(id);
    }

    // Basit erişim: id'ye göre duyuruyu döndürür veya 404 fırlatır
    public Announcement getAnnouncementById(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Duyuru bulunamadı: " + id));
    }
}