package com.educonnect.controller;

import com.educonnect.model.User;
import com.educonnect.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 🚀 YENİ EKLENEN KISIM: Giriş Yapan Kullanıcıyı Hatasız Gönderen Motor
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));

        // Sonsuz döngü (500) hatasını engellemek için sadece gerekli verileri temizce paketliyoruz (DTO/Map mantığı)
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("name", user.getFirstName() + " " + user.getLastName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("role", user.getRole().name());

        if (user.getSchool() != null) {
            response.put("schoolName", user.getSchool().getName());
            // 👇 SADECE .name() KISMINI SİLDİK 👇
            response.put("schoolType", user.getSchool().getSchoolType());
        } else {
            response.put("schoolName", "Kurum Ataması Bekleniyor");
            response.put("schoolType", "UNKNOWN");
        }

        return ResponseEntity.ok(response);
    }

    // 🛠️ Profil güncelleme endpoint'i (me)
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(Principal principal, @RequestBody Map<String, Object> body) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(principal.getName()).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));

        if (body.containsKey("firstName")) user.setFirstName((String) body.get("firstName"));
        if (body.containsKey("lastName")) user.setLastName((String) body.get("lastName"));
        if (body.containsKey("email")) user.setEmail((String) body.get("email"));
        if (body.containsKey("phone")) user.setPhone((String) body.get("phone"));
        if (body.containsKey("password")) {
            String pw = (String) body.get("password");
            if (pw != null && !pw.isEmpty()) {
                // Eğer şifre değişikliği isteniyorsa, mevcut şifre doğrulaması iste
                if (!body.containsKey("currentPassword") || body.get("currentPassword") == null) {
                    return ResponseEntity.badRequest().body("Mevcut şifre sağlanmadı.");
                }
                String currentPw = (String) body.get("currentPassword");
                if (!passwordEncoder.matches(currentPw, user.getPassword())) {
                    return ResponseEntity.badRequest().body("Mevcut şifre yanlış.");
                }
                user.setPassword(passwordEncoder.encode(pw));
            }
        }

        userRepository.save(user);
        return ResponseEntity.ok("Profil güncellendi.");
    }
}