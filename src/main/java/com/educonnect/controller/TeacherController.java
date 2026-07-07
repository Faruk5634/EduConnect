package com.educonnect.controller;

import com.educonnect.dto.TeacherDTO;
import com.educonnect.model.Teacher;
import com.educonnect.service.TeacherService;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // 🚀 CORS Kalkanı eklendi
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService=teacherService;
    }

    @PostMapping
    public Teacher createTeacher(@RequestBody Teacher teacher) {
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
    public org.springframework.http.ResponseEntity<?> updateTeacher(@PathVariable Long id, @RequestBody Teacher updatedTeacher) {
        teacherService.updateTeacher(id, updatedTeacher);
        return org.springframework.http.ResponseEntity.ok().build();
    }

    @PostMapping("/complete-profile/{username}")
    public Teacher completeProfile(@PathVariable String username, @RequestBody Teacher teacherProfile) {
        return teacherService.createProfileForExistingUser(username, teacherProfile);
    }
}