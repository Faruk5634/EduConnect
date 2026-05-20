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

    @PostMapping
    public Parent createParent(@Valid @RequestBody Parent parent) {
        return parentService.createParent(parent);
    }

    @GetMapping
    public List<ParentDTO> getAllParents() {
        return parentService.getAllParents();
    }
}