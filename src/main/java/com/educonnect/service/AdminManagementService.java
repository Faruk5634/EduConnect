package com.educonnect.service;

import com.educonnect.dto.CreateAdminRequest;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.model.Role;
import com.educonnect.model.School;
import com.educonnect.model.User;
import com.educonnect.repository.SchoolRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.PasswordChangeSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminManagementService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordChangeSupport passwordChangeSupport;

    public String createSchoolAdmin(CreateAdminRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hata: Bu kullanıcı adı zaten kullanılıyor!");
        }

        School school = null;
        if (request.getSchoolId() != null) {
            school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("Belirtilen okul bulunamadı!"));
        }

        Role assignedRole = resolveRole(request.getRole());

        User newAdmin = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .role(assignedRole)
                .school(school)
                .build();

        userRepository.save(newAdmin);

        return school != null
                ? "Başarılı: Yeni yönetici " + school.getName() + " kampüsüne atandı!"
                : "Başarılı: Yeni yönetici oluşturuldu ancak henüz bir kampüse atanmadı (Boşta).";
    }

    public List<Map<String, Object>> getAllAdmins() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_ADMIN || u.getRole() == Role.ROLE_VICE_ADMIN)
                .map(u -> {
                    Map<String, Object> adminData = new HashMap<>();
                    adminData.put("id", u.getId());
                    adminData.put("username", u.getUsername());
                    adminData.put("firstName", u.getFirstName() != null ? u.getFirstName() : "");
                    adminData.put("lastName", u.getLastName() != null ? u.getLastName() : "");
                    adminData.put("phone", u.getPhone() != null ? u.getPhone() : "");
                    adminData.put("email", u.getEmail() != null ? u.getEmail() : "");
                    adminData.put("role", u.getRole().name());
                    adminData.put("schoolId", u.getSchool() != null ? u.getSchool().getId() : "");
                    adminData.put("schoolName", u.getSchool() != null ? u.getSchool().getName() : "Boşta");
                    return adminData;
                })
                .toList();
    }

    public void updateAdmin(Long id, CreateAdminRequest request, String currentUsername) {
        User admin = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yönetici bulunamadı!"));

        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setPhone(request.getPhone());
        admin.setEmail(request.getEmail());
        admin.setRole(resolveRole(request.getRole()));

        if (request.getSchoolId() != null) {
            School school = schoolRepository.findById(request.getSchoolId()).orElse(null);
            admin.setSchool(school);
        } else {
            admin.setSchool(null);
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            // Only require current-password verification when admins are
            // changing THEIR OWN password, not when they reset someone else's.
            boolean isSelfChange = currentUsername != null
                    && userRepository.findByUsername(currentUsername)
                    .map(u -> u.getId().equals(id))
                    .orElse(false);

            if (isSelfChange) {
                passwordChangeSupport.verifyCurrentPassword(request.getCurrentPassword(), admin.getPassword());
            }
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(admin);
    }

    public void deleteAdmin(Long id) {
        userRepository.deleteById(id);
    }

    private Role resolveRole(Role requested) {
        return requested == Role.ROLE_VICE_ADMIN ? Role.ROLE_VICE_ADMIN : Role.ROLE_ADMIN;
    }
}