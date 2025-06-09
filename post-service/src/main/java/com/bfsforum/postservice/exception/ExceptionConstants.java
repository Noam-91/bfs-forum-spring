package com.bfsforum.postservice.exception;

/**
 * @author luluxue
 * @date 2025-06-08
 */
public final class ExceptionConstants {
	private ExceptionConstants() {
	
	}
	
	public static final String POST_NOT_FOUND = "POST_NOT_FOUND";
	public static final String REPLY_NOT_FOUND = "REPLY_NOT_FOUND";
	public static final String UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS";
	public static final String INVALID_POST_STATUS = "INVALID_POST_STATUS";
	public static final String POST_ARCHIVED = "POST_ARCHIVED";
	public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
	public static final String DUPLICATE_OPERATION = "DUPLICATE_OPERATION";
	public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
	
	public static final String MSG_POST_NOT_FOUND = "Post not found";
	public static final String MSG_REPLY_NOT_FOUND = "Reply not found";
	public static final String MSG_UNAUTHORIZED = "Access denied";
	public static final String MSG_INVALID_STATUS = "Invalid post status transition";
	public static final String MSG_POST_ARCHIVED = "Cannot reply to archived post";
	public static final String MSG_VALIDATION_FAILED = "Validation failed";
}
