package com.educonnect.dto;

import java.util.List;

public class ParentDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber; // 🚀 EKLENDİ
    private String username;    // 🚀 EKLENDİ (Lise velileri için)
    private List<String> studentNames;

    public ParentDTO() {
    }

    public ParentDTO(Long id, String firstName, String lastName, String email, String phoneNumber, String username, List<String> studentNames) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber; // 🚀 EKLENDİ
        this.username = username;       // 🚀 EKLENDİ
        this.studentNames = studentNames;
    }

    // --- GETTER VE SETTER METOTLARI ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<String> getStudentNames() { return studentNames; }
    public void setStudentNames(List<String> studentNames) { this.studentNames = studentNames; }
}