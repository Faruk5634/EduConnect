package com.educonnect.dto;

import com.educonnect.model.AnnouncementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 🚀 SECURITY / SOLID FIX: the JSON "create announcement" endpoint used to
 * accept a raw Announcement ENTITY as its request body. That means a client
 * could set id, school, author, createdDate — any field on the entity —
 * directly from the request (classic over-posting / mass-assignment risk),
 * and it made the API's actual contract whatever JPA happened to expose.
 *
 * This DTO defines exactly what a caller is allowed to submit; everything
 * else (author, school, createdDate) is still decided server-side in
 * AnnouncementService, same as before.
 */
@Data
public class AnnouncementCreateRequest {

    @NotBlank(message = "Başlık boş bırakılamaz!")
    private String title;

    @NotBlank(message = "İçerik boş bırakılamaz!")
    private String content;

    @NotNull(message = "Duyuru tipi zorunludur!")
    private AnnouncementType type;

    private List<Long> classroomIds;
}