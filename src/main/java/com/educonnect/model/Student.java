package com.educonnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Öğrenci adı boş bırakılamaz!")
    @Size(min = 2, message = "Öğrenci adı en az 2 karakter olmalıdır!")
    private String firstName;

    @NotBlank(message = "Öğrenci soyadı boş bırakılamaz!")
    private String lastName;

    @NotBlank(message = "Okul numarası zorunludur!")
    private String schoolNumber;

    @ManyToOne // SİHİR BURADA: Her öğrencinin sadece 1 velisi olur.
    @JoinColumn(name = "parent_id") // Veritabanında "parent_id" adında bir sütun oluşturur ve bu sütun Parent tablosundaki id'ye referans verir.
    private Parent parent;



    public Student(Long id, String firstName, String lastName, String schoolNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.schoolNumber = schoolNumber;
    }

    public Student() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSchoolNumber() {
        return schoolNumber;
    }

    public void setSchoolNumber(String schoolNumber) {
        this.schoolNumber = schoolNumber;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }
}
