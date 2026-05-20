package com.educonnect.dto;

import java.util.List;

public class ClassroomDTO {
    private Long id;
    private String name;
    private int gradeLevel;
    private String homeroomTeacherFullName; // Sadece "Gökmen Deniz"
    private List<String> studentNames; // Sadece öğrenci isimleri listesi

    public ClassroomDTO() {
    }

    public ClassroomDTO(Long id, String name, int gradeLevel, String homeroomTeacherFullName, List<String> studentNames) {
        this.id = id;
        this.name = name;
        this.gradeLevel = gradeLevel;
        this.homeroomTeacherFullName = homeroomTeacherFullName;
        this.studentNames = studentNames;
    }

    // --- GETTER VE SETTER METOTLARI ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(int gradeLevel) { this.gradeLevel = gradeLevel; }
    public String getHomeroomTeacherFullName() { return homeroomTeacherFullName; }
    public void setHomeroomTeacherFullName(String homeroomTeacherFullName) { this.homeroomTeacherFullName = homeroomTeacherFullName; }
    public List<String> getStudentNames() { return studentNames; }
    public void setStudentNames(List<String> studentNames) { this.studentNames = studentNames; }
}