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
import com.educonnect.repository.SchoolRepository;
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
import com.educonnect.security.TenantContext;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final ParentRepository parentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClassroomRepository classroomRepository;
    private final UserService userService;
    private final UserProvisioningService userProvisioningService;

    private School getCurrentSchool() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) return null;
        return schoolRepository.getReferenceById(tenantId);
    }

    public String createStudentWithUser(CreateStudentRequest request) {
        School tenantSchool = getCurrentSchool();
        User savedUser = null;
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            savedUser = userProvisioningService.provisionUser(
                    request.getUsername(), request.getPassword(),
                    request.getFirstName(), request.getLastName(),
                    request.getPhone(), request.getEmail(),
                    Role.ROLE_STUDENT, tenantSchool
            );
        }

        Parent parent = (request.getParentId() != null) ? parentRepository.findById(request.getParentId()).orElse(null) : null;
        
        Classroom classroom = null;
        if (request.getGrade() != null && !request.getGrade().isEmpty()) {
            if (tenantSchool != null) {
                classroom = classroomRepository.findByNameAndSchool(request.getGrade(), tenantSchool).orElse(null);
            } else {
                classroom = classroomRepository.findAll().stream().filter(c -> c.getName().equals(request.getGrade())).findFirst().orElse(null);
            }
        }

        Student student = Student.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .schoolNumber(request.getSchoolNumber())
                .grade(request.getGrade())
                .user(savedUser)
                .parent(parent)
                .classroom(classroom)
                .school(tenantSchool)
                .gender(request.getGender() != null ? request.getGender() : "Belirtilmemiş")
                .build();

        studentRepository.save(student);
        return "Öğrenci başarıyla eklendi";
    }

    public Student createStudent(Student student) {
        School tenantSchool = getCurrentSchool();
        if (tenantSchool != null) {
            student.setSchool(tenantSchool);
        }
        return studentRepository.save(student);
    }

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(StudentMapper::toDto)
                .collect(Collectors.toList());
    }

    public Student assignParent(Long studentId, Long parentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı"));
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Veli bulunamadı"));

        student.setParent(parent);
        return studentRepository.save(student);
    }

    public Student getStudentBySchoolNumber(String schoolNumber) {
        return studentRepository.findBySchoolNumber(schoolNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bu okul numarasına ait bir öğrenci bulunamadı!"));
    }

    public List<StudentDTO> searchStudentsByFirstName(String firstName) {
        return studentRepository.findByFirstNameContainingIgnoreCase(firstName).stream()
                .map(StudentMapper::toDto)
                .collect(Collectors.toList());
    }

    public Page<StudentDTO> getStudentsPaginated(int page, int size) {
        return studentRepository.findAll(PageRequest.of(page, size)).map(StudentMapper::toDto);
    }

    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı!"));
        studentRepository.delete(student);
    }

    public void updateStudent(Long id, CreateStudentRequest request) {
        School tenantSchool = getCurrentSchool();

        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı!"));

        existingStudent.setSchoolNumber(request.getSchoolNumber());
        existingStudent.setFirstName(request.getFirstName());
        existingStudent.setLastName(request.getLastName());
        existingStudent.setGrade(request.getGrade());

        if (request.getGender() != null) {
            existingStudent.setGender(request.getGender());
        }

        if (request.getGrade() != null && !request.getGrade().isEmpty()) {
            Classroom classroom = null;
            if (tenantSchool != null) {
                classroom = classroomRepository.findByNameAndSchool(request.getGrade(), tenantSchool).orElse(null);
            }
            existingStudent.setClassroom(classroom);
        } else {
            existingStudent.setClassroom(null);
        }

        if (request.getParentId() != null) {
            Parent parent = parentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Veli bulunamadı!"));
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

        School tenantSchool = getCurrentSchool();
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
