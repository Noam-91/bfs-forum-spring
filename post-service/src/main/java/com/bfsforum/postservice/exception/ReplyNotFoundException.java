package com.bfsforum.postservice.exception;

import org.springframework.http.HttpStatus;

/**
 * @author luluxue
 * @date 2025-06-08
 */

public class ReplyNotFoundException extends BaseException {
	private static final String ERROR_CODE = "REPLY_NOT_FOUND";
	private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND;
	
	public ReplyNotFoundException(String replyId) {
		super("Reply not found with ID: " + replyId, ERROR_CODE, HTTP_STATUS);
	}
	
	public ReplyNotFoundException(String postId, String replyId) {
		super("Reply " + replyId + " not found in post " + postId, ERROR_CODE, HTTP_STATUS);
	}
	
	public ReplyNotFoundException(String message, Throwable cause) {
		super(message, ERROR_CODE, HTTP_STATUS, cause);
	}
}
