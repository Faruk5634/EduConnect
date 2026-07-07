package com.educonnect.controller;

import com.educonnect.dto.SchoolStatsDTO;
import com.educonnect.model.*;
import com.educonnect.repository.*;
import com.educonnect.service.SchoolService;
import com.educonnect.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/school")
@CrossOrigin(origins = "http://localhost:5173")
public class SchoolController {

    private final SchoolService schoolService;
    private final UserService userService;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final ParentRepository parentRepository;
    private final AnnouncementRepository announcementRepository;

    // 🚀 Tüm parçaları (dependencies) buraya ekledik, artık kırmızı hata almayacaksın
    public SchoolController(SchoolService schoolService,
                            UserService userService,
                            StudentRepository studentRepository,
                            TeacherRepository teacherRepository,
                            ClassroomRepository classroomRepository,
                            ParentRepository parentRepository,
                            AnnouncementRepository announcementRepository) {
        this.schoolService = schoolService;
        this.userService = userService;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.classroomRepository = classroomRepository;
        this.parentRepository = parentRepository;
        this.announcementRepository = announcementRepository;
    }

    @GetMapping("/stats")
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