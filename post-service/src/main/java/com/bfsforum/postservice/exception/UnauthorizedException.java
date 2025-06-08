package com.bfsforum.postservice.exception;

import org.springframework.http.HttpStatus;

/**
 * @author luluxue
 * @date 2025-06-07
 */
public class UnauthorizedException extends BaseException {
	private static final String ERROR_CODE = "UNAUTHORIZED_ACCESS";
	private static final HttpStatus HTTP_STATUS = HttpStatus.FORBIDDEN;
	
	public UnauthorizedException(String message) {
		super(message, ERROR_CODE, HTTP_STATUS);
	}
	
	public UnauthorizedException(Long userId, String action) {
		super("User" + userId + " is not authorized to " + action, ERROR_CODE, HTTP_STATUS);
	}
	
	public UnauthorizedException(String message, Throwable cause) {
		super(message, ERROR_CODE, HTTP_STATUS, cause);
	}
}
