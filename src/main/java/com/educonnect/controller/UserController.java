package com.educonnect.controller;

import com.educonnect.model.User;
import com.educonnect.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 🚀 YENİ EKLENEN KISIM: Giriş Yapan Kullanıcıyı Hatasız Gönderen Motor
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

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
}