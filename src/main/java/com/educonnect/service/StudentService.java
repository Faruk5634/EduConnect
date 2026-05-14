package com.educonnect.service;

import com.educonnect.model.Parent;
import com.educonnect.model.Student;
import com.educonnect.repository.ParentRepository;
import com.educonnect.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // SİHİRLİ KELİME: Spring'e "Bu sınıf beynimizdir, iş mantığı buradadır" diyoruz.
public class StudentService {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;

    // Constructor (Depoları buraya alıyoruz)
    public StudentService(StudentRepository studentRepository, ParentRepository parentRepository) {
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
    }

    // 1. Öğrenci Kaydetme Mantığı
    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    // 2. Tüm Öğrencileri Getirme Mantığı
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // 3. Veli Atama Mantığı (Uzun kodumuz artık burada güvende)
    public Student assignParent(Long studentId, Long parentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı"));

        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Veli bulunamadı"));

        student.setParent(parent);
        return studentRepository.save(student);
    }
}