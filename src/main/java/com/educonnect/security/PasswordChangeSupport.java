package com.educonnect.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * 🚀 DRY FIX: "if you're changing your OWN password, you must supply and
 * verify your current password first" was implemented twice — once in
 * AdminManagementService.updateAdmin, once in UserService.updateCurrentUserProfile
 * — with slightly different code doing the same job. One shared rule now
 * lives here, so both places (and any future one) enforce it identically.
 */
@Component
@RequiredArgsConstructor
public class PasswordChangeSupport {

    private final PasswordEncoder passwordEncoder;

    /**
     * @param suppliedCurrentPassword the current password as typed by the user, may be null
     * @param encodedExistingPassword the account's actual encoded password
     * @throws ResponseStatusException 400 if the current password is missing or wrong
     */
    public void verifyCurrentPassword(String suppliedCurrentPassword, String encodedExistingPassword) {
        if (suppliedCurrentPassword == null || suppliedCurrentPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mevcut şifre sağlanmadı.");
        }
        if (!passwordEncoder.matches(suppliedCurrentPassword, encodedExistingPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mevcut şifre yanlış.");
        }
    }
}