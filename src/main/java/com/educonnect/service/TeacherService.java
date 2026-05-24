package com.educonnect.service;
import com.educonnect.dto.TeacherDTO;
import com.educonnect.model.Classroom;
import com.educonnect.model.Teacher;
import com.educonnect.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Service
public class TeacherService {
    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public Teacher createTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    public List<TeacherDTO> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    // TeacherService sınıfının içine eklenecek dönüşüm mantığı:
    private TeacherDTO convertToDTO(Teacher teacher) {
        List<TeacherDTO.ClassroomInfo> classInfos = teacher.getHomeroomClasses() != null
                ? teacher.getHomeroomClasses().stream()
                  .map(c -> new TeacherDTO.ClassroomInfo(c.getId(), c.getName()))
                  .collect(java.util.stream.Collectors.toList())
                : List.of();

        return new TeacherDTO(
                teacher.getId(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getBranch(),
                classInfos // List<String> yerine yeni listemizi verdik
        );
    }

    // Branşa Göre Akıllı Arama Mantığı
    public List<TeacherDTO> searchTeachersByBranch(String branch) {
        return teacherRepository.findByBranchContainingIgnoreCase(branch)
                .stream()
                .map(this::convertToDTO) // Çiğ veriyi şık DTO tabağına alıyoruz
                .collect(java.util.stream.Collectors.toList());
    }

    public TeacherDTO getTeacherProfileByUsername(String username) {
        Teacher teacher = teacherRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Eyvah! Bu kullanıcıya ait bir öğretmen profili bulunamadı."));

        return convertToDTO(teacher);
    }


}
