package com.educonnect.service;

import com.educonnect.model.School;
import com.educonnect.repository.SchoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 🚀 EKLENDİ

import java.util.List;

@Service
@Transactional // 🚀 MİMARİ DOKUNUŞ: Okul güncellenirken güvenliği sağlar
public class SchoolService {

    private final SchoolRepository schoolRepository;

    public SchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    public School createSchool(School school) {
        return schoolRepository.save(school);
    }

    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }

    public School getSchoolById(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Okul bulunamadı: " + id));
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