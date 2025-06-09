package com.bfsforum.postservice.exception;

import org.springframework.http.HttpStatus;

/**
 * @author luluxue
 * @date 2025-06-08
 */
public class PostArchivedException extends BaseException {
	private static final String ERROR_CODE = "POST_ARCHIVED";
	private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;
	
	private PostArchivedException(String message) {
		super(message, ERROR_CODE, HTTP_STATUS);
	}
	
	public static PostArchivedException forPostId(String postId) {
		return new PostArchivedException("Post " + postId + " is archived and does not allow new replies");
	}
	
	public PostArchivedException withMessage(String message) {
		return new PostArchivedException(message);
	}
}
