package com.educonnect.service;

import com.educonnect.dto.ClassroomDTO;
import com.educonnect.model.Announcement;
import com.educonnect.model.Classroom;
import com.educonnect.model.Student;
import com.educonnect.model.Teacher;
import com.educonnect.model.User;
import com.educonnect.repository.AnnouncementRepository;
import com.educonnect.repository.ClassroomRepository;
import com.educonnect.repository.StudentRepository;
import com.educonnect.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final AnnouncementRepository announcementRepository;
    private final TeacherRepository teacherRepository;
    private final UserService userService; // 🚀 EKLENDİ: Aktif müdürü bulmak için

    public ClassroomService(ClassroomRepository classroomRepository,
                            StudentRepository studentRepository,
                            AnnouncementRepository announcementRepository,
                            TeacherRepository teacherRepository,
                            UserService userService) {
        this.classroomRepository = classroomRepository;
        this.studentRepository = studentRepository;
        this.announcementRepository = announcementRepository;
        this.teacherRepository = teacherRepository;
        this.userService = userService;
    }

    public Classroom createClassroom(Classroom classroom) {
        User admin = userService.getCurrentUser(); // 🚀 Giriş yapan müdürü bul

        classroom.setSchool(admin.getSchool()); // 🚀 GÜVENLİK KİLİDİ: Sınıfı müdürün okuluna mühürle!

        return classroomRepository.save(classroom);
    }

    public List<ClassroomDTO> getAllClassrooms() {
        User admin = userService.getCurrentUser(); // 🚀 Giriş yapan müdürü bul

        // 🚀 GÜVENLİK KİLİDİ: Sadece bu okula ait sınıfları listele!
        return classroomRepository.findBySchool(admin.getSchool()).stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public Classroom addStudentToClass(Long classId, Long studentId) {
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Sınıf bulunamadı"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Öğrenci bulunamadı"));

        classroom.getStudents().add(student);
        return classroomRepository.save(classroom);
    }

    public Classroom addAnnouncementToClass(Long classId, Long announcementId) {
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Sınıf bulunamadı"));

        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Duyuru bulunamadı"));

        classroom.getAnnouncements().add(announcement);
        return classroomRepository.save(classroom);
    }

    public Classroom assignTeacherToClassroom(Long classId, Long teacherId) {
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Sınıf bulunamadı!"));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Öğretmen bulunamadı!"));

        classroom.setHomeroomTeacher(teacher);
        return classroomRepository.save(classroom);
    }

    private ClassroomDTO convertToDTO(Classroom classroom) {
        String teacherName = (classroom.getHomeroomTeacher() != null)
                ? classroom.getHomeroomTeacher().getFirstName() + " " + classroom.getHomeroomTeacher().getLastName()
                : "Rehber Öğretmen Atanmadı";

        List<String> studentNames = classroom.getStudents() != null
                ? classroom.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName()).collect(java.util.stream.Collectors.toList())
                : List.of();

        return new ClassroomDTO(
                classroom.getId(),
                classroom.getName(),
                classroom.getGradeLevel(),
                teacherName,
                studentNames
        );
    }

    public void deleteClassroom(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Sınıf bulunamadı!"));

        classroom.setHomeroomTeacher(null);
        classroomRepository.save(classroom);

        List<Student> students = studentRepository.findByClassroom(classroom);
        for (Student s : students) {
            s.setClassroom(null);
            studentRepository.save(s);
        }

        classroomRepository.delete(classroom);
    }

    public void updateClassroom(Long id, Classroom updatedClassroom, Long teacherId) {
        Classroom existing = classroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sınıf bulunamadı!"));

        existing.setName(updatedClassroom.getName());
        existing.setGradeLevel(updatedClassroom.getGradeLevel());

        if (teacherId != null) {
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new RuntimeException("Öğretmen bulunamadı!"));
            existing.setHomeroomTeacher(teacher);
        } else {
            existing.setHomeroomTeacher(null);
        }

        classroomRepository.save(existing);
    }
}