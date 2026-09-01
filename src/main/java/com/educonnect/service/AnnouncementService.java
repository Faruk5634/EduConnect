package com.educonnect.service;

import com.educonnect.dto.AnnouncementDTO;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.mapper.AnnouncementMapper;
import com.educonnect.model.*;
import com.educonnect.repository.AnnouncementRepository;
import com.educonnect.repository.ClassroomRepository;
import com.educonnect.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AnnouncementService {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementService.class);
    private static final String ANNOUNCEMENTS_SUBDIR = "announcements";
    private static final int MAX_FILES = 5;

    private final AnnouncementRepository announcementRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService; // 🚀 was duplicating this logic inline before

    private School getTenantSchool(User user) {
        if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
            return null;
        }
        if (user.getSchool() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kullanıcının atanmış bir okulu yok.");
        }
        return user.getSchool();
    }

    /**
     * JSON-only announcement creation (no attachments). Takes the DTO, not
     * the raw entity — see AnnouncementCreateRequest for why that matters.
     */
    public Announcement createAnnouncement(com.educonnect.dto.AnnouncementCreateRequest request, String username) {
        User currentUser = userService.getCurrentUser();
        Teacher author = teacherRepository.findByUserUsername(username).orElse(null);
        School tenantSchool = getTenantSchool(currentUser);

        if (tenantSchool != null && (author == null || author.getUser() == null || author.getUser().getSchool() == null || !tenantSchool.getId().equals(author.getUser().getSchool().getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu duyuruyu başka bir okul adına oluşturamazsınız.");
        }

        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setType(request.getType());
        announcement.setAuthor(author);
        announcement.setCreatedDate(LocalDateTime.now());
        announcement.setSchool(tenantSchool != null ? tenantSchool : currentUser.getSchool());

        if (request.getClassroomIds() != null && !request.getClassroomIds().isEmpty()) {
            announcement.setClassrooms(classroomRepository.findAllById(request.getClassroomIds()));
        }

        return announcementRepository.save(announcement);
    }

    public void createAnnouncementWithFileForMultipleClasses(String title, String content, AnnouncementType type,
                                                             List<Long> classroomIds, List<MultipartFile> files,
                                                             String username) {
        User currentUser = userService.getCurrentUser();
        Teacher author = teacherRepository.findByUserUsername(username).orElse(null);
        School tenantSchool = getTenantSchool(currentUser);

        if (tenantSchool != null && (author == null || author.getUser() == null || author.getUser().getSchool() == null || !tenantSchool.getId().equals(author.getUser().getSchool().getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu duyuruyu başka bir okul adına oluşturamazsınız.");
        }

        List<AnnouncementFile> savedFiles = storeAttachments(files);

        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setType(type);
        announcement.setCreatedDate(LocalDateTime.now());
        announcement.setSchool(tenantSchool != null ? tenantSchool : currentUser.getSchool());
        announcement.setAuthor(author);
        announcement.setAttachedFiles(new ArrayList<>(savedFiles));

        if (classroomIds != null && !classroomIds.isEmpty()) {
            List<Classroom> targetClasses = classroomRepository.findAllById(classroomIds);
            announcement.setClassrooms(targetClasses);
        }

        announcementRepository.save(announcement);
    }

    private List<AnnouncementFile> storeAttachments(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        if (files.size() > MAX_FILES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "En fazla " + MAX_FILES + " adet dosya yükleyebilirsiniz.");
        }

        List<AnnouncementFile> savedFiles = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    FileStorageService.StoredFile stored = fileStorageService.storeFile(file, ANNOUNCEMENTS_SUBDIR);
                    savedFiles.add(new AnnouncementFile(stored.originalFileName(), stored.publicUrl()));
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Dosyalar yüklenirken bir hata oluştu.", e);
        }
        return savedFiles;
    }

    public List<AnnouncementDTO> getAllAnnouncements() {
        User currentUser = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(currentUser);
        return (tenantSchool != null ? announcementRepository.findBySchool(tenantSchool) : announcementRepository.findAll())
                .stream()
                .map(AnnouncementMapper::toDto).collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getAnnouncementsByType(AnnouncementType type) {
        User currentUser = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(currentUser);
        return (tenantSchool != null
                ? announcementRepository.findByTypeAndSchool(type, tenantSchool)
                : announcementRepository.findAll().stream().filter(a -> a.getType() == type).collect(Collectors.toList()))
                .stream()
                .map(AnnouncementMapper::toDto).collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getAnnouncementsByAuthorId(Long authorId) {
        User currentUser = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(currentUser);
        return (tenantSchool != null
                ? announcementRepository.findByAuthorIdAndSchool(authorId, tenantSchool)
                : announcementRepository.findByAuthorId(authorId))
                .stream()
                .map(AnnouncementMapper::toDto).collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getAnnouncementsAfter(LocalDateTime date) {
        User currentUser = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(currentUser);
        return (tenantSchool != null
                ? announcementRepository.findBySchoolAndCreatedDateAfter(tenantSchool, date)
                : announcementRepository.findAll().stream().filter(a -> a.getCreatedDate() != null && a.getCreatedDate().isAfter(date)).collect(Collectors.toList()))
                .stream()
                .map(AnnouncementMapper::toDto).collect(Collectors.toList());
    }

    /**
     * 🚀 SRP FIX: this authorization decision used to live in
     * AnnouncementController, comparing role names as raw strings
     * ("ROLE_ADMIN".equals(...)). It's business logic about who owns an
     * announcement, so it belongs next to the rest of the announcement
     * domain logic, and it now compares the Role enum directly instead of
     * strings.
     */
    public void assertCanDelete(Announcement announcement, User currentUser) {
        boolean isAuthor = announcement.getAuthor() != null
                && announcement.getAuthor().getUser() != null
                && announcement.getAuthor().getUser().getUsername().equals(currentUser.getUsername());

        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN
                || currentUser.getRole() == Role.ROLE_VICE_ADMIN
                || currentUser.getRole() == Role.ROLE_SUPER_ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu duyuruyu silme yetkiniz yok.");
        }
    }

    public void deleteAnnouncement(Long id) {
        Announcement announcement = getAnnouncementById(id);

        if (announcement.getAttachedFiles() != null) {
            for (AnnouncementFile file : announcement.getAttachedFiles()) {
                try {
                    fileStorageService.deleteFile(file.getFileUrl());
                } catch (IOException e) {
                    // Not fatal: the DB record is the source of truth, an orphaned
                    // file on disk is a cleanup nuisance, not a correctness issue.
                    log.warn("Duyuru dosyası silinemedi (id={}, dosya={}): {}", id, file.getFileUrl(), e.getMessage());
                }
            }
        }

        announcementRepository.deleteById(id);
    }

    public Announcement getAnnouncementById(Long id) {
        User currentUser = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(currentUser);
        return tenantSchool != null
                ? announcementRepository.findByIdAndSchool(id, tenantSchool)
                .orElseThrow(() -> new ResourceNotFoundException("Duyuru bulunamadı: " + id))
                : announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Duyuru bulunamadı: " + id));
    }
}
