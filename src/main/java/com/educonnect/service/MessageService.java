package com.educonnect.service;

import com.educonnect.dto.MessageRequest;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.model.Message;
import com.educonnect.model.Role;
import com.educonnect.model.School;
import com.educonnect.model.User;
import com.educonnect.repository.MessageRepository;
import com.educonnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageService {

    // 🚀 DRY / readability: named instead of repeated as bare string literals.
    private static final String TARGET_ALL_ADMINS = "ALL";
    private static final String TARGET_SUPER_ADMIN = "SUPER_ADMIN";

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    private School getTenantSchool(User user) {
        if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
            return null;
        }
        if (user.getSchool() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kullanıcının atanmış bir okulu yok.");
        }
        return user.getSchool();
    }

    private void assertCanMessage(User sender, User receiver) {
        School tenantSchool = getTenantSchool(sender);
        if (tenantSchool == null || receiver.getRole() == Role.ROLE_SUPER_ADMIN) {
            return;
        }
        if (receiver.getSchool() == null || !tenantSchool.getId().equals(receiver.getSchool().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Farklı okul kullanıcılarına mesaj gönderemezsiniz.");
        }
    }

    public void sendMessage(MessageRequest request) {
        User sender = userService.getCurrentUser();

        switch (request.getReceiverId()) {
            case TARGET_ALL_ADMINS -> {
                School tenantSchool = getTenantSchool(sender);
                List<User> admins = tenantSchool != null
                        ? userRepository.findBySchool_IdOrRole(tenantSchool.getId(), Role.ROLE_SUPER_ADMIN)
                        : userRepository.findByRoleIn(List.of(Role.ROLE_ADMIN, Role.ROLE_VICE_ADMIN));
                admins = admins.stream()
                        .filter(admin -> admin.getRole() == Role.ROLE_ADMIN || admin.getRole() == Role.ROLE_VICE_ADMIN || admin.getRole() == Role.ROLE_SUPER_ADMIN)
                        .toList();
                admins.forEach(admin -> saveMessage(sender, admin, request.getSubject(), request.getContent()));
            }
            case TARGET_SUPER_ADMIN -> {
                User superAdmin = userRepository.findFirstByRole(Role.ROLE_SUPER_ADMIN)
                        .orElseThrow(() -> new ResourceNotFoundException("Super Admin bulunamadı!"));
                assertCanMessage(sender, superAdmin);
                saveMessage(sender, superAdmin, request.getSubject(), request.getContent());
            }
            default -> {
                Long receiverId = parseReceiverId(request.getReceiverId());
                User receiver = userRepository.findById(receiverId)
                        .orElseThrow(() -> new ResourceNotFoundException("Alıcı bulunamadı!"));
                assertCanMessage(sender, receiver);
                saveMessage(sender, receiver, request.getSubject(), request.getContent());
            }
        }
    }

    private void saveMessage(User sender, User receiver, String subject, String content) {
        Message msg = new Message();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setSubject(subject);
        msg.setContent(content);
        msg.setSentAt(LocalDateTime.now());
        msg.setRead(false);
        msg.setSentByParent(sender.getRole() == Role.ROLE_PARENT);
        messageRepository.save(msg);
    }

    public List<Map<String, Object>> getMyMessages() {
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
            map.put("isSentByParent", m.isSentByParent());

            if (m.getSender().getId().equals(currentUser.getId())) {
                map.put("type", "SENT");
                map.put("sender", m.getReceiver().getFirstName() + " " + m.getReceiver().getLastName());
            } else {
                map.put("type", "INBOX");
                map.put("sender", m.getSender().getFirstName() + " " + m.getSender().getLastName());
            }
            formattedMessages.add(map);
        }
        return formattedMessages;
    }

    public void markAsRead(Long id) {
        User currentUser = userService.getCurrentUser();
        Message msg = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesaj bulunamadı"));
        if (!msg.getSender().getId().equals(currentUser.getId()) && !msg.getReceiver().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu mesaj üzerinde işlem yapamazsınız.");
        }
        msg.setRead(true);
        messageRepository.save(msg);
    }

    public List<Map<String, Object>> searchUsersForMessage(String keyword) {
        User currentUser = userService.getCurrentUser();
        School mySchool = currentUser.getSchool();

        if (keyword == null || keyword.trim().length() < 2) {
            return new ArrayList<>();
        }

        String searchKey = keyword.toLowerCase();

        List<User> candidates = mySchool != null
                ? userRepository.findBySchool_IdOrRole(mySchool.getId(), Role.ROLE_SUPER_ADMIN)
                : userRepository.findByRoleIn(List.of(Role.ROLE_SUPER_ADMIN));

        List<User> filteredUsers = candidates.stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .filter(u -> {
                    String fullName = (u.getFirstName() + " " + u.getLastName()).toLowerCase();
                    String uname = u.getUsername() != null ? u.getUsername().toLowerCase() : "";
                    return fullName.contains(searchKey) || uname.contains(searchKey);
                })
                .filter(u -> {
                    if (currentUser.getRole() == Role.ROLE_PARENT || currentUser.getRole() == Role.ROLE_STUDENT) {
                        return u.getRole() == Role.ROLE_TEACHER || u.getRole() == Role.ROLE_ADMIN || u.getRole() == Role.ROLE_VICE_ADMIN;
                    }
                    return true;
                })
                .limit(10)
                .toList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : filteredUsers) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", u.getId());
            map.put("fullName", u.getFirstName() + " " + u.getLastName());

            String roleStr = switch (u.getRole()) {
                case ROLE_TEACHER -> "Öğretmen";
                case ROLE_STUDENT -> "Öğrenci";
                case ROLE_PARENT -> "Veli";
                case ROLE_ADMIN, ROLE_VICE_ADMIN -> "İdareci";
                case ROLE_SUPER_ADMIN -> "Sistem Yöneticisi";
            };

            map.put("role", roleStr);
            result.add(map);
        }
        return result;
    }

    private Long parseReceiverId(String receiverId) {
        try {
            return Long.parseLong(receiverId);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geçersiz alıcı kimliği.");
        }
    }
}
