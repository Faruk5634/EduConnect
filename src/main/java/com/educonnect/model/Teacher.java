package com.educonnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Entity
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Öğretmen adı boş bırakılamaz!")
    private String firstName;

    @NotBlank(message = "Öğretmen soyadı boş bırakılamaz!")
    private String lastName;

    @NotBlank(message = "Branş boş bırakılamaz!")
    private String branch;

    // SİHİRLİ KÖPRÜ: Classroom'daki 'homeroomTeacher' değişkeni ile eşleşir.
    @OneToMany(mappedBy = "homeroomTeacher")
    @JsonIgnore // Postman sonsuz döngüye girip çökmesin diye kalkanımız
    private List<Classroom> homeroomClasses;

    public Teacher(Long id, String firstName, String lastName, String branch, List<Classroom> homeroomClasses) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.branch = branch;
        this.homeroomClasses = homeroomClasses;
    }

    public Teacher() {
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
    public List<Classroom> getHomeroomClasses() { return homeroomClasses; }
    public void setHomeroomClasses(List<Classroom> homeroomClasses) { this.homeroomClasses = homeroomClasses; }
}