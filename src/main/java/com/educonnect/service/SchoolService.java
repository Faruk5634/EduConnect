package com.educonnect.service;

import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.model.School;
import com.educonnect.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository schoolRepository;

    public School createSchool(School school) {
        return schoolRepository.save(school);
    }

    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }

    public School getSchoolById(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Okul bulunamadı: " + id));
    }

    public void deleteSchool(Long id) {
        School school = getSchoolById(id);
        schoolRepository.delete(school);
    }

    public School updateSchool(Long id, School updatedDetails) {
        School existing = getSchoolById(id);
        existing.setName(updatedDetails.getName());
        existing.setSchoolType(updatedDetails.getSchoolType());
        existing.setCity(updatedDetails.getCity());
        existing.setDistrict(updatedDetails.getDistrict());
        existing.setNeighborhood(updatedDetails.getNeighborhood());
        existing.setPhone(updatedDetails.getPhone());
        existing.setEmail(updatedDetails.getEmail());
        existing.setAddress(updatedDetails.getAddress());
        return schoolRepository.save(existing);
    }
}