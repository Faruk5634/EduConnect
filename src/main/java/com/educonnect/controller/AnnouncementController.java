package com.educonnect.controller;

import com.educonnect.dto.AnnouncementDTO;
import com.educonnect.model.Announcement;
import com.educonnect.model.AnnouncementType;
import com.educonnect.service.AnnouncementService;
import com.educonnect.service.UserService;
import com.educonnect.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserService userService;

    public AnnouncementController(AnnouncementService announcementService, UserService userService) {
        this.announcementService = announcementService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public Announcement createAnnouncement(@RequestBody Announcement announcement, Principal principal) {
        return announcementService.createAnnouncement(announcement, principal.getName());
    }

    @GetMapping
    public List<AnnouncementDTO> getAllAnnouncements() {
        return announcementService.getAllAnnouncements();
    }

    @GetMapping("/type/{type}")
    public List<AnnouncementDTO> getAnnouncementsByType(@PathVariable AnnouncementType type) {
        return announcementService.getAnnouncementsByType(type);
    }

    @GetMapping("/author/{authorId}")
    public List<AnnouncementDTO> getAnnouncementsByAuthorId(@PathVariable Long authorId) {
        return announcementService.getAnnouncementsByAuthorId(authorId);
    }

    @GetMapping("/after")
    public List<AnnouncementDTO> getAnnouncementsAfter(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return announcementService.getAnnouncementsAfter(date);
    }

    // 🚀 GÜNCELLENDİ: Artık tek bir id yerine, sınıf id'lerinden oluşan bir liste alıyor!
    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createAnnouncement(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("type") AnnouncementType type,
            @RequestParam(value = "classroomIds", required = false) List<Long> classroomIds, // 🚀 ARTIK LİSTE ALIYOR
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            Principal principal) {

        announcementService.createAnnouncementWithFileForMultipleClasses(title, content, type, classroomIds, file, principal.getName());
        return ResponseEntity.ok().body("Duyuru başarıyla dağıtıldı!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Long id, Principal principal) {
        // Yetki kontrolü: yazar veya admin olmalı
        // current user
        User currentUser = userService.getCurrentUser();

        Announcement announcement = announcementService.getAnnouncementById(id);

        boolean isAuthor = announcement.getAuthor() != null
                && announcement.getAuthor().getUser() != null
                && announcement.getAuthor().getUser().getUsername().equals(currentUser.getUsername());

        boolean isAdmin = currentUser.getRole() != null && (
                currentUser.getRole().name().equals("ROLE_ADMIN") || currentUser.getRole().name().equals("ROLE_SUPER_ADMIN")
        );

        if (!isAuthor && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu duyuruyu silme yetkiniz yok.");
        }

        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }
}