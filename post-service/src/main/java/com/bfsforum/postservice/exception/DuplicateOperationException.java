package com.bfsforum.postservice.exception;

import org.springframework.http.HttpStatus;

/**
 * @author luluxue
 * @date 2025-06-08
 */
public class DuplicateOperationException extends BaseException {
	private static final String ERROR_CODE = "DUPLICATE_OPERATION";
	private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;
	
	public DuplicateOperationException(String message) {
		super(ERROR_CODE, message, HTTP_STATUS);
	}
	
	public DuplicateOperationException(String operation, String resourceId) {
		super("Duplicate operation '" + operation + "' on resource: " + resourceId,
				ERROR_CODE, HTTP_STATUS);
	}
}
