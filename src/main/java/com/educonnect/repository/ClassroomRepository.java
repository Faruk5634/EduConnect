package com.educonnect.repository;

import com.educonnect.model.Classroom;
import com.educonnect.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByHomeroomTeacher(Teacher teacher);

    // 🚀 YENİ KÖPRÜ: İsmine göre sınıf bulma (Örn: "10-A")
    Optional<Classroom> findByName(String name);
}