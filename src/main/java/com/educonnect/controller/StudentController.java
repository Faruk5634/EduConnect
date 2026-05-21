package com.educonnect.controller;

import com.educonnect.dto.StudentDTO;
import com.educonnect.model.Student;
import com.educonnect.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService; // Artık depo (Repository) yok, Servis var!

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public Student createStudent(@Valid @RequestBody Student student) {
        return studentService.createStudent(student); // İşi servise pasla
    }

    @GetMapping
    public List<StudentDTO> getAllStudents() {
        return studentService.getAllStudents();
    }

    @PutMapping("/{studentId}/parent/{parentId}")
    public Student assignParentToStudent(@PathVariable Long studentId, @PathVariable Long parentId) {
        return studentService.assignParent(studentId, parentId); // İşi servise pasla
    }


    //  Okul numarasına göre öğrenci bulma iş mantığı
    @GetMapping("/number/{schoolNumber}")
    public Student getStudentBySchoolNumber(@PathVariable String schoolNumber) {
        return studentService.getStudentBySchoolNumber(schoolNumber);
    }

    //Öğrenci isminde geçen harflere göre arama yapma iş mantığı
    @GetMapping("/search")
    public List<StudentDTO> searchStudentsByFirstName(@RequestParam String firstName) {
        return studentService.searchStudentsByFirstName(firstName);
    }


    @GetMapping("/page")
    public Page<StudentDTO> getStudentsPaginated(
            @RequestParam(defaultValue = "0") int page, // Kullanıcı bir şey demezse 0. sayfa (İlk sayfa)
            @RequestParam(defaultValue = "10") int size) { // Kullanıcı bir şey demezse sayfada 10 kişi

        return studentService.getStudentsPaginated(page, size);
    }

}
