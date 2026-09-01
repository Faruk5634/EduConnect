package com.educonnect.service;

import com.educonnect.dto.CreateStudentRequest;
import com.educonnect.dto.StudentDTO;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.mapper.StudentMapper;
import com.educonnect.model.Classroom;
import com.educonnect.model.Parent;
import com.educonnect.model.Role;
import com.educonnect.model.School;
import com.educonnect.model.Student;
import com.educonnect.repository.ClassroomRepository;
import com.educonnect.repository.ParentRepository;
import com.educonnect.repository.StudentRepository;
import com.educonnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;
import com.educonnect.model.User;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClassroomRepository classroomRepository;
    private final UserService userService;
    private final UserProvisioningService userProvisioningService; // 🚀 replaces duplicated account-creation logic

    private School getTenantSchool(User user) {
        if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
            return null;
        }
        if (user.getSchool() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kullanıcının atanmış bir okulu yok.");
        }
        return user.getSchool();
    }

    private void assertStudentBelongsToTenant(Student student, School tenantSchool) {
        if (tenantSchool == null) {
            return;
        }
        if (student.getSchool() == null || !tenantSchool.getId().equals(student.getSchool().getId())) {
            throw new ResourceNotFoundException("Öğrenci bulunamadı");
        }
    }

    private void assertParentBelongsToTenant(Parent parent, School tenantSchool) {
        if (tenantSchool == null) {
            return;
        }
        if (parent.getUser() == null || parent.getUser().getSchool() == null || !tenantSchool.getId().equals(parent.getUser().getSchool().getId())) {
            throw new ResourceNotFoundException("Veli bulunamadı");
        }
    }

    private void assertClassroomBelongsToTenant(Classroom classroom, School tenantSchool) {
        if (tenantSchool == null) {
            return;
        }
        if (classroom.getSchool() == null || !tenantSchool.getId().equals(classroom.getSchool().getId())) {
            throw new ResourceNotFoundException("Sınıf bulunamadı");
        }
    }

    public String createStudentWithUser(CreateStudentRequest request) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);

        // Students, unlike parents/teachers, only get a login account if one
        // was explicitly requested (some students don't log in themselves).
        User savedUser = null;
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            savedUser = userProvisioningService.provisionUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhone(),
                    request.getEmail(),
                    Role.ROLE_STUDENT,
                    tenantSchool
            );
        }

        Parent parent = (request.getParentId() != null) ? parentRepository.findById(request.getParentId()).orElse(null) : null;
        if (parent != null) {
            assertParentBelongsToTenant(parent, tenantSchool);
        }
        Classroom classroom = (request.getGrade() != null && !request.getGrade().isEmpty())
                ? (tenantSchool != null
                    ? classroomRepository.findByNameAndSchool(request.getGrade(), tenantSchool).orElse(null)
                    : classroomRepository.findByNameAndSchool(request.getGrade(), admin.getSchool()).orElse(null))
                : null;
        if (classroom != null) {
            assertClassroomBelongsToTenant(classroom, tenantSchool);
        }

        Student student = Student.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .schoolNumber(request.getSchoolNumber())
                .grade(request.getGrade())
                .user(savedUser)
                .parent(parent)
                .classroom(classroom)
                .school(tenantSchool != null ? tenantSchool : admin.getSchool())
                .gender(request.getGender() != null ? request.getGender() : "Belirtilmemiş")
                .build();

        studentRepository.save(student);
        return "Öğrenci başarıyla eklendi";
    }

    public Student createStudent(Student student) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        if (tenantSchool != null) {
            student.setSchool(tenantSchool);
        }
        return studentRepository.save(student);
    }

    public List<StudentDTO> getAllStudents() {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        List<Student> students = tenantSchool != null
                ? studentRepository.findBySchool(tenantSchool)
                : studentRepository.findAll();
        return students
                .stream()
                .map(StudentMapper::toDto)
                .collect(Collectors.toList());
    }

    public Student assignParent(Long studentId, Long parentId) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı"));
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Veli bulunamadı"));

        assertStudentBelongsToTenant(student, tenantSchool);
        assertParentBelongsToTenant(parent, tenantSchool);
        student.setParent(parent);
        return studentRepository.save(student);
    }

    public Student getStudentBySchoolNumber(String schoolNumber) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        return tenantSchool != null
                ? studentRepository.findBySchoolNumberAndSchool(schoolNumber, tenantSchool)
                .orElseThrow(() -> new ResourceNotFoundException("Bu okul numarasına ait bir öğrenci bulunamadı!"))
                : studentRepository.findBySchoolNumber(schoolNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bu okul numarasına ait bir öğrenci bulunamadı!"));
    }

    public List<StudentDTO> searchStudentsByFirstName(String firstName) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        return (tenantSchool != null
                ? studentRepository.findBySchoolAndFirstNameContainingIgnoreCase(tenantSchool, firstName)
                : studentRepository.findByFirstNameContainingIgnoreCase(firstName))
                .stream()
                .map(StudentMapper::toDto)
                .collect(Collectors.toList());
    }

    public Page<StudentDTO> getStudentsPaginated(int page, int size) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        Pageable pageable = PageRequest.of(page, size);
        return tenantSchool != null
                ? studentRepository.findBySchool(tenantSchool, pageable).map(StudentMapper::toDto)
                : studentRepository.findAll(pageable).map(StudentMapper::toDto);
    }

    public void deleteStudent(Long id) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı!"));
        assertStudentBelongsToTenant(student, tenantSchool);
        studentRepository.delete(student);
    }

    public void updateStudent(Long id, CreateStudentRequest request) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);

        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı!"));
        assertStudentBelongsToTenant(existingStudent, tenantSchool);

        existingStudent.setSchoolNumber(request.getSchoolNumber());
        existingStudent.setFirstName(request.getFirstName());
        existingStudent.setLastName(request.getLastName());
        existingStudent.setGrade(request.getGrade());

        if (request.getGender() != null) {
            existingStudent.setGender(request.getGender());
        }

        if (request.getGrade() != null && !request.getGrade().isEmpty()) {
            Classroom classroom = tenantSchool != null
                    ? classroomRepository.findByNameAndSchool(request.getGrade(), tenantSchool).orElse(null)
                    : classroomRepository.findByNameAndSchool(request.getGrade(), admin.getSchool()).orElse(null);
            existingStudent.setClassroom(classroom);
        } else {
            existingStudent.setClassroom(null);
        }

        if (request.getParentId() != null) {
            Parent parent = parentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Veli bulunamadı!"));
            assertParentBelongsToTenant(parent, tenantSchool);
            existingStudent.setParent(parent);
        } else {
            existingStudent.setParent(null);
        }

        if (existingStudent.getUser() != null) {
            User user = existingStudent.getUser();

            if (request.getUsername() != null && !request.getUsername().isEmpty()) {
                user.setUsername(request.getUsername());
            }
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());

            userRepository.save(user);
        }

        studentRepository.save(existingStudent);
    }

    public Student createProfileForExistingUser(String username, Student studentProfile) {
        User existingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));

        if (existingUser.getStudent() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu hesabın zaten bir öğrenci profili var!");
        }

        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        if (tenantSchool != null && (existingUser.getSchool() == null || !tenantSchool.getId().equals(existingUser.getSchool().getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kullanıcı farklı bir okula ait.");
        }

        studentProfile.setUser(existingUser);
        if (tenantSchool != null) {
            studentProfile.setSchool(tenantSchool);
        }
        return studentRepository.save(studentProfile);
    }

    public StudentDTO getMyProfile() {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getStudent() == null) {
            throw new ResourceNotFoundException("Öğrenci profili bulunamadı!");
        }
        return StudentMapper.toDto(currentUser.getStudent());
    }
}
