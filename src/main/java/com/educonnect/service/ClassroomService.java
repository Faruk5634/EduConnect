package com.educonnect.service;

import com.educonnect.dto.ClassroomDTO;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.mapper.ClassroomMapper;
import com.educonnect.model.Announcement;
import com.educonnect.model.Classroom;
import com.educonnect.model.Student;
import com.educonnect.model.Teacher;
import com.educonnect.model.School;
import com.educonnect.repository.AnnouncementRepository;
import com.educonnect.repository.ClassroomRepository;
import com.educonnect.repository.StudentRepository;
import com.educonnect.repository.TeacherRepository;
import com.educonnect.repository.SchoolRepository;
import com.educonnect.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final AnnouncementRepository announcementRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;

    private School getCurrentSchool() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) return null;
        return schoolRepository.getReferenceById(tenantId);
    }

    public Classroom createClassroom(Classroom classroom) {
        School tenantSchool = getCurrentSchool();
        classroom.setSchool(tenantSchool);
        return classroomRepository.save(classroom);
    }

    public List<ClassroomDTO> getAllClassrooms() {
        return classroomRepository.findAll().stream()
                .map(ClassroomMapper::toDto)
                .collect(Collectors.toList());
    }

    public Classroom addStudentToClass(Long classId, Long studentId) {
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Sınıf bulunamadı!"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı"));

        classroom.getStudents().add(student);
        return classroomRepository.save(classroom);
    }

    /**
     * 🐛 BUG FIX: Classroom.announcements is now the INVERSE (mappedBy) side
     * of the Announcement<->Classroom many-to-many. JPA only persists
     * changes made on the OWNING side (Announcement.classrooms) — adding to
     * the inverse side's collection and saving the Classroom, as this used
     * to do, silently did nothing to the actual join table. We now add the
     * classroom to the announcement's own collection and save the
     * announcement instead.
     */
    public Classroom addAnnouncementToClass(Long classId, Long announcementId) {
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Sınıf bulunamadı!"));
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Duyuru bulunamadı"));

        announcement.getClassrooms().add(classroom);
        announcementRepository.save(announcement);

        return classroom;
    }

    public Classroom assignTeacherToClassroom(Long classId, Long teacherId) {
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Sınıf bulunamadı!"));
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğretmen bulunamadı!"));

        classroom.setHomeroomTeacher(teacher);
        return classroomRepository.save(classroom);
    }

    public void deleteClassroom(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sınıf bulunamadı!"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Sınıf bulunamadı!"));

        existing.setName(updatedClassroom.getName());
        existing.setGradeLevel(updatedClassroom.getGradeLevel());

        if (teacherId != null) {
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Öğretmen bulunamadı!"));
            existing.setHomeroomTeacher(teacher);
        } else {
            existing.setHomeroomTeacher(null);
        }

        classroomRepository.save(existing);
    }
}
