package com.educonnect.service;

import com.educonnect.dto.TeacherDTO;
import com.educonnect.dto.TeacherRequest;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.model.Classroom;
import com.educonnect.model.Role;
import com.educonnect.model.Teacher;
import com.educonnect.model.User;
import com.educonnect.repository.ClassroomRepository;
import com.educonnect.repository.TeacherRepository;
import com.educonnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final UsernameService usernameService;
    private final UserProvisioningService userProvisioningService; // 🚀 replaces duplicated account-creation logic

    public Teacher createTeacherWithUser(TeacherRequest request) {
        User admin = userService.getCurrentUser();

        User savedUser = userProvisioningService.provisionUser(
                request.getUsername(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getEmail(),
                Role.ROLE_TEACHER,
                admin.getSchool()
        );

        Teacher teacher = new Teacher();
        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setBranch(request.getBranch());
        teacher.setUser(savedUser);

        return teacherRepository.save(teacher);
    }

    public List<TeacherDTO> getAllTeachers() {
        User admin = userService.getCurrentUser();
        return teacherRepository.findByUserSchool(admin.getSchool()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TeacherDTO convertToDTO(Teacher teacher) {
        List<TeacherDTO.ClassroomInfo> classInfos = teacher.getHomeroomClasses() != null
                ? teacher.getHomeroomClasses().stream()
                  .map(c -> new TeacherDTO.ClassroomInfo(c.getId(), c.getName()))
                  .collect(Collectors.toList())
                : List.of();

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
                .orElseThrow(() -> new ResourceNotFoundException("Bu kullanıcıya ait öğretmen profili bulunamadı."));
        return convertToDTO(teacher);
    }

    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Öğretmen bulunamadı!"));

        List<Classroom> classrooms = classroomRepository.findByHomeroomTeacher(teacher);
        for (Classroom cls : classrooms) {
            cls.setHomeroomTeacher(null);
            classroomRepository.save(cls);
        }

        teacherRepository.delete(teacher);
    }

    public void updateTeacher(Long id, TeacherRequest request) {
        Teacher existingTeacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Öğretmen bulunamadı!"));

        existingTeacher.setFirstName(request.getFirstName());
        existingTeacher.setLastName(request.getLastName());
        existingTeacher.setBranch(request.getBranch());

        if (existingTeacher.getUser() != null) {
            User user = existingTeacher.getUser();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            if (request.getUsername() != null && !request.getUsername().isBlank()) {
                usernameService.assertUsernameAvailable(request.getUsername(), user.getId());
                user.setUsername(request.getUsername());
            }
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());

            userRepository.save(user);
        }

        teacherRepository.save(existingTeacher);
    }

    public Teacher createProfileForExistingUser(String username, Teacher teacherProfile) {
        User existingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));

        if (teacherRepository.findByUserUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu hesabın zaten bir öğretmen profili var!");
        }

        teacherProfile.setUser(existingUser);
        return teacherRepository.save(teacherProfile);
    }
}