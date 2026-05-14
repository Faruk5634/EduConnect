package com.educonnect.controller;

import com.educonnect.model.Student;
import com.educonnect.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService; // Artık depo (Repository) yok, Servis var!

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        return studentService.createStudent(student); // İşi servise pasla
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.getAllStudents(); // İşi servise pasla
    }

    @PutMapping("/{studentId}/parent/{parentId}")
    public Student assignParentToStudent(@PathVariable Long studentId, @PathVariable Long parentId) {
        return studentService.assignParent(studentId, parentId); // İşi servise pasla
    }
}
