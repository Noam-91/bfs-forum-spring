package com.bfsforum.authservice.exception;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error";

    /** Exception in Validation **/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                                    HttpServletRequest request) {
        String errors = ex.getBindingResult().getAllErrors().stream()
            .map(error -> {
                if (error instanceof FieldError) {
                    return ((FieldError) error).getField() + ": " + error.getDefaultMessage();
                }
                return error.getDefaultMessage();
            })
            .collect(Collectors.joining(", "));

        log.debug("Validation Exception: {}", errors);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .path(request.getRequestURI())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation failed: " + errors)
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /** Exception in JDBC (e.g., database constraint violation) **/
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleException(ConstraintViolationException ex,
                                                         HttpServletRequest request) {
        log.debug("Constraint Violation Exception: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .path(request.getRequestURI()) // Get path from HttpServletRequest
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Database constraint violation: " + ex.getMessage())
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /** Exception in Authentication **/
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleException(InvalidCredentialsException ex,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
        log.debug("Invalid Credentials Exception: {}", ex.getMessage());

        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .path(request.getRequestURI())
            .status(HttpStatus.UNAUTHORIZED.value())
            .error(ex.getMessage())
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /** Default Exception Handler for unhandled RuntimeExceptions **/
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleException(RuntimeException ex,
                                                         HttpServletRequest request) {
        log.error("Internal Server Error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .path(request.getRequestURI())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(INTERNAL_SERVER_ERROR_MESSAGE)
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
