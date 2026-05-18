package com.educonnect.repository;

import com.educonnect.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Okul numarasına göre öğrenci bulma iş mantığı
    Optional<Student> findBySchoolNumber(String schoolNumber);

    //Öğrenci isminde geçen harflere göre arama yapma iş mantığı
    List<Student> findByNameContainingIgnoreCase(String name);
}