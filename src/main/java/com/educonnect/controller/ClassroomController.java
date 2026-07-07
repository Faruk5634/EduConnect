package com.educonnect.controller;

import com.educonnect.dto.ClassroomDTO;
import com.educonnect.model.Classroom;
import com.educonnect.service.ClassroomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@CrossOrigin(origins = "http://localhost:5173")
public class ClassroomController {

    private final ClassroomService classroomService;

    // 🚀 Controller artık sadece Servis ile muhatap, tertemiz oldu!
    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @PostMapping
    public Classroom createClassroom(@Valid @RequestBody Classroom classroom) {
        return classroomService.createClassroom(classroom);
    }

    @GetMapping
    public List<ClassroomDTO> getAllClassrooms() {
        // Servisteki getAllClassrooms zaten sadece o okulun sınıflarını döndürüyor!
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

    @PutMapping("/{classId}/teacher/{teacherId}")
    public Classroom assignTeacherToClassroom(@PathVariable Long classId, @PathVariable Long teacherId) {
        return classroomService.assignTeacherToClassroom(classId, teacherId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateClassroom(
            @PathVariable Long id,
            @RequestBody Classroom classroom,
            @RequestParam(required = false) Long teacherId) {

        classroomService.updateClassroom(id, classroom, teacherId);
        return ResponseEntity.ok().build();
    }

    // 🚀 Frontend'in bozulmaması için bu köprüyü tuttuk ama artık getAllClassrooms ile aynı işi çok daha güvenli yapıyor.
    @GetMapping("/school")
    public ResponseEntity<List<ClassroomDTO>> getClassroomsBySchool() {
        return ResponseEntity.ok(classroomService.getAllClassrooms());
    }
}