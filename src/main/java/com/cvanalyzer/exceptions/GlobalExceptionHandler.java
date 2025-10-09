package com.cvanalyzer.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Geçerli bir değer girin"
                ));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validasyon hatası", null, errors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Erişim reddedildi", ex.getMessage(), null);
    }

    // Özel exception'ları burada yakalayın
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı", ex.getMessage(), null);
    }

    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<Map<String, Object>> handleFileValidation(FileValidationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Dosya geçerlilik hatası", ex.getMessage(), null);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Map<String, Object>> handleFileStorage(FileStorageException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Dosya işlemi hatası", ex.getMessage(), null);
    }

    // Daha genel RuntimeException'ları burada yakalayın
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleGenericRuntimeException(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "İşlem hatası", ex.getMessage(), null);
    }

    // En genel exception'ları en sonda yakalayın
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir hata oluştu", ex.getMessage(), null);
    }

    // Standart yanıt formatını oluşturan yardımcı metot
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message, Map<String, String> errors) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        if (message != null) {
            body.put("message", message);
        }
        if (errors != null && !errors.isEmpty()) {
            body.put("messages", errors);
        }
        return new ResponseEntity<>(body, status);
    }
}