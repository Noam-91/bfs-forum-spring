package com.bfsforum.postservice.dto.kafka;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.domain.PostStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDTO {
	private String postId;
	private Long userId;
	private String title;
	private String content;
	private PostStatus status;
	private Boolean isArchived;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Integer viewCount;
	private Integer replyCount;
}
