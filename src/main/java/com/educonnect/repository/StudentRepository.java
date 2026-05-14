package com.educonnect.repository;

import com.educonnect.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // JpaRepository sayesinde; save(), findAll(), findById(), delete()
    // gibi tüm metodlar şu an otomatik olarak emrine amade!
}