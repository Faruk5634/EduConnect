package com.educonnect.service;

import com.educonnect.dto.ParentDTO;
import com.educonnect.model.Parent;
import com.educonnect.repository.ParentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParentService {

    private final ParentRepository parentRepository;

    public ParentService(ParentRepository parentRepository) {
        this.parentRepository = parentRepository;
    }

    public Parent createParent(Parent parent) {
        return parentRepository.save(parent);
    }

    // İŞTE SİHİRLİ DTO DÖNÜŞÜMÜ BURADA!
    public List<ParentDTO> getAllParents() {
        List<Parent> parents = parentRepository.findAll();

        // Her bir Parent (Veli) objesini alıp ParentDTO'ya dönüştürüyoruz
        return parents.stream().map(parent -> {

            // 1. Önce bu velinin çocuklarının sadece "Ad Soyad" bilgilerini alıp bir liste yapıyoruz
            List<String> studentNames = parent.getStudents().stream()
                    .map(student -> student.getFirstName() + " " + student.getLastName())
                    .collect(Collectors.toList());

            // 2. Çiğ veriyi pişirdik, şimdi DTO tabağına koyup servis ediyoruz
            return new ParentDTO(
                    parent.getId(),
                    parent.getFirstName(),
                    parent.getLastName(),
                    parent.getEmail(),
                    studentNames // Sadece isimlerden oluşan o temiz liste!
            );
        }).collect(Collectors.toList());
    }
}