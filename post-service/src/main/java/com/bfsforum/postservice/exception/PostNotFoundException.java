package com.bfsforum.postservice.exception;

import org.springframework.http.HttpStatus;

/**
 * @author luluxue
 * @date 2025-06-07
 */
public class PostNotFoundException extends BaseException {
	private static final String ERROR_CODE = "POST_NOT_FOUND";
	private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND;
	
	public PostNotFoundException(String postId) {
		super("Post not found with ID: " + postId, ERROR_CODE, HTTP_STATUS);
	}

	public PostNotFoundException(String message, Throwable cause) {
		super(message, ERROR_CODE, HTTP_STATUS, cause);
	}
	
}
