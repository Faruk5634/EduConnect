package com.educonnect.controller;

import com.educonnect.dto.CreateAdminRequest;
import com.educonnect.model.Role;
import com.educonnect.model.School;
import com.educonnect.model.User;
import com.educonnect.repository.SchoolRepository;
import com.educonnect.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
public class AdminManagementController {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/create-admin")
    public ResponseEntity<?> createSchoolAdmin(@RequestBody CreateAdminRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Hata: Bu kullanıcı adı zaten kullanılıyor!");
        }

        // 🚀 Okul nesnesini başlangıçta boş (null) bırakıyoruz
        School school = null;

        // Eğer formdan bir schoolId gelmişse, okulu bulup atıyoruz
        if (request.getSchoolId() != null) {
            school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Belirtilen okul bulunamadı!"));
        }

        // DTO'dan gelen role bilgisine göre rütbeyi belirliyoruz (Varsayılan: ROLE_ADMIN)
        Role assignedRole = Role.ROLE_ADMIN;
        if (request.getRole() != null && request.getRole().equals("ROLE_VICE_ADMIN")) {
            assignedRole = Role.ROLE_VICE_ADMIN;
        }

        User newAdmin = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName()) // YENİ
                .lastName(request.getLastName())   // YENİ
                .phone(request.getPhone())         // YENİ
                .email(request.getEmail())         // YENİ
                .role(assignedRole)
                .school(school)
                .build();

        userRepository.save(newAdmin);

        String message = school != null
                ? "Başarılı: Yeni yönetici " + school.getName() + " kampüsüne atandı!"
                : "Başarılı: Yeni yönetici oluşturuldu ancak henüz bir kampüse atanmadı (Boşta).";

        return ResponseEntity.ok(message);
    }

    @GetMapping("/admins")
    public ResponseEntity<?> getAllAdmins() {
        var admins = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_ADMIN || u.getRole() == Role.ROLE_VICE_ADMIN)
                .map(u -> java.util.Map.of(
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
        return ResponseEntity.ok(admins);
    }

    @PutMapping("/update-admin/{id}")
    public ResponseEntity<?> updateAdmin(Principal principal, @PathVariable Long id, @RequestBody CreateAdminRequest request) {
        User admin = userRepository.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Yönetici bulunamadı!"));

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

        // Şifre kutusu boş değilse şifreyi de güncelle
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            // Eğer güncellenen yönetici ile şu anki kullanıcı aynı ise mevcut şifre doğrulaması iste
            if (principal != null) {
                var currentUserOpt = userRepository.findByUsername(principal.getName());
                if (currentUserOpt.isPresent()) {
                    User currentUser = currentUserOpt.get();
                    if (currentUser.getId().equals(id)) {
                        // kendi hesabını güncelliyor: currentPassword kontrol et
                        if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
                            return ResponseEntity.badRequest().body("Mevcut şifre yanlış.");
                        }
                    }
                }
            }

            admin.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(admin);
        return ResponseEntity.ok("Yönetici güncellendi.");
    }

    @DeleteMapping("/delete-admin/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("Yönetici silindi.");
    }
}