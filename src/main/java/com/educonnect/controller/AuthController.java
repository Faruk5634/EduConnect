package com.educonnect.controller;

import com.educonnect.dto.AuthRequest;
import com.educonnect.dto.AuthResponse;
import com.educonnect.model.Role;
import com.educonnect.model.User;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // 1. VEZNE: KAYIT OL (Sisteme ilk yöneticiyi eklemek için)
    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu kullanıcı adı zaten alınmış!");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Role.ROLE_ADMIN);

        User savedUser = userRepository.save(newUser);
        return new AuthResponse(savedUser.getUsername(), savedUser.getRole(), "Kaptan, kayıt başarılı! Artık giriş yapabilirsin.", null);
    }

    // 2. VEZNE: GİRİŞ YAP
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kullanıcı adı veya şifre hatalı!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kullanıcı adı veya şifre hatalı!");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(user.getUsername(), user.getRole(), "Giriş başarılı.", token);
    }
}
