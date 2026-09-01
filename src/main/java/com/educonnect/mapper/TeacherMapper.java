package com.educonnect.mapper;

import com.educonnect.dto.TeacherDTO;
import com.educonnect.model.Teacher;

import java.util.List;
import java.util.stream.Collectors;

public final class TeacherMapper {

    private TeacherMapper() {
    }

    public static TeacherDTO toDto(Teacher teacher) {
        List<TeacherDTO.ClassroomInfo> classInfos = teacher.getHomeroomClasses() != null
                ? teacher.getHomeroomClasses().stream()
                .map(c -> new TeacherDTO.ClassroomInfo(c.getId(), c.getName()))
                .collect(Collectors.toList())
                : List.of();

        String username = teacher.getUser() != null ? teacher.getUser().getUsername() : null;
        String phone = teacher.getUser() != null ? teacher.getUser().getPhone() : null;
        String email = teacher.getUser() != null ? teacher.getUser().getEmail() : null;

        return new TeacherDTO(
                teacher.getId(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getBranch(),
                username,
                phone,
                email,
                classInfos
        );
    }
}
