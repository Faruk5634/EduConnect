package com.educonnect.repository;

import com.educonnect.model.Announcement;
import com.educonnect.model.AnnouncementType;
import com.educonnect.model.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // 🚀 GÜVENLİ SORGULAR: Artık tüm duyuruları çekerken "Okul" filtresi uyguluyoruz
    List<Announcement> findBySchool(School school);
    List<Announcement> findByTypeAndSchool(AnnouncementType type, School school);
    List<Announcement> findBySchoolAndCreatedDateAfter(School school, java.time.LocalDateTime date);

    List<Announcement> findByAuthorId(Long authorId);
    List<Announcement> findByClassroom_IdOrderByCreatedDateDesc(Long classroomId);

    // 🚀 DÜZELTME: Artık duyurunun direkt okul bağlantısı olduğu için sorgu çok daha stabil!
    @Query("SELECT count(a) FROM Announcement a WHERE a.school = :school")
    long countBySchool(@Param("school") School school);
}