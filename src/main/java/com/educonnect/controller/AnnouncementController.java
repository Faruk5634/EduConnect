package com.educonnect.controller;

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
    public List<Announcement> getAllAnnouncements() {
        return announcementService.getAllAnnouncements();
    }

    //Duyuru tipine göre filtreleme iş mantığı
    @GetMapping("/type/{type}")
    public List<Announcement> getAnnouncementsByType(@PathVariable AnnouncementType type) {
        return announcementService.getAnnouncementsByType(type);
    }

    //Duyuru yapan Öğretmene göre filtreleme iş mantığı
    @GetMapping("/author/{authorId}")
    public List<Announcement> getAnnouncementsByAuthorId(@PathVariable Long authorId) {
        return announcementService.getAnnouncementsByAuthorId(authorId);
    }





    @GetMapping("/after")
    public List<Announcement> getAnnouncementsAfter(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {

        return announcementService.getAnnouncementsAfter(date);
    }


}