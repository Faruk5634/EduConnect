package com.educonnect.controller;

import com.educonnect.dto.AnnouncementDTO;
import com.educonnect.model.Announcement;
import com.educonnect.model.AnnouncementType;
import com.educonnect.service.AnnouncementService;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
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
}