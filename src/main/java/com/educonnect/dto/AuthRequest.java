package com.educonnect.dto;

import com.educonnect.model.Role;
import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;

    // 🚀 YENİ: Kaydedilecek kişinin rütbesi (Örn: ROLE_TEACHER)
    private Role role;

    // 🚀 YENİ: Eğer işlemi Super Admin yapıyorsa okul ID'sini girmek zorunda
    private Long schoolId;
}
