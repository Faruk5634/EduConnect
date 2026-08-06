package com.educonnect.dto;

import com.educonnect.model.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAdminRequest {

    @NotBlank(message = "Kullanıcı adı boş bırakılamaz!")
    private String username;

    private String password;

    @NotBlank(message = "Ad boş bırakılamaz!")
    private String firstName;

    @NotBlank(message = "Soyad boş bırakılamaz!")
    private String lastName;

    private String phone;
    private String email;

    // İsteğe bağlı - kendi şifresini değiştiren admin bunu doğrulamak için gönderir
    private String currentPassword;

    // 🚀 FIX: was a raw String compared with .equals("ROLE_VICE_ADMIN") in the
    // service layer. Now it's the same Role enum used everywhere else in the
    // app (AuthRequest, User, ...), so Jackson validates it at deserialization
    // time instead of silently accepting typos as "not vice admin, so admin".
    private Role role;

    private Long schoolId;
}