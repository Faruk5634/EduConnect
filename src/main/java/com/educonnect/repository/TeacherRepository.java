package com.educonnect.repository;

import com.educonnect.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    List<Teacher> findByBranchContainingIgnoreCase(String branch);

    Optional<Teacher> findByUserUsername(String username);
}
