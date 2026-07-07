package com.educonnect.dto;

public class StudentDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String schoolNumber;
    private String parentFullName;
    private Long parentId;
    private String username;
    private String grade;
    private String gender;

    // 🚀 YENİ EKLENENLER
    private String phone;
    private String email;

    public StudentDTO() {
    }

    public StudentDTO(Long id, String firstName, String lastName, String schoolNumber, String parentFullName, Long parentId, String username, String grade, String gender, String phone, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.schoolNumber = schoolNumber;
        this.parentFullName = parentFullName;
        this.parentId = parentId;
        this.username = username;
        this.grade = grade;
        this.gender = gender;
        this.phone = phone; // 🚀 EKLENDİ
        this.email = email; // 🚀 EKLENDİ
    }

    // --- GETTER VE SETTER METOTLARI ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getSchoolNumber() { return schoolNumber; }
    public void setSchoolNumber(String schoolNumber) { this.schoolNumber = schoolNumber; }
    public String getParentFullName() { return parentFullName; }
    public void setParentFullName(String parentFullName) { this.parentFullName = parentFullName; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    // 🚀 YENİ EKLENEN GETTER/SETTER
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}