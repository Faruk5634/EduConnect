package com.educonnect.controller;

import com.educonnect.dto.CreateStudentRequest;
import com.educonnect.dto.StudentDTO;
import com.educonnect.model.Student;
import com.educonnect.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PostMapping
    public Student createStudent(@Valid @RequestBody Student student) {
        return studentService.createStudent(student);
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PostMapping("/create")
    public String createStudentWithUser(@Valid @RequestBody CreateStudentRequest request) {
        return studentService.createStudentWithUser(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN','TEACHER')")
    @GetMapping("/list")
    public List<StudentDTO> getAllStudents() {
        return studentService.getAllStudents();
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PutMapping("/{studentId}/parent/{parentId}")
    public Student assignParentToStudent(@PathVariable Long studentId, @PathVariable Long parentId) {
        return studentService.assignParent(studentId, parentId);
    }

    @GetMapping("/number/{schoolNumber}")
    public Student getStudentBySchoolNumber(@PathVariable String schoolNumber) {
        return studentService.getStudentBySchoolNumber(schoolNumber);
    }

    @GetMapping("/search")
    public List<StudentDTO> searchStudentsByFirstName(@RequestParam String firstName) {
        return studentService.searchStudentsByFirstName(firstName);
    }

    @GetMapping("/page")
    public Page<StudentDTO> getStudentsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return studentService.getStudentsPaginated(page, size);
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @Valid @RequestBody CreateStudentRequest request) {
        studentService.updateStudent(id, request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PostMapping("/complete-profile/{username}")
    public Student completeProfile(@PathVariable String username, @RequestBody Student studentProfile) {
        return studentService.createProfileForExistingUser(username, studentProfile);
    }

    @GetMapping("/me")
    public StudentDTO getMyProfile() {
        return studentService.getMyProfile();
    }
}