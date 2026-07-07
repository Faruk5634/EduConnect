package com.educonnect.repository;

import com.educonnect.model.Classroom;
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
    List<Student> findByClassroom(Classroom classroom);
    Optional<Student> findBySchoolNumber(String schoolNumber);
    List<Student> findByFirstNameContainingIgnoreCase(String firstName);

    // 🚀 SIZINTIYI ÖNLEYEN KÖPRÜ: Okula göre öğrenci getirme
    List<Student> findByUserSchool(School school);

    @Query("SELECT count(s) FROM Student s WHERE s.classroom.school = :school")
    long countBySchool(@Param("school") School school);
}