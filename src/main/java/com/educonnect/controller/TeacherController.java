package com.educonnect.controller;

import com.educonnect.dto.TeacherDTO;
import com.educonnect.dto.TeacherRequest;
import com.educonnect.model.Teacher;
import com.educonnect.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PostMapping
    public Teacher createTeacher(@Valid @RequestBody TeacherRequest request) {
        return teacherService.createTeacherWithUser(request);
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

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
        teacherService.updateTeacher(id, request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PostMapping("/complete-profile/{username}")
    public Teacher completeProfile(@PathVariable String username, @RequestBody Teacher teacherProfile) {
        return teacherService.createProfileForExistingUser(username, teacherProfile);
    }
}