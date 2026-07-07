package com.educonnect.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private String receiverId; // "SUPER_ADMIN", "ALL" veya normal bir ID ("5") olabilir
    private String subject;
    private String content;
}