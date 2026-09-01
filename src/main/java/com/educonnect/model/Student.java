package com.educonnect.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonIgnore;

@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "schoolId", type = Long.class))
@Filter(name = "tenantFilter", condition = "school_id = :schoolId")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Column(unique = true)
    private String schoolNumber;

    private String grade;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    @JsonIgnore
    private School school;

    @NotBlank(message = "Cinsiyet boş bırakılamaz!")
    private String gender;
}