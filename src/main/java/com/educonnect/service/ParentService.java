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
    // 🚀 Yeni Bağımlılıklar Eklendi
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ParentService(ParentRepository parentRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.parentRepository = parentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 🚀 SİHİRLİ DOKUNUŞ: Veli Eklerken Otomatik User Hesabı Açma
    public Parent createParentWithUser(Parent parent) {
        // Öncelik E-Posta. E-posta yoksa Ad.Soyad üzerinden isim oluşturulur.
        String generatedUsername = parent.getEmail();

        if (generatedUsername == null || generatedUsername.isEmpty()) {
            generatedUsername = (parent.getFirstName() + "." + parent.getLastName())
                    .toLowerCase()
                    .replace("ş", "s").replace("ı", "i").replace("ğ", "g")
                    .replace("ö", "o").replace("ç", "c").replace("ü", "u")
                    .replace(" ", "");
        }

        if (userRepository.findByUsername(generatedUsername).isPresent()) {
            generatedUsername = generatedUsername + "1";
        }

        User user = User.builder()
                .username(generatedUsername)
                .password(passwordEncoder.encode("123456"))
                .role(Role.ROLE_PARENT)
                .build();

        User savedUser = userRepository.save(user);

        // Veliye kullanıcıyı bağla (Parent modeline eklediğin setUser metodu ile)
        parent.setUser(savedUser);

        return parentRepository.save(parent);
    }

    public Parent createParent(Parent parent) {
        return parentRepository.save(parent);
    }

    public List<ParentDTO> getAllParents() {
        List<Parent> parents = parentRepository.findAll();

        return parents.stream().map(parent -> {
            List<String> studentNames = parent.getStudents().stream()
                    .map(student -> student.getFirstName() + " " + student.getLastName())
                    .collect(Collectors.toList());

            return new ParentDTO(
                    parent.getId(),
                    parent.getFirstName(),
                    parent.getLastName(),
                    parent.getEmail(),
                    studentNames
            );
        }).collect(Collectors.toList());
    }

    public void deleteParent(Long id) {
        parentRepository.deleteById(id);
    }

    public void updateParent(Long id, com.educonnect.model.Parent updatedParent) {
        com.educonnect.model.Parent existing = parentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veli bulunamadı!"));

        existing.setFirstName(updatedParent.getFirstName());
        existing.setLastName(updatedParent.getLastName());
        existing.setEmail(updatedParent.getEmail());

        parentRepository.save(existing);
    }

    public ParentDTO getParentProfileByUsername(String username) {
        Parent parent = parentRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Bu kullanıcıya ait veli profili bulunamadı!"));

        List<String> studentNames = parent.getStudents().stream()
                .map(student -> student.getFirstName() + " " + student.getLastName())
                .collect(Collectors.toList());

        return new ParentDTO(
                parent.getId(),
                parent.getFirstName(),
                parent.getLastName(),
                parent.getEmail(),
                studentNames
        );
    }

}