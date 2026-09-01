package com.educonnect.mapper;

import com.educonnect.dto.ParentDTO;
import com.educonnect.model.Parent;

import java.util.List;
import java.util.stream.Collectors;

public final class ParentMapper {

    private ParentMapper() {
    }

    public static ParentDTO toDto(Parent parent) {
        List<String> studentNames = parent.getStudents() != null
                ? parent.getStudents().stream()
                .map(student -> student.getFirstName() + " " + student.getLastName() + "|" + student.getSchoolNumber())
                .collect(Collectors.toList())
                : List.of();

        return new ParentDTO(
                parent.getId(),
                parent.getFirstName(),
                parent.getLastName(),
                parent.getEmail(),
                parent.getPhoneNumber(),
                parent.getUser() != null ? parent.getUser().getUsername() : null,
                studentNames
        );
    }
}
