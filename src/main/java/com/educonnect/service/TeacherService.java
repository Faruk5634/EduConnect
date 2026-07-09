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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

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
        User admin = userService.getCurrentUser();

        String generatedUsername = teacher.getUsername();
        if (generatedUsername == null || generatedUsername.trim().isEmpty()) {
            generatedUsername = (teacher.getFirstName() + "." + teacher.getLastName())
                    .toLowerCase()
                    .replace("ş", "s").replace("ı", "i").replace("ğ", "g")
                    .replace("ö", "o").replace("ç", "c").replace("ü", "u")
                    .replace(" ", "");

            if (userRepository.findByUsername(generatedUsername).isPresent()) {
                generatedUsername = generatedUsername + "1";
            }
            } else if (userRepository.findByUsername(generatedUsername).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Bu kullanıcı adı zaten alınmış!");
            }

        String password = (teacher.getPassword() != null && !teacher.getPassword().trim().isEmpty())
                ? teacher.getPassword() : "123456";

        User user = User.builder()
                .username(generatedUsername)
                .password(passwordEncoder.encode(password))
                .role(Role.ROLE_TEACHER)
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .phone(teacher.getPhone()) // 🚀 VERİTABANINA YAZILDI
                .email(teacher.getEmail()) // 🚀 VERİTABANINA YAZILDI
                .school(admin.getSchool())
                .build();

        User savedUser = userRepository.save(user);
        teacher.setUser(savedUser);

        return teacherRepository.save(teacher);
    }

    public Teacher createTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    public List<TeacherDTO> getAllTeachers() {
        User admin = userService.getCurrentUser();
        return teacherRepository.findByUserSchool(admin.getSchool()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TeacherDTO convertToDTO(Teacher teacher) {
        // İlişki karmaşasını (sonsuz döngüyü) engellemek için sadece String değerleri alıyoruz
        List<TeacherDTO.ClassroomInfo> classInfos = teacher.getHomeroomClasses() != null
                ? teacher.getHomeroomClasses().stream()
                  .map(c -> new TeacherDTO.ClassroomInfo(c.getId(), c.getName()))
                  .collect(Collectors.toList())
                : List.of();

        // 🚀 GÜVENLİ ERİŞİM: User nesnesinin içine gömülmek yerine değerleri "dışarıdan" kontrol ederek al
        String username = null;
        String phone = null;
        String email = null;

        if (teacher.getUser() != null) {
            username = teacher.getUser().getUsername();
            phone = teacher.getUser().getPhone();
            email = teacher.getUser().getEmail();
        }

        return new TeacherDTO(
                teacher.getId(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getBranch(),
                username,
                phone,
                email,
                classInfos
        );
    }

    public List<TeacherDTO> searchTeachersByBranch(String branch) {
        return teacherRepository.findByBranchContainingIgnoreCase(branch)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TeacherDTO getTeacherProfileByUsername(String username) {
        Teacher teacher = teacherRepository.findByUserUsername(username)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Bu kullanıcıya ait öğretmen profili bulunamadı."));

        return convertToDTO(teacher);
    }

    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Öğretmen bulunamadı!"));

        List<Classroom> classrooms = classroomRepository.findByHomeroomTeacher(teacher);
        for (Classroom cls : classrooms) {
            cls.setHomeroomTeacher(null);
            classroomRepository.save(cls);
        }

        teacherRepository.delete(teacher);
    }

    public void updateTeacher(Long id, Teacher updatedTeacher) {
        Teacher existingTeacher = teacherRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Öğretmen bulunamadı!"));

        existingTeacher.setFirstName(updatedTeacher.getFirstName());
        existingTeacher.setLastName(updatedTeacher.getLastName());
        existingTeacher.setBranch(updatedTeacher.getBranch());

        if (existingTeacher.getUser() != null) {
            User user = existingTeacher.getUser();
            user.setFirstName(updatedTeacher.getFirstName());
            user.setLastName(updatedTeacher.getLastName());

            if (updatedTeacher.getUsername() != null && !updatedTeacher.getUsername().trim().isEmpty()) {
                user.setUsername(updatedTeacher.getUsername());
            }
            if (updatedTeacher.getPassword() != null && !updatedTeacher.getPassword().trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updatedTeacher.getPassword()));
            }

            user.setPhone(updatedTeacher.getPhone()); // 🚀 GÜNCELLENDİ
            user.setEmail(updatedTeacher.getEmail()); // 🚀 GÜNCELLENDİ

            userRepository.save(user);
        }

        teacherRepository.save(existingTeacher);
    }

    public Teacher createProfileForExistingUser(String username, Teacher teacherProfile) {
        User existingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı: " + username));

        if (teacherRepository.findByUserUsername(username).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Bu hesabın zaten bir öğretmen profili var!");
        }

        teacherProfile.setUser(existingUser);
        return teacherRepository.save(teacherProfile);
    }
}