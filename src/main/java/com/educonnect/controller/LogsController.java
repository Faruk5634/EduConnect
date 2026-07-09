package com.educonnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LogsController {

    // Simple placeholder endpoint for system logs. Returns empty list by default.
    @GetMapping("/logs")
    public ResponseEntity<List<Map<String, Object>>> getLogs() {
        // For now return an empty list so frontend shows empty-state instead of 500
        List<Map<String, Object>> logs = new ArrayList<>();
        return ResponseEntity.ok(logs);
    }
}


