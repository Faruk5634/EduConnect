package com.educonnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for login endpoint to mitigate brute-force.
 * - Limits attempts per IP to MAX_ATTEMPTS within WINDOW duration.
 * - Non-destructive, in-memory approach (no external dependency required).
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private static class Attempt {
        AtomicInteger count = new AtomicInteger(0);
        Instant expiresAt = Instant.now();
    }

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("/api/auth/login".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        Attempt a = attempts.computeIfAbsent(ip, k -> new Attempt());
        synchronized (a) {
            Instant now = Instant.now();
            if (now.isAfter(a.expiresAt)) {
                a.count.set(1);
                a.expiresAt = now.plus(WINDOW);
            } else {
                int current = a.count.incrementAndGet();
                if (current > MAX_ATTEMPTS) {
                    response.setStatus(429);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"error\":\"Too many login attempts. Try again later.\"}");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}