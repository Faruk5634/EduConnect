package com.educonnect.controller;

import com.educonnect.dto.MessageRequest;
import com.educonnect.model.Message;
import com.educonnect.model.Role;
import com.educonnect.model.School;
import com.educonnect.model.User;
import com.educonnect.repository.MessageRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    // 📩 MESAJ GÖNDERME
    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequest request) {
        User sender = userService.getCurrentUser();

        // 1. Kime: HERKESE (Tüm Müdürlere)
        if ("ALL".equals(request.getReceiverId())) {
            List<User> admins = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ROLE_ADMIN || u.getRole() == Role.ROLE_VICE_ADMIN)
                    .toList();
            for (User admin : admins) {
                saveMessage(sender, admin, request.getSubject(), request.getContent());
            }
        }
        // 2. Kime: SİSTEM YÖNETİMİNE (Super Admin)
        else if ("SUPER_ADMIN".equals(request.getReceiverId())) {
            User superAdmin = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ROLE_SUPER_ADMIN)
                    .findFirst().orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Super Admin bulunamadı!"));
            saveMessage(sender, superAdmin, request.getSubject(), request.getContent());
        }
        // 3. Kime: BELİRLİ BİR KİŞİYE (Akıllı Arama Motorundan Gelen User ID)
        else {
            User receiver = userRepository.findById(Long.parseLong(request.getReceiverId()))
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Alıcı bulunamadı!"));
            saveMessage(sender, receiver, request.getSubject(), request.getContent());
        }

        return ResponseEntity.ok("Mesaj başarıyla gönderildi.");
    }

    private void saveMessage(User sender, User receiver, String subject, String content) {
        Message msg = new Message();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setSubject(subject);
        msg.setContent(content);
        msg.setSentAt(LocalDateTime.now());
        msg.setRead(false);

        // 🛡️ SİHİRLİ DOKUNUŞ: Eğer gönderen veli ise Veli Mührünü bas!
        if (sender.getRole() == Role.ROLE_PARENT) {
            msg.setSentByParent(true);
        } else {
            msg.setSentByParent(false);
        }

        messageRepository.save(msg);
    }

    // 📬 MESAJLARI GETİRME (Hem Inbox hem Sent)
    @GetMapping
    public ResponseEntity<?> getMyMessages() {
        User currentUser = userService.getCurrentUser();
        List<Message> rawMessages = messageRepository.findBySenderOrReceiverOrderBySentAtDesc(currentUser, currentUser);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        List<Map<String, Object>> formattedMessages = new ArrayList<>();

        for (Message m : rawMessages) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("subject", m.getSubject());
            map.put("content", m.getContent());
            map.put("date", m.getSentAt().format(dateFormatter));
            map.put("time", m.getSentAt().format(timeFormatter));
            map.put("isRead", m.isRead());

            // 🛡️ VELİ MÜHRÜNÜ REACT TARAFI İÇİN PAKETLE
            map.put("isSentByParent", m.isSentByParent());

            // Mesajı gönderen BEN isem -> Kutu: SENT, Ekranda Gösterilen İsim: Alıcının İsmi
            if (m.getSender().getId().equals(currentUser.getId())) {
                map.put("type", "SENT");
                map.put("sender", m.getReceiver().getFirstName() + " " + m.getReceiver().getLastName());
            }
            // Mesaj bana GELMİŞSE -> Kutu: INBOX, Ekranda Gösterilen İsim: Gönderenin İsmi
            else {
                map.put("type", "INBOX");
                map.put("sender", m.getSender().getFirstName() + " " + m.getSender().getLastName());
            }
            formattedMessages.add(map);
        }

        return ResponseEntity.ok(formattedMessages);
    }

    // 👁️ MESAJI OKUNDU İŞARETLEME
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        Message msg = messageRepository.findById(id).orElseThrow();
        msg.setRead(true);
        messageRepository.save(msg);
        return ResponseEntity.ok("Okundu");
    }

    // 🚀 GÜNCELLENMİŞ AKILLI ARAMA MOTORU (OKUL FİLTRELİ - VERİ SIZINTISI KESİN ÇÖZÜM)
    @GetMapping("/search-users")
    public ResponseEntity<?> searchUsersForMessage(@RequestParam String keyword) {
        User currentUser = userService.getCurrentUser();
        School mySchool = currentUser.getSchool();

        if (keyword == null || keyword.trim().length() < 2) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        String searchKey = keyword.toLowerCase();

        List<User> filteredUsers = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(currentUser.getId())) // Kendini çıkar
                .filter(u -> {
                    String fullName = (u.getFirstName() + " " + u.getLastName()).toLowerCase();
                    String uname = u.getUsername() != null ? u.getUsername().toLowerCase() : "";
                    return fullName.contains(searchKey) || uname.contains(searchKey);
                })
                .filter(u -> {
                    // 🛡️ EN ÖNEMLİ KURAL: Super Admin hariç herkes MUTLAKA kendi okulunda olmalı!
                    boolean isSameSchool = (u.getSchool() != null && mySchool != null && u.getSchool().getId().equals(mySchool.getId()))
                            || u.getRole() == Role.ROLE_SUPER_ADMIN;

                    if (!isSameSchool) return false; // Okullar uyuşmuyorsa direkt ele!

                    // 1. VELİ İSE: Sadece kendi okulundaki İdareci ve Öğretmenleri bulabilir.
                    if (currentUser.getRole() == Role.ROLE_PARENT) {
                        return u.getRole() == Role.ROLE_TEACHER || u.getRole() == Role.ROLE_ADMIN || u.getRole() == Role.ROLE_VICE_ADMIN;
                    }
                    // 2. ÖĞRENCİ İSE: Sadece kendi okulundaki İdareci ve Öğretmenleri görebilir.
                    else if (currentUser.getRole() == Role.ROLE_STUDENT) {
                        return u.getRole() == Role.ROLE_TEACHER || u.getRole() == Role.ROLE_ADMIN || u.getRole() == Role.ROLE_VICE_ADMIN;
                    }
                    // 3. ÖĞRETMEN / İDARECİ İSE: Kendi okulundaki Öğrenci, Öğretmen ve İdarecileri görebilir.
                    else {
                        return true;
                    }
                })
                .limit(10)
                .toList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : filteredUsers) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", u.getId());
            map.put("fullName", u.getFirstName() + " " + u.getLastName());

            String roleStr = "Kullanıcı";
            if (u.getRole() == Role.ROLE_TEACHER) roleStr = "Öğretmen";
            else if (u.getRole() == Role.ROLE_STUDENT) roleStr = "Öğrenci";
            else if (u.getRole() == Role.ROLE_PARENT) roleStr = "Veli";
            else if (u.getRole() == Role.ROLE_ADMIN || u.getRole() == Role.ROLE_VICE_ADMIN) roleStr = "İdareci";
            else if (u.getRole() == Role.ROLE_SUPER_ADMIN) roleStr = "Sistem Yöneticisi";

            map.put("role", roleStr);
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }
}