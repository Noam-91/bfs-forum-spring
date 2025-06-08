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
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostViewedEvent {
	private UUID eventId;
	private Long userId;
	private String postId;
	private LocalDateTime viewedAt;
	
	public PostViewedEvent(Long userId, String postId, LocalDateTime viewedAt) {
		this.eventId = UUID.randomUUID();
		this.postId = postId;
		this.viewedAt = viewedAt;
		this.userId = userId;
	}
}
