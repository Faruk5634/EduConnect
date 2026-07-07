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

    // 🚨 SAATLİ BOMBA İMHA EDİLDİ: Sadece isme göre aramak yerine, "O okulun o isimdeki sınıfını" arıyoruz
    Optional<Classroom> findByNameAndSchool(String name, School school);

    // Geriye dönük uyumluluk için (İleride tamamen sileceğiz)
    Optional<Classroom> findByName(String name);

    long countBySchool(School school);
    List<Classroom> findBySchool(School school);
}