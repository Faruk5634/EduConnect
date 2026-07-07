package com.educonnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.persistence.FetchType;

@Entity
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(columnDefinition = "TEXT") // Uzun içerikler için veritabanını rahatlatır
    private String content;
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Teacher author;

    @Enumerated(EnumType.STRING)
    private AnnouncementType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    // 🚀 SİHİRLİ KÖPRÜ: Genel duyuruların başka okullara sızmaması için
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    @JsonIgnore
    private School school;

    private String fileName;
    private String fileUrl;

    public Announcement(Long id, String title, String content, LocalDateTime createdDate, Teacher author, AnnouncementType type, Classroom classroom, School school) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.author = author;
        this.type = type;
        this.classroom = classroom;
        this.school = school;
    }

    public Announcement() {
    }

    // --- GETTER VE SETTER METOTLARI ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public Teacher getAuthor() { return author; }
    public void setAuthor(Teacher author) { this.author = author; }
    public AnnouncementType getType() { return type; }
    public void setType(AnnouncementType type) { this.type = type; }
    public Classroom getClassroom() { return classroom; }
    public void setClassroom(Classroom classroom) { this.classroom = classroom; }
    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; } // 🚀 Eklendi
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}