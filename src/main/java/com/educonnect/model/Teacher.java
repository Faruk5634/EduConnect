package com.educonnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @OneToMany(mappedBy = "homeroomTeacher", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Classroom> homeroomClasses;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    @Transient
    private String username;

    @Transient
    private String password;

    @Transient
    private String phone; // 🚀 EKLENDİ

    @Transient
    private String email; // 🚀 EKLENDİ

    public Teacher(Long id, String firstName, String lastName, String branch, List<Classroom> homeroomClasses) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.branch = branch;
        this.homeroomClasses = homeroomClasses;
    }

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

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getUsername() { return username; }
    @com.fasterxml.jackson.annotation.JsonSetter("username")
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    @com.fasterxml.jackson.annotation.JsonSetter("password")
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    @com.fasterxml.jackson.annotation.JsonSetter("phone")
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    @com.fasterxml.jackson.annotation.JsonSetter("email")
    public void setEmail(String email) { this.email = email; }
}