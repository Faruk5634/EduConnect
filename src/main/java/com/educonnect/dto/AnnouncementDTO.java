package com.educonnect.dto;

import com.educonnect.model.AnnouncementType;
import com.educonnect.model.AnnouncementFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDTO {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdDate;
    private String authorName;
    private AnnouncementType type;

    // 🚀 GÜNCELLEME: Tek bir sınıf adı yerine, hedef sınıfların listesi
    private List<String> targetClasses;

    private List<AnnouncementFile> attachedFiles;
}