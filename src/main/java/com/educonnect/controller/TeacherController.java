package com.educonnect.controller;

import com.educonnect.dto.TeacherDTO;
import com.educonnect.model.Teacher;
import com.educonnect.service.TeacherService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor // 🚀 MİMARİ DOKUNUŞ: Manuel constructor silindi!
public class TeacherController {

    private final TeacherService teacherService;

    // 🚀 DOKUNUŞ: Tüm manuel get/set metotları @Data ile tek satıra indirildi
    @Data
    public static class TeacherRequest {
        private String firstName;
        private String lastName;
        private String branch;
        private String username;
        private String password;
        private String phone;
        private String email;
    }

    @PostMapping
    public Teacher createTeacher(@RequestBody TeacherRequest request) {
        Teacher teacher = new Teacher();
        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setBranch(request.getBranch());
        teacher.setUsername(request.getUsername());
        teacher.setPassword(request.getPassword());
        teacher.setPhone(request.getPhone());
        teacher.setEmail(request.getEmail());
        return teacherService.createTeacherWithUser(teacher);
    }

    @GetMapping
    public List<TeacherDTO> getAllTeachers() {
        return teacherService.getAllTeachers();
    }

    @GetMapping("/search")
    public List<TeacherDTO> searchTeachers(@RequestParam String branch) {
        return teacherService.searchTeachersByBranch(branch);
    }

    @GetMapping("/me")
    public TeacherDTO getMyProfile(Principal principal) {
        return teacherService.getTeacherProfileByUsername(principal.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, @RequestBody TeacherRequest request) {
        Teacher teacher = new Teacher();
        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setBranch(request.getBranch());
        teacher.setUsername(request.getUsername());
        teacher.setPassword(request.getPassword());
        teacher.setPhone(request.getPhone());
        teacher.setEmail(request.getEmail());
        teacherService.updateTeacher(id, teacher);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/complete-profile/{username}")
    public Teacher completeProfile(@PathVariable String username, @RequestBody Teacher teacherProfile) {
        return teacherService.createProfileForExistingUser(username, teacherProfile);
    }
}