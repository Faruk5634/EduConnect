package com.educonnect.controller;

import com.educonnect.dto.AnnouncementCreateRequest;
import com.educonnect.dto.AnnouncementDTO;
import com.educonnect.model.Announcement;
import com.educonnect.model.AnnouncementType;
import com.educonnect.model.User;
import com.educonnect.service.AnnouncementService;
import com.educonnect.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserService userService;

    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PostMapping("/create")
    public Announcement createAnnouncement(@Valid @RequestBody AnnouncementCreateRequest request, Principal principal) {
        return announcementService.createAnnouncement(request, principal.getName());
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

    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createAnnouncementWithFiles(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("type") AnnouncementType type,
            @RequestParam(value = "classroomIds", required = false) List<Long> classroomIds,
            @RequestParam(value = "files", required = false) List<org.springframework.web.multipart.MultipartFile> files,
            Principal principal) {

        announcementService.createAnnouncementWithFileForMultipleClasses(title, content, type, classroomIds, files, principal.getName());
        return ResponseEntity.ok().body("Duyuru başarıyla dağıtıldı!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        Announcement announcement = announcementService.getAnnouncementById(id);

        // Ownership/role decision now lives in AnnouncementService, using the
        // Role enum instead of string comparisons — see assertCanDelete().
        announcementService.assertCanDelete(announcement, currentUser);
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }
}