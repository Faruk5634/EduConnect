package com.educonnect.controller;

import com.educonnect.dto.AuthRequest;
import com.educonnect.dto.AuthResponse;
import com.educonnect.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = (authentication == null || authentication.getPrincipal().equals("anonymousUser"));
        String currentUsername = isAnonymous ? null : authentication.getName();

        return ResponseEntity.ok(authService.register(request, currentUsername, isAnonymous));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}