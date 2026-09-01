package com.educonnect.mapper;

import com.educonnect.dto.AnnouncementDTO;
import com.educonnect.model.Announcement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class AnnouncementMapper {

    private AnnouncementMapper() {
    }

    public static AnnouncementDTO toDto(Announcement announcement) {
        List<String> classNames;
        if (announcement.getClassrooms() != null && !announcement.getClassrooms().isEmpty()) {
            classNames = announcement.getClassrooms().stream()
                    .map(classroom -> classroom.getName())
                    .collect(Collectors.toList());
        } else {
            classNames = new ArrayList<>();
            classNames.add("Genel Duyuru");
        }

        return new AnnouncementDTO(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getCreatedDate(),
                announcement.getAuthor() != null ? announcement.getAuthor().getFirstName() + " " + announcement.getAuthor().getLastName() : "Yönetim (Admin)",
                announcement.getType(),
                classNames,
                announcement.getAttachedFiles()
        );
    }
}
