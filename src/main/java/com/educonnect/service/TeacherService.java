package com.educonnect.service;

import com.educonnect.dto.TeacherDTO;
import com.educonnect.dto.TeacherRequest;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.mapper.TeacherMapper;
import com.educonnect.model.Classroom;
import com.educonnect.model.Role;
import com.educonnect.model.School;
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

    private School getTenantSchool(User user) {
        if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
            return null;
        }
        if (user.getSchool() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kullanıcının atanmış bir okulu yok.");
        }
        return user.getSchool();
    }

    private void assertTeacherBelongsToTenant(Teacher teacher, School tenantSchool) {
        if (tenantSchool == null) {
            return;
        }
        if (teacher.getUser() == null || teacher.getUser().getSchool() == null || !tenantSchool.getId().equals(teacher.getUser().getSchool().getId())) {
            throw new ResourceNotFoundException("Öğretmen bulunamadı!");
        }
    }

    public Teacher createTeacherWithUser(TeacherRequest request) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);

        User savedUser = userProvisioningService.provisionUser(
                request.getUsername(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getEmail(),
                Role.ROLE_TEACHER,
                tenantSchool != null ? tenantSchool : admin.getSchool()
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
        School tenantSchool = getTenantSchool(admin);
        return (tenantSchool != null ? teacherRepository.findByUserSchool(tenantSchool) : teacherRepository.findAll())
                .stream()
                .map(TeacherMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TeacherDTO> searchTeachersByBranch(String branch) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        return (tenantSchool != null
                ? teacherRepository.findByUserSchoolAndBranchContainingIgnoreCase(tenantSchool, branch)
                : teacherRepository.findByBranchContainingIgnoreCase(branch))
                .stream()
                .map(TeacherMapper::toDto)
                .collect(Collectors.toList());
    }

    public TeacherDTO getTeacherProfileByUsername(String username) {
        Teacher teacher = teacherRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Bu kullanıcıya ait öğretmen profili bulunamadı."));
        return TeacherMapper.toDto(teacher);
    }

    public void deleteTeacher(Long id) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Öğretmen bulunamadı!"));
        assertTeacherBelongsToTenant(teacher, tenantSchool);

        List<Classroom> classrooms = classroomRepository.findByHomeroomTeacher(teacher);
        for (Classroom cls : classrooms) {
            cls.setHomeroomTeacher(null);
            classroomRepository.save(cls);
        }

        teacherRepository.delete(teacher);
    }

    public void updateTeacher(Long id, TeacherRequest request) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        Teacher existingTeacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Öğretmen bulunamadı!"));
        assertTeacherBelongsToTenant(existingTeacher, tenantSchool);

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

        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        if (tenantSchool != null && (existingUser.getSchool() == null || !tenantSchool.getId().equals(existingUser.getSchool().getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kullanıcı farklı bir okula ait.");
        }

        teacherProfile.setUser(existingUser);
        if (tenantSchool != null && existingUser.getSchool() == null) {
            existingUser.setSchool(tenantSchool);
            userRepository.save(existingUser);
        }
        return teacherRepository.save(teacherProfile);
    }
}
