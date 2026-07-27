package com.educonnect.controller;

import com.educonnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        // Tüm paketleme ve hesaplama işlemleri servise devredildi
        return ResponseEntity.ok(userService.getCurrentUserProfile(principal.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(Principal principal, @RequestBody Map<String, Object> body) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        // Tüm doğrulama ve güncelleme işlemleri servise devredildi
        userService.updateCurrentUserProfile(principal.getName(), body);
        return ResponseEntity.ok("Profil güncellendi.");
    }
}