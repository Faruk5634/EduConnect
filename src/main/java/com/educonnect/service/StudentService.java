package com.educonnect.service;

import com.educonnect.dto.StudentDTO;
import com.educonnect.model.Parent;
import com.educonnect.model.Student;
import com.educonnect.repository.ParentRepository;
import com.educonnect.repository.StudentRepository;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


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

    // 2. Tüm Öğrencileri Getirme Mantığı (DTO DÖNÜŞÜMLÜ)
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::convertToDTO) // Her bir öğrenciyi DTO'ya çevir
                .collect(Collectors.toList());
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

    // Okul numarasına göre öğrenci bulma iş mantığı
    public Student getStudentBySchoolNumber(String schoolNumber) {
        return studentRepository.findBySchoolNumber(schoolNumber)
                .orElseThrow(() -> new RuntimeException("Bu okul numarasına ait bir öğrenci bulunamadı!"));
    }
    //Öğrenci isminde geçen harflere göre arama yapma iş mantığı

    // İsme Göre Akıllı Arama Mantığı
    public List<StudentDTO> searchStudentsByFirstName(String firstName) {
        return studentRepository.findByFirstNameContainingIgnoreCase(firstName)
                .stream()
                .map(this::convertToDTO) // Önceden yazdığımız o harika DTO çevirici yamak çalışıyor!
                .collect(Collectors.toList());
    }

    // Aşçı Yamağı: Çiğ Student objesini, şık StudentDTO'ya çevirir.
    private StudentDTO convertToDTO(Student student) {
        // Eğer öğrencinin velisi varsa adını soyadını birleştir, yoksa "Veli Atanmadı" yaz.
        String parentName = (student.getParent() != null)
                ? student.getParent().getFirstName() + " " + student.getParent().getLastName()
                : "Veli Atanmadı";

        return new StudentDTO(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getSchoolNumber(),
                parentName
        );
    }

    // SAYFALAMA MANTIĞI: Verileri porsiyonlara bölerek getirir
    public Page<StudentDTO> getStudentsPaginated(int page, int size) {
        // Hangi sayfa (page) ve her sayfada kaç veri olacak (size) ayarlıyoruz
        Pageable pageable = PageRequest.of(page, size);

        // Spring'in harika özelliği: Page objesinin kendi map() metodu vardır!
        return studentRepository.findAll(pageable)
                .map(this::convertToDTO); // Yine o şık garsonumuzu (DTO) kullanıyoruz
    }


    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    public void updateStudent(Long id, Student updatedStudent) {
        // 1. Önce veritabanında bu öğrenci var mı diye buluyoruz
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı!"));

        // 2. Eski bilgileri, React'ten gelen yeni bilgilerle değiştiriyoruz
        existingStudent.setSchoolNumber(updatedStudent.getSchoolNumber());
        existingStudent.setFirstName(updatedStudent.getFirstName());
        existingStudent.setLastName(updatedStudent.getLastName());

        // 3. Yeni halini veritabanına geri kaydediyoruz (save metodu ID varsa günceller, yoksa yeni ekler)
        studentRepository.save(existingStudent);
    }

}