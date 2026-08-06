package com.educonnect.config;

import com.educonnect.model.Role;
import com.educonnect.model.User;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 🔒 SECURITY FIX: previously seeded a fixed "superadmin / 123" account in
 * EVERY environment, including production, and printed it with
 * System.out.println. That's a permanent backdoor if this ever runs
 * against a real database.
 *
 * Now: only runs in the "dev" profile, and generates a random password
 * instead of a literal one, logged once via a proper logger so it doesn't
 * get lost in stdout noise and isn't a fixed, guessable value.
 *
 * Activate with: spring.profiles.active=dev (local/dev only — never in prod).
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("superadmin").isPresent()) {
            return;
        }

        String generatedPassword = passwordGenerator.generate();

        User superAdmin = User.builder()
                .username("superadmin")
                .password(passwordEncoder.encode(generatedPassword))
                .firstName("Super")
                .lastName("Admin")
                .phone("05550000000")
                .role(Role.ROLE_SUPER_ADMIN)
                .build();

        userRepository.save(superAdmin);

        log.warn("=========================================================");
        log.warn(" DEV bootstrap: created 'superadmin' account.");
        log.warn(" username: superadmin");
        log.warn(" password: {}", generatedPassword);
        log.warn(" Change this password after first login. This message");
        log.warn(" only appears because the 'dev' profile is active.");
        log.warn("=========================================================");
    }
}