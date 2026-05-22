package com.educonnect.service;

import com.educonnect.dto.StudentDTO;
import com.educonnect.model.Parent;
import com.educonnect.model.Student;
import com.educonnect.repository.ParentRepository;
import com.educonnect.repository.StudentRepository;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;

    public StudentService(StudentRepository studentRepository, ParentRepository parentRepository) {
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Student assignParent(Long studentId, Long parentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı"));

        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Veli bulunamadı"));

        student.setParent(parent);
        return studentRepository.save(student);
    }

    public Student getStudentBySchoolNumber(String schoolNumber) {
        return studentRepository.findBySchoolNumber(schoolNumber)
                .orElseThrow(() -> new RuntimeException("Bu okul numarasına ait bir öğrenci bulunamadı!"));
    }

    public List<StudentDTO> searchStudentsByFirstName(String firstName) {
        return studentRepository.findByFirstNameContainingIgnoreCase(firstName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 🚀 GÜNCELLENEN AŞÇI YAMAĞI
    private StudentDTO convertToDTO(Student student) {
        String parentName = (student.getParent() != null)
                ? student.getParent().getFirstName() + " " + student.getParent().getLastName()
                : "Veli Atanmadı";

        // 🚀 YENİ: Velinin ID'sini güvenli bir şekilde alıyoruz
        Long parentId = (student.getParent() != null)
                ? student.getParent().getId()
                : null;

        return new StudentDTO(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getSchoolNumber(),
                parentName,
                parentId // 🚀 YENİ: Constructor'a ID'yi de gönderdik
        );
    }

    public Page<StudentDTO> getStudentsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return studentRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    public void updateStudent(Long id, Student updatedStudent) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı!"));

        existingStudent.setSchoolNumber(updatedStudent.getSchoolNumber());
        existingStudent.setFirstName(updatedStudent.getFirstName());
        existingStudent.setLastName(updatedStudent.getLastName());
        existingStudent.setParent(updatedStudent.getParent());

        studentRepository.save(existingStudent);
    }
}