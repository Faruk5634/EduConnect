package com.educonnect.service;

import com.educonnect.model.Announcement;
import com.educonnect.model.Classroom;
import com.educonnect.model.Student;
import com.educonnect.repository.AnnouncementRepository;
import com.educonnect.repository.ClassroomRepository;
import com.educonnect.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final AnnouncementRepository announcementRepository;

    public ClassroomService(ClassroomRepository classroomRepository,
                            StudentRepository studentRepository,
                            AnnouncementRepository announcementRepository) {
        this.classroomRepository = classroomRepository;
        this.studentRepository = studentRepository;
        this.announcementRepository = announcementRepository;
    }

    // Yeni Sınıf Ekleme
    public Classroom createClassroom(Classroom classroom) {
        return classroomRepository.save(classroom);
    }

    // Tüm Sınıfları Listeleme
    public List<Classroom> getAllClassrooms() {
        return classroomRepository.findAll();
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
}