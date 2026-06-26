package com.educonnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Sınıf adı boş bırakılamaz!")
    private String name; // İnsan olmadığı için "name" olarak bıraktık.

    private Integer gradeLevel;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    // SİHİRLİ KÖPRÜ: Sabahçı/Öğlenci mantığı için harika bir seçim!
    // Classroom.java dosyasındaki ilgili kısım
    @ManyToOne
    @JoinColumn(name = "teacher_id") // 🚀 Öğretmen silindiğinde sınıfın öğretmeni boşalır, sınıf silinmez
    private Teacher homeroomTeacher;

    // Öğrenciler kısmını da şu şekilde güncelle (Sınıf silinirse öğrencilerin sınıf bilgisi temizlenir)
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.REMOVE)
    private List<Student> students;

    @OneToMany
    private List<Announcement> announcements = new ArrayList<>();

    public Classroom(Long id, String name, Integer gradeLevel, Teacher homeroomTeacher, List<Student> students, List<Announcement> announcements,School school) {
        this.id = id;
        this.name = name;
        this.gradeLevel = gradeLevel;
        this.homeroomTeacher = homeroomTeacher;
        this.students = students;
        this.announcements = announcements;
        this.school=school;
    }

    public Classroom() {
    }

    // --- GETTER VE SETTER METOTLARI (Aynı şekilde duruyor) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(Integer gradeLevel) { this.gradeLevel = gradeLevel; }
    public Teacher getHomeroomTeacher() { return homeroomTeacher; }
    public void setHomeroomTeacher(Teacher homeroomTeacher) { this.homeroomTeacher = homeroomTeacher; }
    public List<Student> getStudents() { return students; }
    public void setStudents(List<Student> students) { this.students = students; }
    public List<Announcement> getAnnouncements() { return announcements; }
    public void setAnnouncements(List<Announcement> announcements) { this.announcements = announcements; }
    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }
}