package com.educonnect.model;

import jakarta.persistence.*; // Bütün sihirli kelimeleri tek seferde getirmesi için sonuna * koyduk

import java.util.ArrayList;
import java.util.List;

@Entity
public class Classroom {
    @Id // Kimlik numaramız
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int gradeLevel;

    @ManyToOne
    private Teacher homeroomTeacher;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Student> students = new ArrayList<>();

    @OneToMany
    private List<Announcement> announcements = new ArrayList<>();

    public Classroom(Long id, String name, int gradeLevel, Teacher homeroomTeacher, List<Student> students, List<Announcement> announcements) {
        this.id = id;
        this.name = name;
        this.gradeLevel = gradeLevel;
        this.homeroomTeacher = homeroomTeacher;
        this.students = students;
        this.announcements = announcements;
    }

    public Classroom() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(int gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public Teacher getHomeroomTeacher() {
        return homeroomTeacher;
    }

    public void setHomeroomTeacher(Teacher homeroomTeacher) {
        this.homeroomTeacher = homeroomTeacher;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(List<Announcement> announcements) {
        this.announcements = announcements;
    }
}
