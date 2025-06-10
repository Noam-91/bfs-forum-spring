package com.bfsforum.postservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author luluxue
 * @date 2025-06-09
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubReply {
	@Builder.Default
	private String id = UUID.randomUUID().toString();
	private String userId;
	private String comment;
	@Builder.Default
	private Boolean isActive = true;
	private LocalDateTime createdAt;
}
