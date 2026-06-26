package com.educonnect.repository;

import com.educonnect.model.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    // İleride isme göre okul aramak istersek diye hazırda bekleyen bir komut
    Optional<School> findByName(String name);
}
