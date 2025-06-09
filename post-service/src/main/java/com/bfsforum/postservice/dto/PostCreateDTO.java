package com.bfsforum.postservice.dto;

import com.bfsforum.postservice.domain.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateDTO {
	
	@NotNull(message = "User ID cannot be null")
	private Long userId;
	
	@NotBlank(message = "Title cannot be blank")
	@Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
	private String title;
	
	@NotBlank(message = "Content cannot be blank")
	@Size(min = 1, max = 10000, message = "Content must be between 1 and 10000 characters")
	private String content;
	
	private PostStatus status = PostStatus.UNPUBLISHED; // default status: draft
	
	private Boolean isArchived = false;
	
	// attachments: images and files
	private List<String> images;
	private List<String> attachments;
}
