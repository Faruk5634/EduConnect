package com.educonnect;

import com.educonnect.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class MainTest {
    public static void main(String[] args) {
        System.out.println("Hello, EduConnect!\n");

        // 1. Öğretmeni oluşturduk (Constructor sırasına dikkat: id, SOYAD, AD, branş)
        Teacher teacher = new Teacher(1L, "Gökmen", "Deniz", "Mathematics");

        // 2. Öğrencileri oluşturduk
        Student student1 = new Student(1L, "Aytek", "Yılmaz", "1056");
        Student student2 = new Student(2L, "Ceren", "Kara", "1028");

        // 3. Duyuruyu oluşturduk (null yerine LocalDateTime.now() ile o anki zamanı verdik)
        Announcement announcement = new Announcement(1L, "Sınav", "Yarın matematik sınavı var.", LocalDateTime.now(), teacher, AnnouncementType.EXAM_INFO);

        // 4. İŞTE SİHRİN GERÇEKLEŞTİĞİ YER:
        // Sınıfı yaratıyoruz ve öğrencileri listeye (List.of) koyarak sınıfın İÇİNE atıyoruz!
        Classroom classroom = new Classroom(1L, "5A", 5, teacher, List.of(student1, student2), List.of(announcement));

        // --- TEST ÇIKTISI (Objeler birbiriyle nasıl konuşuyor izle) ---

        System.out.println("--- " + classroom.getName() + " SINIFI BİLGİ PANELİ ---");

        // Sınıfın içinden, rehber hocasının adına ulaşıyoruz! (İşte OOP budur)
        System.out.println("Matematik Öğretmeni: " + classroom.getHomeroomTeacher().getFirstName() + " " + classroom.getHomeroomTeacher().getLastName());

        // Sınıfın içindeki öğrenci listesinin boyutunu (size) alıyoruz
        System.out.println("Öğrenci Sayısı: " + classroom.getStudents().size());

        // Sınıfın içindeki ilk duyuruyu (0. index) alıp ekrana basıyoruz
        System.out.println("\nSON DUYURU [" + classroom.getAnnouncements().get(0).getType() + "]");
        System.out.println("Başlık: " + classroom.getAnnouncements().get(0).getTitle());
        System.out.println("İçerik: " + classroom.getAnnouncements().get(0).getContent());
        System.out.println("Tarih: " + classroom.getAnnouncements().get(0).getCreatedDate());
    }
}
