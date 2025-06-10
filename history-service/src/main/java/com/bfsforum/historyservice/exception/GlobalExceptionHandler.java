package com.bfsforum.historyservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


//    @ExceptionHandler(ResponseStatusException.class)
//    public ResponseEntity<?> handleResponseStatus(ResponseStatusException ex) {
//        log.error("ResponseStatusException: {}", ex.getMessage());
//        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", "error message: " + ex.getMessage()));
//    }
//
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<?> handleAll(Exception ex) {
//        log.error("UnexpectedException: {}", ex.getMessage());
//        return ResponseEntity
//                .status(500)
//                .body(Map.of("error", "error message: "+ex.getMessage()));
//    }
// 1) Handle ResponseStatusException (400–500 responses as thrown in your code)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,String>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        log.error("ResponseStatusException [{}]: {}", status, message);
        return ResponseEntity
                .status(status)
                .body(Map.of("error", message));
    }

    // 2) TimeoutException → 504
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<Map<String,String>> handleTimeout(TimeoutException ex) {
        log.error("TimeoutException: {}", ex.getMessage());
        return ResponseEntity
                .status(504)  // HttpStatus implements HttpStatusCode
                .body(Map.of("error", "Request timed out: " + ex.getMessage()));
    }

    // 3) TypeMismatch (e.g. bad date format) → 400
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String field = ex.getPropertyName();
        String requiredType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "value";
        String msg = String.format("Parameter '%s' must be a valid %s", field, requiredType);

        log.error("TypeMismatchException on '{}': expected {}", field, requiredType);
        return ResponseEntity
                .status(status)
                .body(Map.of("error", msg));
    }

    // 4) Fallback → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> handleAll(Exception ex) {
        log.error("Unexpected exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
    }
}
