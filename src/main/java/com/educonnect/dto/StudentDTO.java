package com.educonnect.dto;

public class StudentDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String schoolNumber;

    // DİKKAT: Bütün Parent objesi yerine sadece Velinin Adı Soyadı!
    private String parentFullName;

    public StudentDTO() {
    }

    public StudentDTO(Long id, String firstName, String lastName, String schoolNumber, String parentFullName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.schoolNumber = schoolNumber;
        this.parentFullName = parentFullName;
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
}
