package com.educonnect.controller;

import com.educonnect.dto.ParentDTO;
import com.educonnect.dto.ParentRequest;
import com.educonnect.model.Parent;
import com.educonnect.service.ParentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PostMapping
    public Parent createParent(@Valid @RequestBody ParentRequest request) {
        return parentService.createParentWithUser(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN','TEACHER')")
    @GetMapping
    public List<ParentDTO> getAllParents() {
        return parentService.getAllParents();
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParent(@PathVariable Long id) {
        parentService.deleteParent(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','VICE_ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateParent(@PathVariable Long id, @Valid @RequestBody ParentRequest request) {
        parentService.updateParent(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ParentDTO getMyProfile(Principal principal) {
        return parentService.getParentProfileByUsername(principal.getName());
    }
}