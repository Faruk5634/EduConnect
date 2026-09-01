package com.educonnect.repository;

import com.educonnect.model.Classroom;
import com.educonnect.model.School;
import com.educonnect.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByHomeroomTeacher(Teacher teacher);
    Optional<Classroom> findByIdAndSchool(Long id, School school);

    // Scoped to school, so two schools can each have their own "9-A" etc.
    Optional<Classroom> findByNameAndSchool(String name, School school);

    // 🚀 REMOVED: findByName(String name) — the old, unscoped lookup that
    // findByNameAndSchool replaced. It was marked "for backward
    // compatibility, delete later" and nothing in the service layer called
    // it anymore.

    long countBySchool(School school);
    List<Classroom> findBySchool(School school);
}
