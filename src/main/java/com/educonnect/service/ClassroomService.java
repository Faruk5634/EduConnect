package com.educonnect.service;

import com.educonnect.dto.ClassroomDTO;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.mapper.ClassroomMapper;
import com.educonnect.model.Announcement;
import com.educonnect.model.Classroom;
import com.educonnect.model.Student;
import com.educonnect.model.Teacher;
import com.educonnect.model.School;
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

    private School getTenantSchool(User user) {
        if (user.getRole() == com.educonnect.model.Role.ROLE_SUPER_ADMIN) {
            return null;
        }
        if (user.getSchool() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Bu kullanıcının atanmış bir okulu yok.");
        }
        return user.getSchool();
    }

    private Classroom getClassroomOrThrow(Long id, School tenantSchool) {
        return tenantSchool != null
                ? classroomRepository.findByIdAndSchool(id, tenantSchool).orElseThrow(() -> new ResourceNotFoundException("Sınıf bulunamadı!"))
                : classroomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sınıf bulunamadı!"));
    }

    public Classroom createClassroom(Classroom classroom) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        classroom.setSchool(tenantSchool != null ? tenantSchool : admin.getSchool());
        return classroomRepository.save(classroom);
    }

    public List<ClassroomDTO> getAllClassrooms() {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        return (tenantSchool != null ? classroomRepository.findBySchool(tenantSchool) : classroomRepository.findAll())
                .stream()
                .map(ClassroomMapper::toDto)
                .collect(Collectors.toList());
    }

    public Classroom addStudentToClass(Long classId, Long studentId) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        Classroom classroom = getClassroomOrThrow(classId, tenantSchool);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı"));
        if (tenantSchool != null && (student.getSchool() == null || !tenantSchool.getId().equals(student.getSchool().getId()))) {
            throw new ResourceNotFoundException("Öğrenci bulunamadı");
        }

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
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        Classroom classroom = getClassroomOrThrow(classId, tenantSchool);
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Duyuru bulunamadı"));
        if (tenantSchool != null && (announcement.getSchool() == null || !tenantSchool.getId().equals(announcement.getSchool().getId()))) {
            throw new ResourceNotFoundException("Duyuru bulunamadı");
        }

        announcement.getClassrooms().add(classroom);
        announcementRepository.save(announcement);

        return classroom;
    }

    public Classroom assignTeacherToClassroom(Long classId, Long teacherId) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        Classroom classroom = getClassroomOrThrow(classId, tenantSchool);
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğretmen bulunamadı!"));
        if (tenantSchool != null && (teacher.getUser() == null || teacher.getUser().getSchool() == null || !tenantSchool.getId().equals(teacher.getUser().getSchool().getId()))) {
            throw new ResourceNotFoundException("Öğretmen bulunamadı!");
        }

        classroom.setHomeroomTeacher(teacher);
        return classroomRepository.save(classroom);
    }

    public void deleteClassroom(Long id) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        Classroom classroom = getClassroomOrThrow(id, tenantSchool);

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
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        Classroom existing = getClassroomOrThrow(id, tenantSchool);

        existing.setName(updatedClassroom.getName());
        existing.setGradeLevel(updatedClassroom.getGradeLevel());

        if (teacherId != null) {
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Öğretmen bulunamadı!"));
            if (tenantSchool != null && (teacher.getUser() == null || teacher.getUser().getSchool() == null || !tenantSchool.getId().equals(teacher.getUser().getSchool().getId()))) {
                throw new ResourceNotFoundException("Öğretmen bulunamadı!");
            }
            existing.setHomeroomTeacher(teacher);
        } else {
            existing.setHomeroomTeacher(null);
        }

        classroomRepository.save(existing);
    }
}
