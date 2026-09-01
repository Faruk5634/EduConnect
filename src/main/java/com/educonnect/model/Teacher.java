package com.educonnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "schoolId", type = Long.class))
@Filter(name = "tenantFilter", condition = "user_id IN (SELECT u.id FROM users u WHERE u.school_id = :schoolId)")
@Entity
@Getter
@Setter
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

    // Şimdilik sistemin bozulmaması için bunları tutuyoruz (DTO'ya geçince silinecek)
    @Transient
    private String username;

    @Transient
    private String password;

    @Transient
    private String phone;

    @Transient
    private String email;

    @com.fasterxml.jackson.annotation.JsonSetter("username")
    public void setUsername(String username) { this.username = username; }

    @com.fasterxml.jackson.annotation.JsonSetter("password")
    public void setPassword(String password) { this.password = password; }

    @com.fasterxml.jackson.annotation.JsonSetter("phone")
    public void setPhone(String phone) { this.phone = phone; }

    @com.fasterxml.jackson.annotation.JsonSetter("email")
    public void setEmail(String email) { this.email = email; }
}