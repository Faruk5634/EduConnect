package com.educonnect.repository;

import com.educonnect.model.School;
import com.educonnect.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findBySchoolNumber(String schoolNumber);
    List<Student> findByFirstNameContainingIgnoreCase(String firstName);

    // 🚀 ESKİSİ: List<Student> findByUserSchool(School school);
    // 🚀 YENİSİ: Doğrudan öğrencinin okuluna bakıyoruz!
    List<Student> findBySchool(School school);

    List<Student> findByClassroom(com.educonnect.model.Classroom classroom);

    // 🚀 İstatistik için öğrenci sayısını çekerken de doğrudan kendi mühürüne bakıyoruz
    @Query("SELECT count(s) FROM Student s WHERE s.school = :school")
    long countBySchool(@Param("school") School school);
}