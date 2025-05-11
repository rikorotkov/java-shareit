package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(
            final ResourceNotFoundException e) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        log.warn("Resource not found: {}", e.getMessage());
        return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExistsException(
            final EmailAlreadyExistsException e) {
        HttpStatus status = HttpStatus.CONFLICT;
        log.warn("Email conflict: {}", e.getMessage());
        return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleUserIsNotOwner(final UserIsNotOwnerException e) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        log.warn("User is not owner: {}", e.getMessage());
        return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
    }
}
