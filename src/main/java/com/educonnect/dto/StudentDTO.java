package com.educonnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String schoolNumber;
    private String parentFullName;
    private Long parentId;
    private String username;
    private String grade;
    private String gender;
    private String phone;
    private String email;
}