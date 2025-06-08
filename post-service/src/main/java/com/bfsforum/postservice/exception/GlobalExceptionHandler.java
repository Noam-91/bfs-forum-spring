package com.bfsforum.postservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * @author luluxue
 * @date 2025-06-07
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<Object> handleBaseException(BaseException e) {
		log.error("{}: {}", e.getErrorCode(), e.getMessage());
		Map<String, Object> body = Map.of(
				"error", e.getMessage(),
				"code", e.getErrorCode()
		);
		
		return new ResponseEntity<>(body, e.getHttpStatus());
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleUnexpected(Exception e) {
		log.error("Unhandled exception", e);
		Map<String, Object> body = Map.of(
				"error", "Internal Server Error",
				"code", "INTERNAL_ERROR"
		);
		return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
