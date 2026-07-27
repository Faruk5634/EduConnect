package com.educonnect.controller;

import com.educonnect.dto.CreateAdminRequest;
import com.educonnect.service.AdminManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
public class AdminManagementController {

    private final AdminManagementService adminManagementService;

    @PostMapping("/create-admin")
    public ResponseEntity<String> createSchoolAdmin(@RequestBody CreateAdminRequest request) {
        return ResponseEntity.ok(adminManagementService.createSchoolAdmin(request));
    }

    @GetMapping("/admins")
    public ResponseEntity<?> getAllAdmins() {
        return ResponseEntity.ok(adminManagementService.getAllAdmins());
    }

    @PutMapping("/update-admin/{id}")
    public ResponseEntity<String> updateAdmin(Principal principal, @PathVariable Long id, @RequestBody CreateAdminRequest request) {
        String currentUsername = principal != null ? principal.getName() : null;
        adminManagementService.updateAdmin(id, request, currentUsername);
        return ResponseEntity.ok("Yönetici güncellendi.");
    }

    @DeleteMapping("/delete-admin/{id}")
    public ResponseEntity<String> deleteAdmin(@PathVariable Long id) {
        adminManagementService.deleteAdmin(id);
        return ResponseEntity.ok("Yönetici silindi.");
    }
}