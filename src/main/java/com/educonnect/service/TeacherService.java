package com.educonnect.service;

import com.educonnect.dto.TeacherDTO;
import com.educonnect.model.Classroom;
import com.educonnect.model.Role;
import com.educonnect.model.Teacher;
import com.educonnect.model.User;
import com.educonnect.repository.ClassroomRepository;
import com.educonnect.repository.TeacherRepository;
import com.educonnect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    // 🚀 Yeni Bağımlılıklar Eklendi
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor güncellendi
    public TeacherService(TeacherRepository teacherRepository,
                          ClassroomRepository classroomRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 🚀 SİHİRLİ DOKUNUŞ: Öğretmen Eklerken Otomatik User Hesabı Açma
    public Teacher createTeacherWithUser(Teacher teacher) {
        // Standart Öğretmen Kullanıcı Adı: "isim.soyisim" (Örn: gokmen.deniz)
        String generatedUsername = (teacher.getFirstName() + "." + teacher.getLastName())
                .toLowerCase()
                .replace("ş", "s").replace("ı", "i").replace("ğ", "g")
                .replace("ö", "o").replace("ç", "c").replace("ü", "u")
                .replace(" ", ""); // Eğer iki ismi varsa boşlukları sil

        // Aynı isimde biri varsa basit bir mantıkla sonuna 1 ekle
        if (userRepository.findByUsername(generatedUsername).isPresent()) {
            generatedUsername = generatedUsername + "1";
        }

        // 1. Yeni Öğretmen Kullanıcısını Oluştur (Standart Şifre: 123456)
        User user = User.builder()
                .username(generatedUsername)
                .password(passwordEncoder.encode("123456"))
                .role(Role.ROLE_TEACHER)
                .build();

        User savedUser = userRepository.save(user);

        // 2. Oluşan hesabı öğretmene bağla
        teacher.setUser(savedUser);

        // 3. Öğretmeni kaydet
        return teacherRepository.save(teacher);
    }

    // Eski create metodu (geriye dönük uyumluluk için bozulmadı)
    public Teacher createTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    public List<TeacherDTO> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    // TeacherService sınıfının içine eklenecek dönüşüm mantığı:
    private TeacherDTO convertToDTO(Teacher teacher) {
        List<TeacherDTO.ClassroomInfo> classInfos = teacher.getHomeroomClasses() != null
                ? teacher.getHomeroomClasses().stream()
                  .map(c -> new TeacherDTO.ClassroomInfo(c.getId(), c.getName()))
                  .collect(java.util.stream.Collectors.toList())
                : List.of();

        return new TeacherDTO(
                teacher.getId(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getBranch(),
                classInfos // List<String> yerine yeni listemizi verdik
        );
    }

    // Branşa Göre Akıllı Arama Mantığı
    public List<TeacherDTO> searchTeachersByBranch(String branch) {
        return teacherRepository.findByBranchContainingIgnoreCase(branch)
                .stream()
                .map(this::convertToDTO) // Çiğ veriyi şık DTO tabağına alıyoruz
                .collect(java.util.stream.Collectors.toList());
    }

    public TeacherDTO getTeacherProfileByUsername(String username) {
        Teacher teacher = teacherRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Eyvah! Bu kullanıcıya ait bir öğretmen profili bulunamadı."));

        return convertToDTO(teacher);
    }

    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Öğretmen bulunamadı!"));

        // 1. Bu öğretmen bir sınıfın rehberiyse, önce o sınıftaki rehberini boşalt (Null yap)
        List<Classroom> classrooms = classroomRepository.findByHomeroomTeacher(teacher);
        for (Classroom cls : classrooms) {
            cls.setHomeroomTeacher(null);
            classroomRepository.save(cls);
        }

        // 2. Şimdi güvenle silebilirsin
        teacherRepository.delete(teacher);
    }

    public void updateTeacher(Long id, Teacher updatedTeacher) {
        Teacher existingTeacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Öğretmen bulunamadı!"));

        existingTeacher.setFirstName(updatedTeacher.getFirstName());
        existingTeacher.setLastName(updatedTeacher.getLastName());
        existingTeacher.setBranch(updatedTeacher.getBranch());

        teacherRepository.save(existingTeacher);
    }
}