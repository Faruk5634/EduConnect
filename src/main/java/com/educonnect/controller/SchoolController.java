package com.educonnect.controller;

import com.educonnect.dto.SchoolStatsDTO;
import com.educonnect.model.School;
import com.educonnect.service.SchoolService;
import com.educonnect.service.SchoolStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;
    private final SchoolStatsService schoolStatsService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/schools")
    public List<School> getAllSchools() {
        return schoolService.getAllSchools();
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/schools")
    public School createSchool(@RequestBody School school) {
        return schoolService.createSchool(school);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/schools/{id}")
    public School updateSchool(@PathVariable Long id, @RequestBody School school) {
        return schoolService.updateSchool(id, school);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/schools/{id}")
    public ResponseEntity<Void> deleteSchool(@PathVariable Long id) {
        schoolService.deleteSchool(id);
        return ResponseEntity.noContent().build();
    }

    // Any authenticated user can see stats for their OWN school —
    // SchoolStatsService already scopes this to userService.getCurrentUser().getSchool().
    @GetMapping("/school/stats")
    public ResponseEntity<SchoolStatsDTO> getSchoolStats() {
        return ResponseEntity.ok(schoolStatsService.getCurrentSchoolStats());
    }
}