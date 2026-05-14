package com.educonnect.model;

import jakarta.persistence.*;

import javax.annotation.processing.Generated;

@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname;
    private String schoolNumber;
    @ManyToOne // SİHİR BURADA: Her öğrencinin sadece 1 velisi olur.
    private Parent parent;



    public Student(Long id, String name, String surname, String schoolNumber) {
        this.id = id;
        this.name = name;
        this.surname = surname;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
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
