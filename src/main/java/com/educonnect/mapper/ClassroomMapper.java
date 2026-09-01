package com.educonnect.mapper;

import com.educonnect.dto.ClassroomDTO;
import com.educonnect.model.Classroom;

import java.util.List;
import java.util.stream.Collectors;

public final class ClassroomMapper {

    private ClassroomMapper() {
    }

    public static ClassroomDTO toDto(Classroom classroom) {
        String teacherName = classroom.getHomeroomTeacher() != null
                ? classroom.getHomeroomTeacher().getFirstName() + " " + classroom.getHomeroomTeacher().getLastName()
                : "Rehber Öğretmen Atanmadı";

        List<String> studentNames = classroom.getStudents() != null
                ? classroom.getStudents().stream()
                .map(s -> s.getFirstName() + " " + s.getLastName())
                .collect(Collectors.toList())
                : List.of();

        return new ClassroomDTO(
                classroom.getId(),
                classroom.getName(),
                classroom.getGradeLevel(),
                teacherName,
                studentNames
        );
    }
}
