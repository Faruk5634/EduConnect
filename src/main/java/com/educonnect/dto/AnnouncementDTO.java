package com.educonnect.dto;

import com.educonnect.model.AnnouncementType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDTO {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdDate;
    private String authorName; // Sadece öğretmenin adı!
    private AnnouncementType type;
    private String classroomName; // Sadece sınıfın adı!
}