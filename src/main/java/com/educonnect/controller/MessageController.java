package com.educonnect.controller;

import com.educonnect.dto.MessageRequest;
import com.educonnect.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestBody MessageRequest request) {
        messageService.sendMessage(request);
        return ResponseEntity.ok("Mesaj başarıyla gönderildi.");
    }

    @GetMapping
    public ResponseEntity<?> getMyMessages() {
        return ResponseEntity.ok(messageService.getMyMessages());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return ResponseEntity.ok("Okundu");
    }

    @GetMapping("/search-users")
    public ResponseEntity<?> searchUsersForMessage(@RequestParam String keyword) {
        return ResponseEntity.ok(messageService.searchUsersForMessage(keyword));
    }
}