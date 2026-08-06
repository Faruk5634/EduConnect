package com.educonnect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 🚀 SOLID FIX: this used to be a static inner class defined directly inside
 * ParentController, alongside a near-identical one in TeacherController.
 * Request DTOs belong in the dto package with everything else — a teammate
 * looking for "all request shapes" shouldn't have to know to look inside a
 * controller for two of them.
 */
@Data
public class ParentRequest {

    @NotBlank(message = "Veli adı boş bırakılamaz!")
    @Size(min = 2, message = "Veli adı en az 2 karakter olmalıdır!")
    private String firstName;

    @NotBlank(message = "Veli soyadı boş bırakılamaz!")
    private String lastName;

    @NotBlank(message = "E-posta adresi boş bırakılamaz!")
    @Email(message = "Geçerli bir e-posta adresi giriniz!")
    private String email;

    @NotBlank(message = "Telefon numarası zorunludur!")
    private String phoneNumber;

    private String username;
    private String password;
}