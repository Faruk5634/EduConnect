package com.educonnect.model;

public enum Role {
    ROLE_SUPER_ADMIN, // 🚀 YENİ: Genel Merkez (Tüm okulları ve sistemi görür)
    ROLE_ADMIN,       // Okul İdaresi (Sadece kendi okulunun müdürüdür)
    ROLE_TEACHER,     // Öğretmenler
    ROLE_PARENT,      // Veliler
    ROLE_STUDENT      // Öğrenciler
}