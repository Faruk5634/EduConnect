package com.educonnect.service;

import com.educonnect.model.User;
import com.educonnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsernameService {

    private final UserRepository userRepository;

    public String resolveForCreate(String requestedUsername, String firstName, String lastName) {
        if (requestedUsername != null && !requestedUsername.trim().isEmpty()) {
            String normalized = requestedUsername.trim();
            assertUsernameAvailable(normalized, null);
            return normalized;
        }

        return generateUniqueUsername(firstName, lastName);
    }

    public void assertUsernameAvailable(String username, Long excludedUserId) {
        userRepository.findByUsername(username).ifPresent(existingUser -> {
            if (excludedUserId == null || !existingUser.getId().equals(excludedUserId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu kullanıcı adı zaten alınmış!");
            }
        });
    }

    private String generateUniqueUsername(String firstName, String lastName) {
        String base = normalizePart(firstName) + "." + normalizePart(lastName);
        String candidate = base;
        int counter = 1;

        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = base + counter;
            counter++;
        }

        return candidate;
    }

    private String normalizePart(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "user";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase(Locale.ROOT);

        return normalized.isEmpty() ? "user" : normalized;
    }
}
