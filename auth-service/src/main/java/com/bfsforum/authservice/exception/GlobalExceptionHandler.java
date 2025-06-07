package com.bfsforum.authservice.exception;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error";

    /** Exception in Validation **/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
          log.debug("{}: {}", fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().build();
    }

    /** Exception in JDBC **/
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleException(ConstraintViolationException ex) {
        log.debug(ex.getMessage());
        return ResponseEntity.internalServerError().body(INTERNAL_SERVER_ERROR_MESSAGE);
    }

    /** Exception in Authentication **/
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> handleException(InvalidCredentialsException ex,
                                                  HttpServletResponse response) {
        log.debug(ex.getMessage());
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    /** Exception default */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        log.debug(ex.getMessage());
        return ResponseEntity.internalServerError().body(INTERNAL_SERVER_ERROR_MESSAGE);
    }
}
