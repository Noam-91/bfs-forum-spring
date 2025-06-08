package com.bfsforum.postservice.exception;

import lombok.extern.slf4j.Slf4j;
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
}
