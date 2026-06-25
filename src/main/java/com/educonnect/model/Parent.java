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

    // cascade = CascadeType.ALL geri geldi!
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Student> students;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public Parent(Long id, String firstName, String lastName, String email, String phoneNumber, List<Student> students) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.students = students;
    }

    public Parent() {
    }

    // ... (Aşağıdaki Getter ve Setter metotların aynı şekilde kalsın, onlarda sorun yok)
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
}