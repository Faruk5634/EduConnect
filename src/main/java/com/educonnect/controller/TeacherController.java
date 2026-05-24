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

        // Yeni Öğretmen Kaydı (POST)
        @PostMapping
        public Teacher createTeacher(@RequestBody Teacher teacher) {
            return teacherService.createTeacher(teacher);
        }

        // Tüm Öğretmenleri Listeleme (GET)
        @GetMapping
        public List<TeacherDTO> getAllTeachers() {
            return teacherService.getAllTeachers();
        }

        // YENİ KÖPRÜ: Arama motoru -> http://localhost:8080/api/teachers/search?branch=Matematik
        @GetMapping("/search")
        public List<TeacherDTO> searchTeachers(@RequestParam String branch) {
            return teacherService.searchTeachersByBranch(branch);
        }

        @GetMapping("/me")
        public TeacherDTO getMyProfile(Principal principal) {
            // Principal, Spring Security'nin kapıdaki muhafızıdır.
            // Biletin üstündeki ismi (username) bize doğrudan verir.
            return teacherService.getTeacherProfileByUsername(principal.getName());
        }
    }
