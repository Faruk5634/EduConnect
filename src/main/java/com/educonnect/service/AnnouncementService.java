package com.educonnect.service;

import com.educonnect.model.Announcement;
import com.educonnect.model.AnnouncementType;
import com.educonnect.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public Announcement createAnnouncement(Announcement announcement) {
        // Tarih atama iş mantığı artık Service'te!
        announcement.setCreatedDate(LocalDateTime.now());
        return announcementRepository.save(announcement);
    }

    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAll();
    }

    // Duyuru tipine göre filtreleme iş mantığı
    public List<Announcement> getAnnouncementsByType(AnnouncementType type) {
        return announcementRepository.findByType(type);
    }
    //Duyuru yapan Öğretmene göre filtreleme iş mantığı
    public List<Announcement> getAnnouncementsByAuthorId(Long authorId) {
        return announcementRepository.findByAuthorId(authorId);
    }
    //Duyrunun yapıldığı zamana göre filtreleme iş mantığı
    public List<Announcement> getAnnouncementsAfter(java.time.LocalDateTime date) {
        return announcementRepository.findByCreatedDateAfter(date);
    }
}