package com.educonnect.controller;

import com.educonnect.dto.ParentDTO;
import com.educonnect.model.Parent;
import com.educonnect.service.ParentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor // 🚀 MİMARİ DOKUNUŞ: Uzun Constructor bloğunu sildi!
public class ParentController {

    private final ParentService parentService;

    // 🚀 DOKUNUŞ: Tüm manuel get/set metotları @Data ile tek satıra indirildi
    @Data
    public static class ParentRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String username;
        private String password;
    }

    @PostMapping
    public Parent createParent(@Valid @RequestBody ParentRequest request) {
        Parent parent = new Parent();
        parent.setFirstName(request.getFirstName());
        parent.setLastName(request.getLastName());
        parent.setEmail(request.getEmail());
        parent.setPhoneNumber(request.getPhoneNumber());
        parent.setUsername(request.getUsername());
        parent.setPassword(request.getPassword());
        return parentService.createParentWithUser(parent);
    }

    @GetMapping
    public List<ParentDTO> getAllParents() {
        return parentService.getAllParents();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParent(@PathVariable Long id) {
        parentService.deleteParent(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateParent(@PathVariable Long id, @RequestBody ParentRequest request) {
        Parent parent = new Parent();
        parent.setFirstName(request.getFirstName());
        parent.setLastName(request.getLastName());
        parent.setEmail(request.getEmail());
        parent.setPhoneNumber(request.getPhoneNumber());
        parent.setUsername(request.getUsername());
        parent.setPassword(request.getPassword());
        parentService.updateParent(id, parent);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ParentDTO getMyProfile(Principal principal) {
        return parentService.getParentProfileByUsername(principal.getName());
    }
}