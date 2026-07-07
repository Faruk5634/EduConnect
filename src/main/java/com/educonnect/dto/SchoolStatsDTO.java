package com.educonnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolStatsDTO {
    private long totalStudents;
    private long totalTeachers;
    private long totalClasses;
    private long totalParents;
    private long totalAnnouncements;
}