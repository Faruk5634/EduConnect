package com.educonnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomDTO {
    private Long id;
    private String name;
    private int gradeLevel;
    private String homeroomTeacherFullName; // Sadece "Gökmen Deniz"
    private List<String> studentNames; // Sadece öğrenci isimleri listesi
}