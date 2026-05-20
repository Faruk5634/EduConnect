package com.educonnect.dto;

import java.util.List;

public class TeacherDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String branch;
    private List<String> homeroomClassNames; // Sadece sınıf isimleri (Örn: ["5-A", "6-B"])

    public TeacherDTO() {
    }

    public TeacherDTO(Long id, String firstName, String lastName, String branch, List<String> homeroomClassNames) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.branch = branch;
        this.homeroomClassNames = homeroomClassNames;
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
    public List<String> getHomeroomClassNames() { return homeroomClassNames; }
    public void setHomeroomClassNames(List<String> homeroomClassNames) { this.homeroomClassNames = homeroomClassNames; }
}