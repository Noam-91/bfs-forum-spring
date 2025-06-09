package com.bfsforum.postservice.exception;

import org.springframework.http.HttpStatus;

/**
 * @author luluxue
 * @date 2025-06-08
 */
public class ServiceUnavailableException extends BaseException {
	
	private static final String ERROR_CODE = "SERVICE_UNAVAILABLE";
	private static final HttpStatus HTTP_STATUS = HttpStatus.SERVICE_UNAVAILABLE;
	
	public ServiceUnavailableException(String serviceName) {
		super("Service " + serviceName + " is currently unavailable", ERROR_CODE, HTTP_STATUS);
	}
	
	public ServiceUnavailableException(String message, Throwable cause) {
		super(message, ERROR_CODE, HTTP_STATUS, cause);
	}
}
