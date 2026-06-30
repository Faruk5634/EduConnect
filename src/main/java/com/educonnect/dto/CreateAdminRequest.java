package com.educonnect.dto;
import lombok.Data;

@Data
public class CreateAdminRequest {
    private String username;
    private String password;
    private String firstName; // YENİ
    private String lastName;  // YENİ
    private String phone;     // YENİ
    private String email;     // YENİ
    private String role;
    private Long schoolId;
}