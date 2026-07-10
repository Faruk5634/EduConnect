package com.educonnect.service;

import com.educonnect.dto.ParentDTO;
import com.educonnect.model.Parent;
import com.educonnect.model.Role;
import com.educonnect.model.User;
import com.educonnect.repository.ParentRepository;
import com.educonnect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 🚀 EKLENDİ

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional // 🚀 EKLENDİ
public class ParentService {

    private final ParentRepository parentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public ParentService(ParentRepository parentRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, UserService userService) {
        this.parentRepository = parentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    public Parent createParentWithUser(Parent parent) {
        User admin = userService.getCurrentUser();

        String usernameToUse = parent.getUsername();
        String passwordToUse = parent.getPassword();

        if (usernameToUse == null || usernameToUse.trim().isEmpty()) {
            usernameToUse = (parent.getFirstName() + "." + parent.getLastName())
                    .toLowerCase()
                    .replaceAll("[çÇ]", "c").replaceAll("[ğĞ]", "g").replaceAll("[ıİ]", "i")
                    .replaceAll("[öÖ]", "o").replaceAll("[şŞ]", "s").replaceAll("[üÜ]", "u")
                    .replaceAll("\\s+", "");

            if (userRepository.findByUsername(usernameToUse).isPresent()) {
                usernameToUse = usernameToUse + "1";
            }
        } else {
            if (userRepository.findByUsername(usernameToUse).isPresent()) {
                    throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Bu kullanıcı adı zaten alınmış!");
            }
        }

        if (passwordToUse == null || passwordToUse.trim().isEmpty()) {
            passwordToUse = "123456";
        }

        User user = new User();
        user.setUsername(usernameToUse);
        user.setPassword(passwordEncoder.encode(passwordToUse));
        user.setRole(Role.ROLE_PARENT);
        user.setFirstName(parent.getFirstName());
        user.setLastName(parent.getLastName());
        user.setSchool(admin.getSchool());

        User savedUser = userRepository.save(user);
        parent.setUser(savedUser);

        return parentRepository.save(parent);
    }

    public List<ParentDTO> getAllParents() {
        User admin = userService.getCurrentUser();
        List<Parent> parents = parentRepository.findByUserSchool(admin.getSchool());

        return parents.stream().map(parent -> {
            List<String> studentNames = parent.getStudents() != null ? parent.getStudents().stream()

                                                                       .map(student -> student.getFirstName() + " " + student.getLastName() + "|" + student.getSchoolNumber())
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
        }).collect(Collectors.toList());
    }

    public void deleteParent(Long id) {
        parentRepository.deleteById(id);
    }

    public void updateParent(Long id, Parent updatedParent) {
        Parent existing = parentRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Veli bulunamadı!"));

        existing.setFirstName(updatedParent.getFirstName());
        existing.setLastName(updatedParent.getLastName());
        existing.setEmail(updatedParent.getEmail());
        existing.setPhoneNumber(updatedParent.getPhoneNumber());

        if (existing.getUser() != null) {
            User user = existing.getUser();
            user.setFirstName(updatedParent.getFirstName());
            user.setLastName(updatedParent.getLastName());

            if (updatedParent.getUsername() != null && !updatedParent.getUsername().trim().isEmpty()) {
                user.setUsername(updatedParent.getUsername());
            }
            if (updatedParent.getPassword() != null && !updatedParent.getPassword().trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updatedParent.getPassword()));
            }

            userRepository.save(user);
        }

        parentRepository.save(existing);
    }

    public ParentDTO getParentProfileByUsername(String username) {
        Parent parent = parentRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Bu kullanıcıya ait veli profili bulunamadı!"));

        List<String> studentNames = parent.getStudents() != null ? parent.getStudents().stream()
                                                                   .map(student -> student.getFirstName() + " " + student.getLastName() + "|" + student.getSchoolNumber())
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