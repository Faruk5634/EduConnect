package com.educonnect.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Entity
@Table(name = "schools")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Örn: "EduConnect İzmir Şubesi"

    private String address;
    private String phone;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String neighborhood;

    private String email;

    // Bu okula ait olan sınıflar
    @JsonIgnore
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL)
    private List<Classroom> classrooms;

    // Bu okula kayıtlı kullanıcılar (Müdür, Öğretmen, Öğrenci vb.)
    @JsonIgnore
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL)
    private List<User> users;

    @Column(nullable = false)
    private String schoolType;
}