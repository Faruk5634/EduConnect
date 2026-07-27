package com.educonnect.service;

import com.educonnect.dto.CreateAdminRequest;
import com.educonnect.model.Role;
import com.educonnect.model.School;
import com.educonnect.model.User;
import com.educonnect.repository.SchoolRepository;
import com.educonnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminManagementService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;

    public String createSchoolAdmin(CreateAdminRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hata: Bu kullanıcı adı zaten kullanılıyor!");
        }

        School school = null;
        if (request.getSchoolId() != null) {
            school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Belirtilen okul bulunamadı!"));
        }

        Role assignedRole = (request.getRole() != null && request.getRole().equals("ROLE_VICE_ADMIN"))
                ? Role.ROLE_VICE_ADMIN : Role.ROLE_ADMIN;

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
                .map(u -> Map.of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "firstName", u.getFirstName() != null ? u.getFirstName() : "",
                        "lastName", u.getLastName() != null ? u.getLastName() : "",
                        "phone", u.getPhone() != null ? u.getPhone() : "",
                        "email", u.getEmail() != null ? u.getEmail() : "",
                        "role", u.getRole().name(),
                        "schoolId", u.getSchool() != null ? u.getSchool().getId() : "",
                        "schoolName", u.getSchool() != null ? u.getSchool().getName() : "Boşta"
                ))
                .toList();
    }

    public void updateAdmin(Long id, CreateAdminRequest request, String currentUsername) {
        User admin = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Yönetici bulunamadı!"));

        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setPhone(request.getPhone());
        admin.setEmail(request.getEmail());
        admin.setRole(request.getRole() != null && request.getRole().equals("ROLE_VICE_ADMIN") ? Role.ROLE_VICE_ADMIN : Role.ROLE_ADMIN);

        if (request.getSchoolId() != null) {
            School school = schoolRepository.findById(request.getSchoolId()).orElse(null);
            admin.setSchool(school);
        } else {
            admin.setSchool(null);
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (currentUsername != null) {
                User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();
                if (currentUser.getId().equals(id)) {
                    if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mevcut şifre yanlış.");
                    }
                }
            }
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(admin);
    }

    public void deleteAdmin(Long id) {
        userRepository.deleteById(id);
    }
}