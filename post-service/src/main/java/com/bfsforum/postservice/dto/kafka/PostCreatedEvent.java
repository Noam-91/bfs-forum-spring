package com.bfsforum.postservice.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author luluxue
 * @date 2025-06-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreatedEvent {
	private UUID eventId;
	private String postId;
	private Long usrId;
	private String title;
	private LocalDateTime createdAt;
	
	public PostCreatedEvent(String postId, Long usrId, String title, LocalDateTime createdAt) {
		this.eventId = UUID.randomUUID();
		this.postId = postId;
		this.usrId = usrId;
		this.title = title;
		this.createdAt = createdAt;
	}
}
