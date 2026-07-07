package com.educonnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
public class Parent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Veli adı boş bırakılamaz!")
    @Size(min = 2, message = "Veli adı en az 2 karakter olmalıdır!")
    private String firstName;

    @NotBlank(message = "Veli soyadı boş bırakılamaz!")
    private String lastName;

    @NotBlank(message = "E-posta adresi boş bırakılamaz!")
    private String email;

    @NotBlank(message = "Telefon numarası zorunludur!")
    private String phoneNumber;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Student> students;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    // 🚀 SİHİRLİ DOKUNUŞ: Frontend'den şifre ve kullanıcı adı alabilmek için
    // @Transient, "Bunu veritabanına sütun olarak ekleme, sadece geçici olarak hafızada tut" demektir.
    @Transient
    private String username;

    @Transient
    private String password;

    public Parent() {
    }

    public Parent(Long id, String firstName, String lastName, String email, String phoneNumber, List<Student> students) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.students = students;
    }

    // --- GETTER VE SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public List<Student> getStudents() { return students; }
    public void setStudents(List<Student> students) { this.students = students; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // 🚀 Yeni eklenenlerin Getter/Setter'ları
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}