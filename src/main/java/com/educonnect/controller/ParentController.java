package com.educonnect.controller;

import com.educonnect.dto.ParentDTO;
import com.educonnect.model.Parent;
import com.educonnect.service.ParentService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // 🚀 CORS Koruması eklendi
@RequestMapping("/api/parents")
public class ParentController {

    private final ParentService parentService;

    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }

    @PostMapping
    public Parent createParent(@Valid @RequestBody Parent parent) {
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
    public org.springframework.http.ResponseEntity<?> updateParent(@PathVariable Long id, @RequestBody Parent parent) {
        parentService.updateParent(id, parent);
        return org.springframework.http.ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ParentDTO getMyProfile(java.security.Principal principal) {
        return parentService.getParentProfileByUsername(principal.getName());
    }
}