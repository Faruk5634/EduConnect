package com.educonnect.dto;

import java.util.List;

public class ParentDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    // DİKKAT: Artık kocaman Student objelerini değil,
    // sadece çocukların isimlerini veya numaralarını taşıyan basit bir liste yollayacağız!
    private List<String> studentNames;

    // Boş Constructor
    public ParentDTO() {
    }

    // Dolu Constructor
    public ParentDTO(Long id, String firstName, String lastName, String email, List<String> studentNames) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
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
    public List<String> getStudentNames() { return studentNames; }
    public void setStudentNames(List<String> studentNames) { this.studentNames = studentNames; }
}
