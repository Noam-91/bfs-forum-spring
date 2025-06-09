package com.bfsforum.postservice.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
	
	private boolean success = false;
	private String message;
	private String errorCode;
	private int status;
	private String path;
	private LocalDateTime timestamp;
	
	private Map<String, List<String>> filedErrors;
	private List<String> details;
	
	private String debugMessage;
	private String stackTrace;
	
	public static ErrorResponse of(String message, String errorCode, int status, String path) {
		return ErrorResponse.builder()
				.message(message)
				.errorCode(errorCode)
				.status(status)
				.path(path)
				.timestamp(LocalDateTime.now())
				.build();
	}
	
	public static ErrorResponse of(BaseException ex, String path) {
		return ErrorResponse.builder()
				.message(ex.getMessage())
				.errorCode(ex.getErrorCode())
				.status(ex.getHttpStatus().value())
				.path(path)
				.timestamp(LocalDateTime.now())
				.build();
	}
}
