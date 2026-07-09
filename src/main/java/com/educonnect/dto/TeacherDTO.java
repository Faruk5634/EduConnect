package com.educonnect.dto;

import java.util.List;

public class TeacherDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String branch;
    private String username;
    private String phone; // 🚀 EKLENDİ
    private String email; // 🚀 EKLENDİ

    public static class ClassroomInfo {
        private Long id;
        private String name;

        public ClassroomInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }
        public Long getId() { return id; }
        public String getName() { return name; }
    }

    private List<ClassroomInfo> homeroomClasses;

    public TeacherDTO() {
    }

    public TeacherDTO(Long id, String firstName, String lastName, String branch, String username, String phone, String email, List<ClassroomInfo> homeroomClasses) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.branch = branch;
        this.username = username;
        this.phone = phone; // 🚀 EKLENDİ
        this.email = email; // 🚀 EKLENDİ
        this.homeroomClasses = homeroomClasses;
    }

    // --- GETTER VE SETTER METOTLARI ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<ClassroomInfo> getHomeroomClasses() { return homeroomClasses; }
    public void setHomeroomClasses(List<ClassroomInfo> homeroomClasses) { this.homeroomClasses = homeroomClasses; }
}