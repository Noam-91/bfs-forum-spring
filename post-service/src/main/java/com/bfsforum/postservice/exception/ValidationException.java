package com.bfsforum.postservice.exception;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * @author luluxue
 * @date 2025-06-08
 */
public class ValidationException extends BaseException {
	
	private static final String ERROR_CODE = "VALIDATOR_ERROR";
	private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;
	
	private final Map<String, List<String>> fileErrors;
	
	public ValidationException(String message) {
		super(message, ERROR_CODE, HTTP_STATUS);
		this.fileErrors = null;
	}
	
	public ValidationException(String message, Map<String, List<String>> fieldErrors) {
		super(message, ERROR_CODE, HTTP_STATUS);
		this.fileErrors = fieldErrors;
	}
	
	public Map<String, List<String>> getFileErrors() {
		return fileErrors;
	}
}
