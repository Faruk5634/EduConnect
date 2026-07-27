package com.educonnect.service;

import com.educonnect.dto.MessageRequest;
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

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public void sendMessage(MessageRequest request) {
        User sender = userService.getCurrentUser();

        if ("ALL".equals(request.getReceiverId())) {
            List<User> admins = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ROLE_ADMIN || u.getRole() == Role.ROLE_VICE_ADMIN)
                    .toList();
            for (User admin : admins) {
                saveMessage(sender, admin, request.getSubject(), request.getContent());
            }
        } else if ("SUPER_ADMIN".equals(request.getReceiverId())) {
            User superAdmin = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ROLE_SUPER_ADMIN)
                    .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Super Admin bulunamadı!"));
            saveMessage(sender, superAdmin, request.getSubject(), request.getContent());
        } else {
            User receiver = userRepository.findById(Long.parseLong(request.getReceiverId()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alıcı bulunamadı!"));
            saveMessage(sender, receiver, request.getSubject(), request.getContent());
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
        Message msg = messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesaj bulunamadı"));
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

        List<User> filteredUsers = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .filter(u -> {
                    String fullName = (u.getFirstName() + " " + u.getLastName()).toLowerCase();
                    String uname = u.getUsername() != null ? u.getUsername().toLowerCase() : "";
                    return fullName.contains(searchKey) || uname.contains(searchKey);
                })
                .filter(u -> {
                    boolean isSameSchool = (u.getSchool() != null && mySchool != null && u.getSchool().getId().equals(mySchool.getId()))
                            || u.getRole() == Role.ROLE_SUPER_ADMIN;

                    if (!isSameSchool) return false;

                    if (currentUser.getRole() == Role.ROLE_PARENT || currentUser.getRole() == Role.ROLE_STUDENT) {
                        return u.getRole() == Role.ROLE_TEACHER || u.getRole() == Role.ROLE_ADMIN || u.getRole() == Role.ROLE_VICE_ADMIN;
                    } else {
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

            String roleStr = switch (u.getRole()) {
                case ROLE_TEACHER -> "Öğretmen";
                case ROLE_STUDENT -> "Öğrenci";
                case ROLE_PARENT -> "Veli";
                case ROLE_ADMIN, ROLE_VICE_ADMIN -> "İdareci";
                case ROLE_SUPER_ADMIN -> "Sistem Yöneticisi";
                default -> "Kullanıcı";
            };

            map.put("role", roleStr);
            result.add(map);
        }
        return result;
    }
}