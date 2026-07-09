package com.educonnect.controller;

import com.educonnect.dto.TeacherDTO;
import com.educonnect.model.Teacher;
import com.educonnect.service.TeacherService;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService=teacherService;
    }

    public static class TeacherRequest {
        private String firstName;
        private String lastName;
        private String branch;
        private String username;
        private String password;
        private String phone; // 🚀 EKLENDİ
        private String email; // 🚀 EKLENDİ

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    @PostMapping
    public Teacher createTeacher(@RequestBody TeacherRequest request) {
        Teacher teacher = new Teacher();
        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setBranch(request.getBranch());
        teacher.setUsername(request.getUsername());
        teacher.setPassword(request.getPassword());
        teacher.setPhone(request.getPhone()); // 🚀 EKLENDİ
        teacher.setEmail(request.getEmail()); // 🚀 EKLENDİ
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
    public org.springframework.http.ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public org.springframework.http.ResponseEntity<?> updateTeacher(@PathVariable Long id, @RequestBody TeacherRequest request) {
        Teacher teacher = new Teacher();
        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setBranch(request.getBranch());
        teacher.setUsername(request.getUsername());
        teacher.setPassword(request.getPassword());
        teacher.setPhone(request.getPhone()); // 🚀 EKLENDİ
        teacher.setEmail(request.getEmail()); // 🚀 EKLENDİ
        teacherService.updateTeacher(id, teacher);
        return org.springframework.http.ResponseEntity.ok().build();
    }

    @PostMapping("/complete-profile/{username}")
    public Teacher completeProfile(@PathVariable String username, @RequestBody Teacher teacherProfile) {
        return teacherService.createProfileForExistingUser(username, teacherProfile);
    }
}