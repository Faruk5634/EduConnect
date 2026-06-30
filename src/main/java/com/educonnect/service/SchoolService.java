package com.educonnect.service;

import com.educonnect.model.School;
import com.educonnect.repository.SchoolRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolService {

    private final SchoolRepository schoolRepository;

    public SchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    // Yeni Okul Ekle
    public School createSchool(School school) {
        // İleride aynı isimde okul var mı diye kontrol mekanizmaları buraya eklenebilir
        return schoolRepository.save(school);
    }

    // Tüm Okulları Listele
    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }

    // ID ile tek bir okul getir
    public School getSchoolById(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Okul bulunamadı: " + id));
    }

    // Okul Sil
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