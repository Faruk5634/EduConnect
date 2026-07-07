package com.educonnect.repository;

import com.educonnect.model.Parent;
import com.educonnect.model.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {

    @Query("SELECT count(p) FROM Parent p WHERE p.user.school = :school")
    long countBySchool(@Param("school") School school);

    // 🚀 SIZINTIYI ÖNLEYEN KÖPRÜ: Okula göre veli getirme
    List<Parent> findByUserSchool(School school);
}