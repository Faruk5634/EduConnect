package com.educonnect.service;

import com.educonnect.dto.ClassroomDTO;
import com.educonnect.model.Announcement;
import com.educonnect.model.Classroom;
import com.educonnect.model.Student;
import com.educonnect.model.Teacher; // YENİ EKLENDİ
import com.educonnect.repository.AnnouncementRepository;
import com.educonnect.repository.ClassroomRepository;
import com.educonnect.repository.StudentRepository;
import com.educonnect.repository.TeacherRepository; // YENİ EKLENDİ
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final AnnouncementRepository announcementRepository;
    private final TeacherRepository teacherRepository; // YENİ EKLENDİ

    // Constructor güncellendi (TeacherRepository eklendi)
    public ClassroomService(ClassroomRepository classroomRepository,
                            StudentRepository studentRepository,
                            AnnouncementRepository announcementRepository,
                            TeacherRepository teacherRepository) {
        this.classroomRepository = classroomRepository;
        this.studentRepository = studentRepository;
        this.announcementRepository = announcementRepository;
        this.teacherRepository = teacherRepository;
    }

    // Yeni Sınıf Ekleme
    public Classroom createClassroom(Classroom classroom) {
        return classroomRepository.save(classroom);
    }

    public List<ClassroomDTO> getAllClassrooms() {
        return classroomRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    // Sınıfa Öğrenci Ekleme İş Mantığı
    public Classroom addStudentToClass(Long classId, Long studentId) {
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Sınıf bulunamadı"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı"));

        classroom.getStudents().add(student);
        return classroomRepository.save(classroom);
    }

    // Sınıfa Duyuru Asma İş Mantığı
    public Classroom addAnnouncementToClass(Long classId, Long announcementId) {
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Sınıf bulunamadı"));

        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("Duyuru bulunamadı"));

        classroom.getAnnouncements().add(announcement);
        return classroomRepository.save(classroom);
    }

    // YENİ SİHİRLİ METODUMUZ: Sınıfa Rehber Öğretmen Atama
    public Classroom assignTeacherToClassroom(Long classId, Long teacherId) {
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Sınıf bulunamadı!"));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Öğretmen bulunamadı!"));

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
}