package com.bfsforum.postservice.exception;

import com.bfsforum.postservice.domain.PostStatus;
import org.springframework.http.HttpStatus;

/**
 * @author luluxue
 * @date 2025-06-07
 */
public class InvalidPostStatusException extends BaseException{
	private static final String ERROR_CODE = "INVALID_POST_STATUS";
	private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;
	
	public InvalidPostStatusException(PostStatus currentStatus, PostStatus targetStatus) {
		super("Cannot change post status from " + currentStatus + " to " + targetStatus,
				ERROR_CODE, HTTP_STATUS);
	}
	
	public InvalidPostStatusException(String message) {
		super(message, ERROR_CODE, HTTP_STATUS);
	}
	
	public InvalidPostStatusException(String message, Throwable cause) {
		super(message, ERROR_CODE, HTTP_STATUS, cause);
	}
}
