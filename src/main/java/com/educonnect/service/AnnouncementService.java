package com.educonnect.service;

import com.educonnect.model.Announcement;
import com.educonnect.model.AnnouncementType;
import com.educonnect.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;
import com.educonnect.repository.TeacherRepository;
import com.educonnect.model.Teacher;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final TeacherRepository teacherRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository,TeacherRepository teacherRepository) {
        this.announcementRepository = announcementRepository;
        this.teacherRepository=teacherRepository;
    }

    public Announcement createAnnouncement(Announcement announcement,String username) {
        Teacher author = teacherRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Duyuru yapacak öğretmen bulunamadı!"));

        announcement.setAuthor(author); // Yazarı güvenli şekilde ata
        announcement.setCreatedDate(LocalDateTime.now()); // Tarihi ata

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