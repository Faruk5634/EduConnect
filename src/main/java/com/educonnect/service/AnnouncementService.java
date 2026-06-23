package com.educonnect.service;

import com.educonnect.dto.AnnouncementDTO;
import com.educonnect.model.Announcement;
import com.educonnect.model.AnnouncementType;
import com.educonnect.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;
import com.educonnect.repository.TeacherRepository;
import com.educonnect.model.Teacher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final TeacherRepository teacherRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository, TeacherRepository teacherRepository) {
        this.announcementRepository = announcementRepository;
        this.teacherRepository = teacherRepository;
    }

    public Announcement createAnnouncement(Announcement announcement, String username) {
        Teacher author = teacherRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Duyuru yapacak öğretmen bulunamadı!"));

        announcement.setAuthor(author);
        announcement.setCreatedDate(LocalDateTime.now());

        return announcementRepository.save(announcement);
    }

    // 🚀 SİHİRLİ DÖNÜŞÜM METODU: Çiğ Duyuruyu, temiz DTO'ya çevirir
    private AnnouncementDTO convertToDTO(Announcement a) {
        return new AnnouncementDTO(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getCreatedDate(),
                a.getAuthor() != null ? a.getAuthor().getFirstName() + " " + a.getAuthor().getLastName() : "Sistem",
                a.getType(),
                a.getClassroom() != null ? a.getClassroom().getName() : "Genel Duyuru"
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