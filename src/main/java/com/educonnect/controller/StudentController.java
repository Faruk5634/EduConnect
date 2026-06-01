package com.educonnect.controller;

import com.educonnect.dto.CreateStudentRequest;
import com.educonnect.dto.StudentDTO;
import com.educonnect.model.Role;
import com.educonnect.model.Student;
import com.educonnect.model.User;
import com.educonnect.repository.StudentRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService; // Artık depo (Repository) yok, Servis var!
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentController(StudentService studentService, UserRepository userRepository,
                             StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentService = studentService;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public Student createStudent(@Valid @RequestBody Student student) {
        return studentService.createStudent(student); // İşi servise pasla
    }

    @PostMapping("/create")
    public String createStudentWithUser(@RequestBody CreateStudentRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu kullanıcı adı zaten alınmış!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_STUDENT)
                .build();
        User savedUser = userRepository.save(user);

        Student student = Student.builder()
                .firstName(request.getUsername())
                .lastName("Student")
                .schoolNumber(request.getSchoolNumber())
                .grade(request.getGrade())
                .user(savedUser)
                .build();
        studentRepository.save(student);

        return "Öğrenci başarıyla eklendi";
    }

    @GetMapping
    public List<StudentDTO> getAllStudents() {
        return studentService.getAllStudents();
    }

    @PutMapping("/{studentId}/parent/{parentId}")
    public Student assignParentToStudent(@PathVariable Long studentId, @PathVariable Long parentId) {
        return studentService.assignParent(studentId, parentId); // İşi servise pasla
    }


    //  Okul numarasına göre öğrenci bulma iş mantığı
    @GetMapping("/number/{schoolNumber}")
    public Student getStudentBySchoolNumber(@PathVariable String schoolNumber) {
        return studentService.getStudentBySchoolNumber(schoolNumber);
    }

    //Öğrenci isminde geçen harflere göre arama yapma iş mantığı
    @GetMapping("/search")
    public List<StudentDTO> searchStudentsByFirstName(@RequestParam String firstName) {
        return studentService.searchStudentsByFirstName(firstName);
    }


    @GetMapping("/page")
    public Page<StudentDTO> getStudentsPaginated(
            @RequestParam(defaultValue = "0") int page, // Kullanıcı bir şey demezse 0. sayfa (İlk sayfa)
            @RequestParam(defaultValue = "10") int size) { // Kullanıcı bir şey demezse sayfada 10 kişi

        return studentService.getStudentsPaginated(page, size);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        // Servis katmanından silme işlemini çağırıyoruz
        // Not: Eğer senin servisindeki metodun adı farklıysa (örn: deleteById) burayı ona göre düzenle
        studentService.deleteStudent(id);

        // İşlem başarılı, ancak geri döndürecek bir veri yok (204 No Content) mesajı yolluyoruz
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody com.educonnect.model.Student updatedStudent) {
        // 1. Servis katmanına "Bu ID'li öğrenciyi bu yeni bilgilerle güncelle" diyoruz.
        // Not: com.educonnect.entity.Student kısmını kendi Student sınıfının paketine göre (gerekirse import ederek) düzelt
        studentService.updateStudent(id, updatedStudent);

        // 2. İşlem başarılı mesajı dönüyoruz
        return ResponseEntity.ok().build();
    }

}
