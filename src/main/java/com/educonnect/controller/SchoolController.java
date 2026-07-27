package com.educonnect.controller;

import com.educonnect.dto.SchoolStatsDTO;
import com.educonnect.model.School;
import com.educonnect.model.User;
import com.educonnect.repository.*;
import com.educonnect.service.SchoolService;
import com.educonnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor // 🚀 MİMARİ DOKUNUŞ: 7 tane bağımlılığı tek satırda bağladık!
public class SchoolController {

    private final SchoolService schoolService;
    private final UserService userService;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final ParentRepository parentRepository;
    private final AnnouncementRepository announcementRepository;

    @GetMapping("/schools")
    public List<School> getAllSchools() {
        return schoolService.getAllSchools();
    }

    @PostMapping("/schools")
    public School createSchool(@RequestBody School school) {
        return schoolService.createSchool(school);
    }

    @PutMapping("/schools/{id}")
    public School updateSchool(@PathVariable Long id, @RequestBody School school) {
        return schoolService.updateSchool(id, school);
    }

    @DeleteMapping("/schools/{id}")
    public ResponseEntity<Void> deleteSchool(@PathVariable Long id) {
        schoolService.deleteSchool(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/school/stats")
    public ResponseEntity<?> getSchoolStats() {
        try {
            User user = userService.getCurrentUser();
            School school = user.getSchool();

            if (school == null) return ResponseEntity.ok(new SchoolStatsDTO(0, 0, 0, 0, 0));

            return ResponseEntity.ok(new SchoolStatsDTO(
                    studentRepository.countBySchool(school),
                    teacherRepository.countBySchool(school),
                    classroomRepository.countBySchool(school),
                    parentRepository.countBySchool(school),
                    announcementRepository.countBySchool(school)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Hata: " + e.getMessage());
        }
    }
}