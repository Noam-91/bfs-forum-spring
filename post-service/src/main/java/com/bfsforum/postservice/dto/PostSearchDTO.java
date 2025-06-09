package com.bfsforum.postservice.dto;

import com.bfsforum.postservice.domain.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author luluxue
 * @date 2025-06-08
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostSearchDTO {
	private String keyword;
	private Long userId;
	private PostStatus status;
	private Boolean isArchived;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	private Integer page = 0;
	private Integer size = 10;
	private String sortBy = "createdAt";
	private String sortDir = "desc";
}
