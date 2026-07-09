package com.educonnect.service;

import com.educonnect.dto.StudentDTO;
import com.educonnect.model.Classroom;
import com.educonnect.model.Parent;
import com.educonnect.model.Role;
import com.educonnect.model.Student;
import com.educonnect.repository.ClassroomRepository;
import com.educonnect.repository.ParentRepository;
import com.educonnect.repository.StudentRepository;
import com.educonnect.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 🚀 EKLENDİ
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.educonnect.dto.CreateStudentRequest;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import com.educonnect.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional // 🚀 SİHİRLİ DOKUNUŞ: Veritabanı tembelliği çözüldü, tüm işlemler kesin kaydedilecek!
public class StudentService {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClassroomRepository classroomRepository;
    private final UserService userService;

    public String createStudentWithUser(CreateStudentRequest request) {
        User admin = userService.getCurrentUser();

        User savedUser = null;

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {

            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu kullanıcı adı zaten alınmış!");
            }

            User user = new User();
            user.setUsername(request.getUsername());

            String password = (request.getPassword() != null && !request.getPassword().trim().isEmpty()) ? request.getPassword() : "123456";
            user.setPassword(passwordEncoder.encode(password));

            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setRole(Role.ROLE_STUDENT);
            user.setSchool(admin.getSchool());
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());

            savedUser = userRepository.save(user);
        }

        Parent parent = (request.getParentId() != null) ? parentRepository.findById(request.getParentId()).orElse(null) : null;
        Classroom classroom = (request.getGrade() != null && !request.getGrade().isEmpty()) ? classroomRepository.findByNameAndSchool(request.getGrade(), admin.getSchool()).orElse(null) : null;

        Student student = Student.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .schoolNumber(request.getSchoolNumber())
                .grade(request.getGrade())
                .user(savedUser)
                .parent(parent)
                .classroom(classroom)
                .school(admin.getSchool())
                .gender(request.getGender() != null ? request.getGender() : "Belirtilmemiş")
                .build();

        studentRepository.save(student);
        return "Öğrenci başarıyla eklendi";
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public List<StudentDTO> getAllStudents() {
        User admin = userService.getCurrentUser();
        return studentRepository.findBySchool(admin.getSchool())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Student assignParent(Long studentId, Long parentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Öğrenci bulunamadı"));

        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veli bulunamadı"));

        student.setParent(parent);
        return studentRepository.save(student);
    }

    public Student getStudentBySchoolNumber(String schoolNumber) {
        return studentRepository.findBySchoolNumber(schoolNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bu okul numarasına ait bir öğrenci bulunamadı!"));
    }

    public List<StudentDTO> searchStudentsByFirstName(String firstName) {
        return studentRepository.findByFirstNameContainingIgnoreCase(firstName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private StudentDTO convertToDTO(Student student) {
        String parentName = (student.getParent() != null) ? student.getParent().getFirstName() + " " + student.getParent().getLastName() : "Veli Atanmadı";
        Long parentId = (student.getParent() != null) ? student.getParent().getId() : null;
        String username = (student.getUser() != null) ? student.getUser().getUsername() : null;
        String phone = (student.getUser() != null) ? student.getUser().getPhone() : null;
        String email = (student.getUser() != null) ? student.getUser().getEmail() : null;

        return new StudentDTO(
                student.getId(), student.getFirstName(), student.getLastName(), student.getSchoolNumber(),
                parentName, parentId, username, student.getGrade(), student.getGender(),
                phone, email
        );
    }

    public Page<StudentDTO> getStudentsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return studentRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    public void updateStudent(Long id, CreateStudentRequest request) {
        User admin = userService.getCurrentUser();

        Student existingStudent = studentRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Öğrenci bulunamadı!"));

        existingStudent.setSchoolNumber(request.getSchoolNumber());
        existingStudent.setFirstName(request.getFirstName());
        existingStudent.setLastName(request.getLastName());
        existingStudent.setGrade(request.getGrade());

        if (request.getGender() != null) {
            existingStudent.setGender(request.getGender());
        }

        if (request.getGrade() != null && !request.getGrade().isEmpty()) {
            Classroom classroom = classroomRepository.findByNameAndSchool(request.getGrade(),admin.getSchool()).orElse(null);
            existingStudent.setClassroom(classroom);
        } else {
            existingStudent.setClassroom(null);
        }

        if (request.getParentId() != null) {
            Parent parent = parentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veli bulunamadı!"));
            existingStudent.setParent(parent);
        } else {
            existingStudent.setParent(null);
        }

        if (existingStudent.getUser() != null) {
            User user = existingStudent.getUser();

            if (request.getUsername() != null && !request.getUsername().isEmpty()) {
                user.setUsername(request.getUsername());
            }
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı: " + username));

        if (existingUser.getStudent() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu hesabın zaten bir öğrenci profili var!");
        }

        studentProfile.setUser(existingUser);
        return studentRepository.save(studentProfile);
    }
}