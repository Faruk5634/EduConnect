package com.educonnect.service;

import com.educonnect.dto.AuthRequest;
import com.educonnect.dto.AuthResponse;
import com.educonnect.model.Role;
import com.educonnect.model.School;
import com.educonnect.model.User;
import com.educonnect.repository.SchoolRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(AuthRequest request, String currentUsername, boolean isAnonymous) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu kullanıcı adı zaten alınmış!");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(request.getRole() != null ? request.getRole() : Role.ROLE_STUDENT);

        if (isAnonymous) {
            if (userRepository.count() == 0) {
                newUser.setRole(Role.ROLE_SUPER_ADMIN);
                User savedUser = userRepository.save(newUser);
                return new AuthResponse(savedUser.getUsername(), savedUser.getRole(), "Sistemin ilk kurucusu (Super Admin) başarıyla oluşturuldu!", null);
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sisteme dışarıdan yetkisiz kayıt yapılamaz! Lütfen giriş yapın.");
            }
        }

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kayıt yapan kullanıcı bulunamadı."));

        if (currentUser.getRole() == Role.ROLE_SUPER_ADMIN) {
            if (request.getSchoolId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Super Admin birini kaydederken mutlaka bir 'schoolId' göndermelidir.");
            }
            School targetSchool = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Belirtilen okul bulunamadı."));
            newUser.setSchool(targetSchool);
        } else if (currentUser.getRole() == Role.ROLE_ADMIN) {
            if (currentUser.getSchool() == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Bu müdürün atanmış bir okulu yok!");
            }
            newUser.setSchool(currentUser.getSchool());
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sadece Müdürler ve Super Admin sisteme yeni kullanıcı ekleyebilir.");
        }

        User savedUser = userRepository.save(newUser);
        return new AuthResponse(savedUser.getUsername(), savedUser.getRole(), "Kayıt başarılı! Yeni personel okula atandı.", null);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kullanıcı adı veya şifre hatalı!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kullanıcı adı veya şifre hatalı!");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(user.getUsername(), user.getRole(), "Giriş başarılı.", token);
    }
}