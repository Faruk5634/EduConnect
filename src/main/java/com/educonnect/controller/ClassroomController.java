package com.educonnect.controller;

import com.educonnect.dto.ClassroomDTO;
import com.educonnect.model.Classroom;
import com.educonnect.service.ClassroomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PostMapping
    public Classroom createClassroom(@Valid @RequestBody Classroom classroom) {
        return classroomService.createClassroom(classroom);
    }

    @GetMapping
    public List<ClassroomDTO> getAllClassrooms() {
        return classroomService.getAllClassrooms();
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PostMapping("/{classId}/students/{studentId}")
    public Classroom addStudentToClass(@PathVariable Long classId, @PathVariable Long studentId) {
        return classroomService.addStudentToClass(classId, studentId);
    }

    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PostMapping("/{classId}/announcements/{announcementId}")
    public Classroom addAnnouncementToClass(@PathVariable Long classId, @PathVariable Long announcementId) {
        return classroomService.addAnnouncementToClass(classId, announcementId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PutMapping("/{classId}/teacher/{teacherId}")
    public Classroom assignTeacherToClassroom(@PathVariable Long classId, @PathVariable Long teacherId) {
        return classroomService.assignTeacherToClassroom(classId, teacherId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClassroom(
            @PathVariable Long id,
            @RequestBody Classroom classroom,
            @RequestParam(required = false) Long teacherId) {

        classroomService.updateClassroom(id, classroom, teacherId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/school")
    public ResponseEntity<List<ClassroomDTO>> getClassroomsBySchool() {
        return ResponseEntity.ok(classroomService.getAllClassrooms());
    }
}