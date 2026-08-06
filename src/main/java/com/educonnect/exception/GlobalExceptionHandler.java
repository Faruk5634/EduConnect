package com.educonnect.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 🔒 SECURITY FIX: raw exception messages are only ever echoed to the
    // client when this is explicitly enabled (e.g. a local/dev profile).
    // In production this MUST stay false, or internal error detail
    // (stack traces, SQL fragments, etc.) leaks to callers.
    @Value("${app.debug-errors:false}")
    private boolean debugErrors;

    // 🐛 CORRECTNESS FIX: IllegalArgumentException means "bad input" (400),
    // not "not found" (404). Genuine not-found cases should throw
    // ResourceNotFoundException or ResponseStatusException(NOT_FOUND, ...).
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("zaman", LocalDateTime.now());
        errorResponse.put("durumKodu", HttpStatus.BAD_REQUEST.value());

        Map<String, String> validasyonHatalari = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                validasyonHatalari.put(error.getField(), error.getDefaultMessage())
        );
        errorResponse.put("hatalar", validasyonHatalari);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        return buildResponse(HttpStatus.valueOf(ex.getStatusCode().value()),
                ex.getReason() != null ? ex.getReason() : ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Bu işlemi gerçekleştirmek için yetkiniz yok.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        // Always log the real error server-side, regardless of what we tell the client.
        log.error("Unhandled exception", ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("zaman", LocalDateTime.now());
        errorResponse.put("durumKodu", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("mesaj", "Sistemsel beklenmedik bir hata oluştu. Teknik ekiple görüşün.");

        if (debugErrors) {
            errorResponse.put("detay", ex.getMessage());
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("zaman", LocalDateTime.now());
        errorResponse.put("durumKodu", status.value());
        errorResponse.put("mesaj", message);
        return new ResponseEntity<>(errorResponse, status);
    }
}