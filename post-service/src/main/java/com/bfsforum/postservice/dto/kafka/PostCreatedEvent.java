package com.bfsforum.postservice.dto.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author luluxue
 * @date 2025-06-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostCreatedEvent {
	private UUID eventId;
	private String postId;
	private Long userId;
	private String title;
	private LocalDateTime createdAt;
	
	public PostCreatedEvent(String postId, Long userId, String title, LocalDateTime createdAt) {
		this.eventId = UUID.randomUUID();
		this.postId = postId;
		this.userId = userId;
		this.title = title;
		this.createdAt = createdAt;
	}
}
