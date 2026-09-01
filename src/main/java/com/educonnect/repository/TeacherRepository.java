package com.educonnect.repository;

import com.educonnect.model.School;
import com.educonnect.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    List<Teacher> findByBranchContainingIgnoreCase(String branch);
    List<Teacher> findByUserSchoolAndBranchContainingIgnoreCase(School school, String branch);
    Optional<Teacher> findByUserUsername(String username);

    // 🚀 SIZINTIYI ÖNLEYEN KÖPRÜ: Okula göre öğretmen getirme
    List<Teacher> findByUserSchool(School school);

    @Query("SELECT count(t) FROM Teacher t WHERE t.user.school = :school")
    long countBySchool(@Param("school") School school);
}
