package com.educonnect.service;

import com.educonnect.dto.TeacherDTO;
import com.educonnect.model.Classroom;
import com.educonnect.model.Role;
import com.educonnect.model.Teacher;
import com.educonnect.model.User;
import com.educonnect.repository.ClassroomRepository;
import com.educonnect.repository.TeacherRepository;
import com.educonnect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService; // 🚀 EKLENDİ: Müdürü bulmak için

    public TeacherService(TeacherRepository teacherRepository,
                          ClassroomRepository classroomRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          UserService userService) {
        this.teacherRepository = teacherRepository;
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    public Teacher createTeacherWithUser(Teacher teacher) {
        User admin = userService.getCurrentUser(); // 🚀 Giriş yapan müdürü bul

        String generatedUsername = teacher.getUsername();
        if (generatedUsername == null || generatedUsername.isEmpty()) {
            generatedUsername = (teacher.getFirstName() + "." + teacher.getLastName())
                    .toLowerCase()
                    .replace("ş", "s").replace("ı", "i").replace("ğ", "g")
                    .replace("ö", "o").replace("ç", "c").replace("ü", "u")
                    .replace(" ", "");

            if (userRepository.findByUsername(generatedUsername).isPresent()) {
                generatedUsername = generatedUsername + "1";
            }
        } else if (userRepository.findByUsername(generatedUsername).isPresent()) {
            throw new RuntimeException("Bu kullanıcı adı zaten alınmış!");
        }

        String password = (teacher.getPassword() != null && !teacher.getPassword().isEmpty())
                ? teacher.getPassword() : "123456";

        User user = User.builder()
                .username(generatedUsername)
                .password(passwordEncoder.encode(password))
                .role(Role.ROLE_TEACHER)
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .school(admin.getSchool()) // 🚀 GÜVENLİK KİLİDİ: Öğretmeni müdürün okuluna bağla
                .build();

        User savedUser = userRepository.save(user);
        teacher.setUser(savedUser);

        return teacherRepository.save(teacher);
    }

    public Teacher createTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    public List<TeacherDTO> getAllTeachers() {
        User admin = userService.getCurrentUser(); // 🚀 Müdürü bul

        // 🚀 GÜVENLİK KİLİDİ: Sadece bu okula ait öğretmenleri çek
        return teacherRepository.findByUserSchool(admin.getSchool()).stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    private TeacherDTO convertToDTO(Teacher teacher) {
        List<TeacherDTO.ClassroomInfo> classInfos = teacher.getHomeroomClasses() != null
                ? teacher.getHomeroomClasses().stream()
                  .map(c -> new TeacherDTO.ClassroomInfo(c.getId(), c.getName()))
                  .collect(java.util.stream.Collectors.toList())
                : List.of();

        String username = teacher.getUser() != null ? teacher.getUser().getUsername() : null;

        return new TeacherDTO(
                teacher.getId(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getBranch(),
                username, // 🚀 Kullanıcı adı DTO'ya eklendi
                classInfos
        );
    }

    public List<TeacherDTO> searchTeachersByBranch(String branch) {
        // İleride buraya da okul bazlı arama filtresi eklenebilir
        return teacherRepository.findByBranchContainingIgnoreCase(branch)
                .stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public TeacherDTO getTeacherProfileByUsername(String username) {
        Teacher teacher = teacherRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Eyvah! Bu kullanıcıya ait bir öğretmen profili bulunamadı."));

        return convertToDTO(teacher);
    }

    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Öğretmen bulunamadı!"));

        List<Classroom> classrooms = classroomRepository.findByHomeroomTeacher(teacher);
        for (Classroom cls : classrooms) {
            cls.setHomeroomTeacher(null);
            classroomRepository.save(cls);
        }

        teacherRepository.delete(teacher);
    }

    public void updateTeacher(Long id, Teacher updatedTeacher) {
        Teacher existingTeacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Öğretmen bulunamadı!"));

        existingTeacher.setFirstName(updatedTeacher.getFirstName());
        existingTeacher.setLastName(updatedTeacher.getLastName());
        existingTeacher.setBranch(updatedTeacher.getBranch());

        // 🚀 HESAP GÜNCELLEMELERİ
        if (existingTeacher.getUser() != null) {
            existingTeacher.getUser().setFirstName(updatedTeacher.getFirstName());
            existingTeacher.getUser().setLastName(updatedTeacher.getLastName());

            if (updatedTeacher.getUsername() != null && !updatedTeacher.getUsername().isEmpty()) {
                existingTeacher.getUser().setUsername(updatedTeacher.getUsername());
            }
            if (updatedTeacher.getPassword() != null && !updatedTeacher.getPassword().isEmpty()) {
                existingTeacher.getUser().setPassword(passwordEncoder.encode(updatedTeacher.getPassword()));
            }
        }

        teacherRepository.save(existingTeacher);
    }

    public Teacher createProfileForExistingUser(String username, Teacher teacherProfile) {
        User existingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + username));

        if (teacherRepository.findByUserUsername(username).isPresent()) {
            throw new RuntimeException("Bu hesabın zaten bir öğretmen profili var!");
        }

        teacherProfile.setUser(existingUser);
        return teacherRepository.save(teacherProfile);
    }
}