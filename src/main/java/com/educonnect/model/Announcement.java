package com.educonnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Teacher author;

    @Enumerated(EnumType.STRING)
    private AnnouncementType type;

    // 🚀 GÜNCELLEME: Tekil sınıf yerine Çoklu Sınıf Bağlantısı (Many-To-Many)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "announcement_classrooms",
            joinColumns = @JoinColumn(name = "announcement_id"),
            inverseJoinColumns = @JoinColumn(name = "classroom_id")
    )
    private List<Classroom> classrooms = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    @JsonIgnore
    private School school;

    @ElementCollection
    @CollectionTable(name = "announcement_files", joinColumns = @JoinColumn(name = "announcement_id"))
    private List<AnnouncementFile> attachedFiles = new ArrayList<>();

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

    public List<Classroom> getClassrooms() { return classrooms; }
    public void setClassrooms(List<Classroom> classrooms) { this.classrooms = classrooms; }

    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }
    public List<AnnouncementFile> getAttachedFiles() { return attachedFiles; }
    public void setAttachedFiles(List<AnnouncementFile> attachedFiles) { this.attachedFiles = attachedFiles; }
}