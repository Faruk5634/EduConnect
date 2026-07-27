package com.educonnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    // Şimdilik sistemin bozulmaması için bunları tutuyoruz (DTO'ya geçince silinecek)
    @Transient
    private String username;

    @Transient
    private String password;

    @com.fasterxml.jackson.annotation.JsonSetter("username")
    public void setUsername(String username) { this.username = username; }

    @com.fasterxml.jackson.annotation.JsonSetter("password")
    public void setPassword(String password) { this.password = password; }
}