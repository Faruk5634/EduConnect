package com.educonnect.repository;

import com.educonnect.model.Announcement;
import com.educonnect.model.AnnouncementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    //Duyuru tipine göre filtreleme iş mantığı
    List<Announcement> findByType(AnnouncementType type);

    //Duyuru yapan Öğretmene göre filtreleme iş mantığı
    List<Announcement> findByAuthorId(Long authorId);

    //Duyrunun yapıldığı zamana göre filtreleme iş mantığı
    List<Announcement> findByCreatedDateAfter(java.time.LocalDateTime date);
}