package com.educonnect.controller;

import com.educonnect.model.Classroom;
import com.educonnect.service.ClassroomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;

    // Artık sadece servisi tanıyor
    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @PostMapping
    public Classroom createClassroom(@RequestBody Classroom classroom) {
        return classroomService.createClassroom(classroom);
    }

    @GetMapping
    public List<Classroom> getAllClassrooms() {
        return classroomService.getAllClassrooms();
    }

    @PostMapping("/{classId}/students/{studentId}")
    public Classroom addStudentToClass(@PathVariable Long classId, @PathVariable Long studentId) {
        return classroomService.addStudentToClass(classId, studentId);
    }

    @PostMapping("/{classId}/announcements/{announcementId}")
    public Classroom addAnnouncementToClass(@PathVariable Long classId, @PathVariable Long announcementId) {
        return classroomService.addAnnouncementToClass(classId, announcementId);
    }
}
