package com.educonnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private LocalDateTime createdDate;

    @ManyToOne
    private Teacher author;

    @Enumerated(EnumType.STRING)
    private AnnouncementType type;

    public Announcement(Long id, String title, String content, LocalDateTime createdDate, Teacher author, AnnouncementType type) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.author = author;
        this.type = type;
    }


    public Announcement() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Teacher getAuthor() {
        return author;
    }

    public void setAuthor(Teacher author) {
        this.author = author;
    }

    public AnnouncementType getType() {
        return type;
    }

    public void setType(AnnouncementType type) {
        this.type = type;
    }
}


