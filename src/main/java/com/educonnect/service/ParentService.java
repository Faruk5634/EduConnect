package com.educonnect.service;

import com.educonnect.dto.ParentDTO;
import com.educonnect.model.Parent;
import com.educonnect.model.Role;
import com.educonnect.model.User;
import com.educonnect.repository.ParentRepository;
import com.educonnect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParentService {

    private final ParentRepository parentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService; // 🚀 EKLENDİ: Müdürü bulmak için

    public ParentService(ParentRepository parentRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, UserService userService) {
        this.parentRepository = parentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    public Parent createParentWithUser(Parent parent) {
        User admin = userService.getCurrentUser(); // 🚀 Giriş yapan müdürü bul

        String usernameToUse = parent.getUsername();
        String passwordToUse = parent.getPassword();

        // 1. Eğer username Frontend'den gelmediyse (İlkokul/Ortaokul ise) otomatik oluştur
        if (usernameToUse == null || usernameToUse.isEmpty()) {
            usernameToUse = (parent.getFirstName() + "." + parent.getLastName())
                    .toLowerCase()
                    .replaceAll("[çÇ]", "c").replaceAll("[ğĞ]", "g").replaceAll("[ıİ]", "i")
                    .replaceAll("[öÖ]", "o").replaceAll("[şŞ]", "s").replaceAll("[üÜ]", "u")
                    .replaceAll("\\s+", "");

            if (userRepository.findByUsername(usernameToUse).isPresent()) {
                usernameToUse = usernameToUse + "1";
            }
        } else {
            // Frontend'den geldiyse (Lise) çakışma var mı diye kontrol et
            if (userRepository.findByUsername(usernameToUse).isPresent()) {
                throw new RuntimeException("Bu kullanıcı adı zaten alınmış!");
            }
        }

        // Şifre gelmediyse varsayılan şifre koy
        if (passwordToUse == null || passwordToUse.isEmpty()) {
            passwordToUse = "123456";
        }

        // 2. Kullanıcıyı oluştur
        User user = new User();
        user.setUsername(usernameToUse);
        user.setPassword(passwordEncoder.encode(passwordToUse));
        user.setRole(Role.ROLE_PARENT);
        user.setFirstName(parent.getFirstName());
        user.setLastName(parent.getLastName());

        user.setSchool(admin.getSchool()); // 🚀 GÜVENLİK KİLİDİ: Veliyi müdürün okuluna bağla

        User savedUser = userRepository.save(user);
        parent.setUser(savedUser);

        return parentRepository.save(parent);
    }

    public List<ParentDTO> getAllParents() {
        User admin = userService.getCurrentUser(); // 🚀 Müdürü bul

        // 🚀 GÜVENLİK KİLİDİ: Sadece bu okula ait velileri çek
        List<Parent> parents = parentRepository.findByUserSchool(admin.getSchool());

        return parents.stream().map(parent -> {
            List<String> studentNames = parent.getStudents() != null ? parent.getStudents().stream()
                                                                       .map(student -> student.getFirstName() + " " + student.getLastName())
                                                                       .collect(Collectors.toList()) : List.of();

            return new ParentDTO(
                    parent.getId(),
                    parent.getFirstName(),
                    parent.getLastName(),
                    parent.getEmail(),
                    parent.getPhoneNumber(), // 🚀 Telefon Numarası Eklendi
                    parent.getUser() != null ? parent.getUser().getUsername() : null, // 🚀 Username Eklendi
                    studentNames
            );
        }).collect(Collectors.toList());
    }

    public void deleteParent(Long id) {
        parentRepository.deleteById(id);
    }

    public void updateParent(Long id, Parent updatedParent) {
        Parent existing = parentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veli bulunamadı!"));

        // Temel bilgileri güncelle
        existing.setFirstName(updatedParent.getFirstName());
        existing.setLastName(updatedParent.getLastName());
        existing.setEmail(updatedParent.getEmail());
        existing.setPhoneNumber(updatedParent.getPhoneNumber()); // 🚀 HATA ÇÖZÜLDÜ: Artık telefon da güncelleniyor

        // User (Hesap) bilgilerini güncelle
        if (existing.getUser() != null) {
            existing.getUser().setFirstName(updatedParent.getFirstName());
            existing.getUser().setLastName(updatedParent.getLastName());

            if (updatedParent.getUsername() != null && !updatedParent.getUsername().isEmpty()) {
                existing.getUser().setUsername(updatedParent.getUsername());
            }
            if (updatedParent.getPassword() != null && !updatedParent.getPassword().isEmpty()) {
                existing.getUser().setPassword(passwordEncoder.encode(updatedParent.getPassword()));
            }
        }

        parentRepository.save(existing);
    }

    public ParentDTO getParentProfileByUsername(String username) {
        Parent parent = parentRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Bu kullanıcıya ait veli profili bulunamadı!"));

        List<String> studentNames = parent.getStudents() != null ? parent.getStudents().stream()
                                                                   .map(student -> student.getFirstName() + " " + student.getLastName())
                                                                   .collect(Collectors.toList()) : List.of();

        return new ParentDTO(
                parent.getId(),
                parent.getFirstName(),
                parent.getLastName(),
                parent.getEmail(),
                parent.getPhoneNumber(),
                parent.getUser() != null ? parent.getUser().getUsername() : null,
                studentNames
        );
    }
}