package com.educonnect.service;

import com.educonnect.model.User;
import com.educonnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));
    }

    public Map<String, Object> getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("name", user.getFirstName() + " " + user.getLastName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("role", user.getRole().name());

        if (user.getSchool() != null) {
            response.put("schoolName", user.getSchool().getName());
            response.put("schoolType", user.getSchool().getSchoolType());
        } else {
            response.put("schoolName", "Kurum Ataması Bekleniyor");
            response.put("schoolType", "UNKNOWN");
        }

        return response;
    }

    public void updateCurrentUserProfile(String username, Map<String, Object> body) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));

        if (body.containsKey("firstName")) user.setFirstName((String) body.get("firstName"));
        if (body.containsKey("lastName")) user.setLastName((String) body.get("lastName"));
        if (body.containsKey("email")) user.setEmail((String) body.get("email"));
        if (body.containsKey("phone")) user.setPhone((String) body.get("phone"));

        if (body.containsKey("password")) {
            String pw = (String) body.get("password");
            if (pw != null && !pw.isEmpty()) {
                if (!body.containsKey("currentPassword") || body.get("currentPassword") == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mevcut şifre sağlanmadı.");
                }
                String currentPw = (String) body.get("currentPassword");
                if (!passwordEncoder.matches(currentPw, user.getPassword())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mevcut şifre yanlış.");
                }
                user.setPassword(passwordEncoder.encode(pw));
            }
        }

        userRepository.save(user);
    }
}