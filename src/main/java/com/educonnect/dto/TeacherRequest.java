package com.educonnect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeacherRequest {

    @NotBlank(message = "Öğretmen adı boş bırakılamaz!")
    private String firstName;

    @NotBlank(message = "Öğretmen soyadı boş bırakılamaz!")
    private String lastName;

    @NotBlank(message = "Branş boş bırakılamaz!")
    private String branch;

    private String username;
    private String password;
    private String phone;

    @Email(message = "Geçerli bir e-posta adresi giriniz!")
    private String email;
}