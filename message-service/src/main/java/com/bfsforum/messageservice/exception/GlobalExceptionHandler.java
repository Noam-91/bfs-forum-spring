package com.bfsforum.messageservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException ex) {
        log.error("NotFoundException: {}", ex.getMessage());
        return new ResponseEntity<>(Map.of("error",ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<Object> handleNotAuthorizedException(NotAuthorizedException ex) {
        log.error("NotAuthorizedException: {}", ex.getMessage());
        return new ResponseEntity<>(Map.of("error",ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex) {
        log.error("BadRequestException: {}", ex.getMessage());
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException: {}", ex.getMessage());
        return new ResponseEntity<>(Map.of("error", INTERNAL_SERVER_ERROR_MESSAGE), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
