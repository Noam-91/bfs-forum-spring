package com.bfsforum.postservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author luluxue
 * @date 2025-06-06
 */
// 嵌套在Post中的reply
@Document(collation = "replies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reply {
	private String replyId = UUID.randomUUID().toString();           // automatically generated
	private Long userId;
	private String postId;
	private String comment;
	private Boolean isActive = true;  // isActive (false -> deleted)
	private LocalDateTime createdAt = LocalDateTime.now();
}
