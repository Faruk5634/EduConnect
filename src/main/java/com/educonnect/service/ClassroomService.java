package com.educonnect.service;

import com.educonnect.dto.ClassroomDTO;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.model.Announcement;
import com.educonnect.model.Classroom;
import com.educonnect.model.Student;
import com.educonnect.model.Teacher;
import com.educonnect.model.User;
import com.educonnect.repository.AnnouncementRepository;
import com.educonnect.repository.ClassroomRepository;
import com.educonnect.repository.StudentRepository;
import com.educonnect.repository.TeacherRepository;
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
    private final UserService userService;

    public Classroom createClassroom(Classroom classroom) {
        User admin = userService.getCurrentUser();
        classroom.setSchool(admin.getSchool());
        return classroomRepository.save(classroom);
    }

    public List<ClassroomDTO> getAllClassrooms() {
        User admin = userService.getCurrentUser();
        return classroomRepository.findBySchool(admin.getSchool()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Classroom addStudentToClass(Long classId, Long studentId) {
        Classroom classroom = getClassroomOrThrow(classId);
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
        Classroom classroom = getClassroomOrThrow(classId);
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Duyuru bulunamadı"));

        announcement.getClassrooms().add(classroom);
        announcementRepository.save(announcement);

        return classroom;
    }

    public Classroom assignTeacherToClassroom(Long classId, Long teacherId) {
        Classroom classroom = getClassroomOrThrow(classId);
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğretmen bulunamadı!"));

        classroom.setHomeroomTeacher(teacher);
        return classroomRepository.save(classroom);
    }

    private ClassroomDTO convertToDTO(Classroom classroom) {
        String teacherName = (classroom.getHomeroomTeacher() != null)
                ? classroom.getHomeroomTeacher().getFirstName() + " " + classroom.getHomeroomTeacher().getLastName()
                : "Rehber Öğretmen Atanmadı";

        List<String> studentNames = classroom.getStudents() != null
                ? classroom.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName()).collect(Collectors.toList())
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
        Classroom classroom = getClassroomOrThrow(id);

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
        Classroom existing = getClassroomOrThrow(id);

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

    private Classroom getClassroomOrThrow(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sınıf bulunamadı!"));
    }
}