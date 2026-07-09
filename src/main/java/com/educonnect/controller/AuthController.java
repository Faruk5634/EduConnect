package com.educonnect.controller;

import com.educonnect.dto.AuthRequest;
import com.educonnect.dto.AuthResponse;
import com.educonnect.model.Role;
import com.educonnect.model.School;
import com.educonnect.model.User;
import com.educonnect.repository.SchoolRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, SchoolRepository schoolRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu kullanıcı adı zaten alınmış!");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        Role userRole = request.getRole() != null ? request.getRole() : Role.ROLE_STUDENT;
        newUser.setRole(userRole);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 🚀 KORSAN KAPISI KAPATILDI: Sadece veritabanı boşsa anonim kayda izin ver!
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            if (userRepository.count() == 0) {
                newUser.setRole(Role.ROLE_SUPER_ADMIN);
                User savedUser = userRepository.save(newUser);
                return new AuthResponse(savedUser.getUsername(), savedUser.getRole(), "Sistemin ilk kurucusu (Super Admin) başarıyla oluşturuldu!", null);
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sisteme dışarıdan yetkisiz kayıt yapılamaz! Lütfen giriş yapın.");
            }
        }

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kayıt yapan kullanıcı bulunamadı."));

        if (currentUser.getRole() == Role.ROLE_SUPER_ADMIN) {
            if (request.getSchoolId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Super Admin birini kaydederken mutlaka bir 'schoolId' göndermelidir.");
            }
            School targetSchool = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Belirtilen okul bulunamadı."));

            newUser.setSchool(targetSchool);
        }
        else if (currentUser.getRole() == Role.ROLE_ADMIN) {
            if (currentUser.getSchool() == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Bu müdürün atanmış bir okulu yok!");
            }
            newUser.setSchool(currentUser.getSchool());
        }
        else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sadece Müdürler ve Super Admin sisteme yeni kullanıcı ekleyebilir.");
        }

        User savedUser = userRepository.save(newUser);
        return new AuthResponse(savedUser.getUsername(), savedUser.getRole(), "Kayıt başarılı! Yeni personel okula atandı.", null);
    }

    @PostMapping("/login")
    public org.springframework.http.ResponseEntity<?> login(@RequestBody AuthRequest request) {

        java.util.Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        // Kullanıcı yoksa 500 atma, kibarca 401 (Yetkisiz) dön!
        if (userOpt.isEmpty()) {
            return org.springframework.http.ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kullanıcı adı veya şifre hatalı!");
        }

        User user = userOpt.get();

        // Şifre eşleşmiyorsa 500 atma, kibarca 401 dön!
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return org.springframework.http.ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kullanıcı adı veya şifre hatalı!");
        }

        // Her şey doğruysa bileti ver
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return org.springframework.http.ResponseEntity.ok(new AuthResponse(user.getUsername(), user.getRole(), "Giriş başarılı.", token));
    }
}