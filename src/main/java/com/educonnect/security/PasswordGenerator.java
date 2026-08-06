package com.educonnect.security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 🚀 DRY FIX: ParentService, StudentService, and TeacherService each
 * independently fell back to the literal string "123456" when no password
 * was supplied at account creation. That's (a) duplicated logic and
 * (b) a predictable default password for every new account in the system —
 * a real credential-stuffing risk, not just a style issue.
 *
 * This is now the single place that decides what a "no password supplied"
 * account gets: a random, sufficiently long temporary password. Pair this
 * with a "must change password on first login" flag on User if you want to
 * force a reset (left as a follow-up — needs a schema change).
 */
@Component
public class PasswordGenerator {

    private static final String CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
    private static final int DEFAULT_LENGTH = 12;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        return generate(DEFAULT_LENGTH);
    }

    public String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}