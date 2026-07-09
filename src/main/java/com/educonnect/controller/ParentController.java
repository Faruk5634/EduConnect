package com.educonnect.controller;

import com.educonnect.dto.ParentDTO;
import com.educonnect.model.Parent;
import com.educonnect.service.ParentService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/parents")
public class ParentController {

    private final ParentService parentService;

    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }

    // 🚀 EKLENDİ: Şifrenin yolda kaybolmasını engelleyen Taşıyıcı Kutu (DTO)
    public static class ParentRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String username;
        private String password;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping
    public Parent createParent(@Valid @RequestBody ParentRequest request) {
        Parent parent = new Parent();
        parent.setFirstName(request.getFirstName());
        parent.setLastName(request.getLastName());
        parent.setEmail(request.getEmail());
        parent.setPhoneNumber(request.getPhoneNumber());
        parent.setUsername(request.getUsername());
        parent.setPassword(request.getPassword()); // 🚀 Şifre güvenle aktarıldı
        return parentService.createParentWithUser(parent);
    }

    @GetMapping
    public List<ParentDTO> getAllParents() {
        return parentService.getAllParents();
    }

    @DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<Void> deleteParent(@PathVariable Long id) {
        parentService.deleteParent(id);
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public org.springframework.http.ResponseEntity<?> updateParent(@PathVariable Long id, @RequestBody ParentRequest request) {
        Parent parent = new Parent();
        parent.setFirstName(request.getFirstName());
        parent.setLastName(request.getLastName());
        parent.setEmail(request.getEmail());
        parent.setPhoneNumber(request.getPhoneNumber());
        parent.setUsername(request.getUsername());
        parent.setPassword(request.getPassword()); // 🚀 Şifre güvenle aktarıldı
        parentService.updateParent(id, parent);
        return org.springframework.http.ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ParentDTO getMyProfile(java.security.Principal principal) {
        return parentService.getParentProfileByUsername(principal.getName());
    }
}