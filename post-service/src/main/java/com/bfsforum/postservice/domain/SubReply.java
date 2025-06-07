package com.bfsforum.postservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author luluxue
 * @date 2025-06-06
 */
// subreply
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubReply {
	private String subReplyId = UUID.randomUUID().toString();        // subreplyID
	private Long userId;
	private String parentReplyId;
	private String comment;
	private Boolean isActive = true;
	private LocalDateTime createdAt = LocalDateTime.now();
	
	// constructor
	public SubReply(Long userId, String comment) {
		this.userId = userId;
		this.parentReplyId = parentReplyId;
		this.comment = comment;
	}
}