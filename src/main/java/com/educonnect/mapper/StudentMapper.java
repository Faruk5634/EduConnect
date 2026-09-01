package com.educonnect.mapper;

import com.educonnect.dto.StudentDTO;
import com.educonnect.model.Student;

public final class StudentMapper {

    private StudentMapper() {
    }

    public static StudentDTO toDto(Student student) {
        String parentName = student.getParent() != null
                ? student.getParent().getFirstName() + " " + student.getParent().getLastName()
                : "Veli Atanmadı";
        Long parentId = student.getParent() != null ? student.getParent().getId() : null;
        String username = student.getUser() != null ? student.getUser().getUsername() : null;
        String phone = student.getUser() != null ? student.getUser().getPhone() : null;
        String email = student.getUser() != null ? student.getUser().getEmail() : null;

        return new StudentDTO(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getSchoolNumber(),
                parentName,
                parentId,
                username,
                student.getGrade(),
                student.getGender(),
                phone,
                email
        );
    }
}
