package com.educonnect.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// SİHİRLİ KELİME: Bu etiket sayesinde Spring bu sınıfı "Hata Yakalayıcı" olarak tanır.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Ne zaman bir RuntimeException fırlatılsa bu metot devreye girer
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {

        // Şık bir JSON formatı oluşturmak için Map kullanıyoruz
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("zaman", LocalDateTime.now());
        errorResponse.put("mesaj", ex.getMessage()); // Bizim serviste yazdığımız "Öğrenci bulunamadı!" mesajı
        errorResponse.put("durumKodu", HttpStatus.NOT_FOUND.value()); // 404 Kodu

        // Bu şık paketi Postman'e geri gönderiyoruz
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("zaman", LocalDateTime.now());
        errorResponse.put("durumKodu", HttpStatus.BAD_REQUEST.value()); // 400 Kodu

        // Burada çok şık bir hareket yapıyoruz: Hangi alanda ne hata var hepsini topluyoruz
        Map<String, String> validasyonHatalari = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                validasyonHatalari.put(error.getField(), error.getDefaultMessage())
        );

        errorResponse.put("hatalar", validasyonHatalari);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("zaman", LocalDateTime.now());
        errorResponse.put("durumKodu", HttpStatus.INTERNAL_SERVER_ERROR.value()); // 500 hatası
        errorResponse.put("mesaj", "Sistemsel beklenmedik bir hata oluştu. Teknik ekiple görüşün.");

        // Geliştirme aşamasında hatanın detayını Postman'de görebilmek için:
        errorResponse.put("detay", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}