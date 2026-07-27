package com.educonnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String branch;
    private String username;
    private String phone;
    private String email;
    private List<ClassroomInfo> homeroomClasses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassroomInfo {
        private Long id;
        private String name;
    }
}