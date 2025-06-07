package com.bfsforum.postservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author luluxue
 * @date 2025-06-06
 */
// 嵌套在Post中的reply
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostReply {
	private String replyId = UUID.randomUUID().toString();           // automatically generated
	private Long userId;
	private String postId;
	private String comment;
	private Boolean isActive = true;  // isActive (false -> deleted)
	private LocalDateTime createdAt = LocalDateTime.now();
	
	// subreply list
	private List<SubReply> subReplies = new ArrayList<>();
	
	// constructor
	public PostReply(Long userId, String postId, String comment) {
		this.userId = userId;
		this.postId = postId;
		this.comment = comment;
	}
}
