package com.educonnect.controller;

import com.educonnect.dto.AuthRequest;
import com.educonnect.dto.AuthResponse;
import com.educonnect.model.Role;
import com.educonnect.model.User;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.CustomUserDetailsService;
import com.educonnect.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. VEZNE: KAYIT OL (Sisteme ilk yöneticiyi eklemek için)
    @PostMapping("/register")
    public String register(@RequestBody AuthRequest request) {
        // Aynı isimde biri var mı kontrol et
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return "Bu kullanıcı adı zaten alınmış!";
        }

        // Yeni kimlik kartı basıyoruz
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword())); // Şifreyi kriptolayarak kaydediyoruz!
        newUser.setRole(Role.ROLE_ADMIN); // Şimdilik kayıt olan herkesi Admin yapalım ki test edebilelim

        userRepository.save(newUser);
        return "Kaptan, kayıt başarılı! Artık giriş yapabilirsin.";
    }

    // 2. VEZNE: GİRİŞ YAP (Şifre doğruysa JWT Biletini bas)
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) throws Exception {
        try {
            // Güvenlik şefi, gelen adı ve şifreyi veritabanıyla karşılaştırıyor
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception e) {
            throw new Exception("Kullanıcı adı veya şifre hatalı!");
        }

        // Şifre doğruysa kullanıcının dosyalarını getir
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        // Matbaaya emri ver ve bileti (Token) üret
        final String jwtToken = jwtUtil.generateToken(userDetails);

        // Bileti yolcuya teslim et
        return new AuthResponse(jwtToken);
    }
}
