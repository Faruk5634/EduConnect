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
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // SchoolRepository telsizini de içeri aldık
    public AuthController(UserRepository userRepository, SchoolRepository schoolRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // 1. VEZNE: KAYIT OL (Artık çok daha akıllı)
    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRequest request) {

        // 1. Aynı isimde biri var mı?
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu kullanıcı adı zaten alınmış!");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // Gönderilen rol yoksa varsayılan olarak STUDENT yapalım
        Role userRole = request.getRole() != null ? request.getRole() : Role.ROLE_STUDENT;
        newUser.setRole(userRole);

        // 2. İşlemi Yapan Kişiyi (Kayıt memurunu) Tanı
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Eğer sistemde hiç kimse yoksa (ilk kurulum), ilk kişiyi SUPER_ADMIN yap ve geç
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            // İlk kurucuya okul atamıyoruz, o her şeyin üstünde
            newUser.setRole(Role.ROLE_SUPER_ADMIN);
            User savedUser = userRepository.save(newUser);
            return new AuthResponse(savedUser.getUsername(), savedUser.getRole(), "Sistemin ilk kurucusu (Super Admin) başarıyla oluşturuldu!", null);
        }

        // 3. Kayıt memuru sisteme giriş yapmış biri. Kim olduğuna bakalım:
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kayıt yapan kullanıcı bulunamadı."));

        // A) Eğer Kayıt Yapan Kişi SUPER ADMIN ise:
        if (currentUser.getRole() == Role.ROLE_SUPER_ADMIN) {
            // Süper Admin'in kimi kaydettiğine bakalım
            if (request.getSchoolId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Super Admin birini kaydederken mutlaka bir 'schoolId' göndermelidir.");
            }
            School targetSchool = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Belirtilen okul bulunamadı."));

            newUser.setSchool(targetSchool);
        }
        // B) Eğer Kayıt Yapan Kişi OKUL MÜDÜRÜ (ADMIN) ise:
        else if (currentUser.getRole() == Role.ROLE_ADMIN) {
            // Müdür başka okula adam kaydedemez, sadece kendi okuluna kaydedebilir.
            // Bu yüzden dışarıdan gelen schoolId'yi umursamıyoruz, müdürün kendi okulunu basıyoruz.
            if (currentUser.getSchool() == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Bu müdürün atanmış bir okulu yok!");
            }
            newUser.setSchool(currentUser.getSchool());
        }
        // C) Öğretmen, Öğrenci veya Veli başkasını kaydedemez!
        else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sadece Müdürler ve Super Admin sisteme yeni kullanıcı ekleyebilir.");
        }

        // 4. Her şey tamamsa kaydet
        User savedUser = userRepository.save(newUser);
        return new AuthResponse(savedUser.getUsername(), savedUser.getRole(), "Kayıt başarılı! Yeni personel okula atandı.", null);
    }

    // 2. VEZNE: GİRİŞ YAP (Değişmedi)
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