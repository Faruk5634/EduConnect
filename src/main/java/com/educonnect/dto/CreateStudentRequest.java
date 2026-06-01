package com.educonnect.dto;

import lombok.Data;

@Data
public class CreateStudentRequest {
    private String username;
    private String password;
    private String schoolNumber;
    private String grade;
}

