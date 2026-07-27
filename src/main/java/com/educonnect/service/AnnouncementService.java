package com.educonnect.service;

import com.educonnect.dto.AnnouncementDTO;
import com.educonnect.model.Announcement;
import com.educonnect.model.AnnouncementFile;
import com.educonnect.model.AnnouncementType;
import com.educonnect.model.User;
import com.educonnect.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 🚀 EKLENDİ
import com.educonnect.repository.TeacherRepository;
import com.educonnect.model.Teacher;
import org.springframework.web.multipart.MultipartFile;
import com.educonnect.repository.ClassroomRepository;
import com.educonnect.model.Classroom;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional // 🚀 MİMARİ DOKUNUŞ: Dosya yüklerken DB patlarsa işlemleri geri al!
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final UserService userService;

    // 🚀 MİMARİ DOKUNUŞ: Magic String temizlendi, sabit değişkene atandı.
    private static final String UPLOAD_DIR = "uploads/announcements/";
    private static final String UPLOAD_URL_PREFIX = "/uploads/announcements/";

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
        User currentUser = userService.getCurrentUser();
        Teacher author = teacherRepository.findByUserUsername(username).orElse(null);
        announcement.setAuthor(author);
        announcement.setCreatedDate(LocalDateTime.now());
        announcement.setSchool(currentUser.getSchool());
        return announcementRepository.save(announcement);
    }

    public void createAnnouncementWithFileForMultipleClasses(String title, String content, AnnouncementType type, List<Long> classroomIds, List<MultipartFile> files, String username) {
        User currentUser = userService.getCurrentUser();
        Teacher author = teacherRepository.findByUserUsername(username).orElse(null);

        List<AnnouncementFile> savedFiles = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            if (files.size() > 5) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "En fazla 5 adet dosya yükleyebilirsiniz.");
            }

            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String savedFileName = file.getOriginalFilename();
                        String uniqueFilename = UUID.randomUUID().toString() + "_" + savedFileName;
                        Path filePath = uploadPath.resolve(uniqueFilename);
                        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                        String savedFileUrl = UPLOAD_URL_PREFIX + uniqueFilename;
                        savedFiles.add(new AnnouncementFile(savedFileName, savedFileUrl));
                    }
                }
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Dosyalar yüklenirken bir hata oluştu.", e);
            }
        }

        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setType(type);
        announcement.setCreatedDate(LocalDateTime.now());
        announcement.setSchool(currentUser.getSchool());
        announcement.setAuthor(author);
        announcement.setAttachedFiles(new ArrayList<>(savedFiles));

        if (classroomIds != null && !classroomIds.isEmpty()) {
            List<Classroom> targetClasses = classroomRepository.findAllById(classroomIds);
            announcement.setClassrooms(targetClasses);
        }

        announcementRepository.save(announcement);
    }

    private AnnouncementDTO convertToDTO(Announcement a) {
        List<String> classNames = new ArrayList<>();
        if (a.getClassrooms() != null && !a.getClassrooms().isEmpty()) {
            classNames = a.getClassrooms().stream()
                    .map(Classroom::getName)
                    .collect(Collectors.toList());
        } else {
            classNames.add("Genel Duyuru");
        }

        return new AnnouncementDTO(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getCreatedDate(),
                a.getAuthor() != null ? a.getAuthor().getFirstName() + " " + a.getAuthor().getLastName() : "Yönetim (Admin)",
                a.getType(),
                classNames,
                a.getAttachedFiles()
        );
    }

    public List<AnnouncementDTO> getAllAnnouncements() {
        User currentUser = userService.getCurrentUser();
        return announcementRepository.findBySchool(currentUser.getSchool()).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getAnnouncementsByType(AnnouncementType type) {
        User currentUser = userService.getCurrentUser();
        return announcementRepository.findByTypeAndSchool(type, currentUser.getSchool()).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getAnnouncementsByAuthorId(Long authorId) {
        return announcementRepository.findByAuthorId(authorId).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getAnnouncementsAfter(LocalDateTime date) {
        User currentUser = userService.getCurrentUser();
        return announcementRepository.findBySchoolAndCreatedDateAfter(currentUser.getSchool(), date).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Duyuru bulunamadı: " + id));

        if (announcement.getAttachedFiles() != null && !announcement.getAttachedFiles().isEmpty()) {
            for (AnnouncementFile file : announcement.getAttachedFiles()) {
                try {
                    String filePathStr = file.getFileUrl();
                    if (filePathStr.startsWith("/")) {
                        filePathStr = filePathStr.substring(1);
                    }
                    Path filePath = Paths.get(filePathStr);
                    Files.deleteIfExists(filePath);
                } catch (Exception e) {
                    System.err.println("Dosya silinirken hata: " + e.getMessage());
                }
            }
        }

        announcementRepository.deleteById(id);
    }

    public Announcement getAnnouncementById(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Duyuru bulunamadı: " + id));
    }
}